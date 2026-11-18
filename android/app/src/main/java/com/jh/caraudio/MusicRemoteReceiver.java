package com.jh.caraudio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MusicRemoteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        String action = intent.getAction();

        MainActivity act = MainActivity.getInstance();
        if (act != null) {
            if ("com.jh.music.action.PLAY_PAUSE".equals(action)) {
                act.togglePlayPause();
            } else if ("com.jh.music.action.NEXT".equals(action)) {
                act.playNextTrack();
            } else if ("com.jh.music.action.PREV".equals(action)) {
                act.playPreviousTrack();
            } else if ("com.jh.music.action.REQUEST_STATE".equals(action)) {
                act.requestCurrentStateBroadcast();
            } else if ("com.jh.music.action.SEEK".equals(action)) {
                // Arrastre de la barra de tiempo del launcher: segundo al que ir (sin la app abierta no hay canción)
                int sec = intent.getIntExtra("extra_position_sec", -1);
                if (sec >= 0) act.seekFromLauncher(sec);
            } else if ("com.jh.music.action.TOGGLE_SHUFFLE".equals(action)) {
                act.toggleShuffleFromLauncher();
            }
        } else {
            // Si JH Music está cerrada y el usuario toca Aleatorio en el Launcher, se alterna directo en preferencias
            if ("com.jh.music.action.TOGGLE_SHUFFLE".equals(action)) {
                android.content.SharedPreferences prefs = context.getSharedPreferences("jh_music", Context.MODE_PRIVATE);
                boolean nextShuffle = !prefs.getBoolean("shuffle_on", false);
                prefs.edit().putBoolean("shuffle_on", nextShuffle).apply();
                try {
                    java.io.File f = new java.io.File(android.os.Environment.getExternalStorageDirectory(), ".jh_music_state.json");
                    if (f.exists() && f.length() > 0 && f.length() < 64 * 1024) {
                        byte[] buf = new byte[(int) f.length()];
                        try (java.io.FileInputStream fis = new java.io.FileInputStream(f)) {
                            int n = fis.read(buf);
                            if (n > 0) {
                                org.json.JSONObject json = new org.json.JSONObject(new String(buf, 0, n, java.nio.charset.StandardCharsets.UTF_8));
                                json.put("shuffle", nextShuffle);
                                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(f)) {
                                    fos.write(json.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                                }
                            }
                        }
                    }
                } catch (Throwable ignored) {}
                return;
            }
            // Si la app de música estaba cerrada y el usuario presiona Play, Next o Prev en el Launcher,
            // se inicia en segundo plano protegido ejecutando la acción solicitada
            if ("com.jh.music.action.PLAY_PAUSE".equals(action) ||
                "com.jh.music.action.NEXT".equals(action) ||
                "com.jh.music.action.PREV".equals(action)) {
                Intent i = new Intent(context, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                i.putExtra("extra_from_launcher_bg", true);
                i.putExtra("extra_pending_action", action);
                try {
                    android.app.ActivityOptions opts = android.app.ActivityOptions.makeCustomAnimation(context, 0, 0);
                    context.startActivity(i, opts.toBundle());
                } catch (Exception e) {
                    context.startActivity(i);
                }
            }
        }
    }
}
