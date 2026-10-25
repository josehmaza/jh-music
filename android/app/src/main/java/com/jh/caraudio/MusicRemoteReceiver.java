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
            }
        } else {
            // Si la app de música estaba cerrada y el usuario presiona Play en el Launcher,
            // se inicia en modo segundo plano protegido sin tapar la pantalla del launcher
            if ("com.jh.music.action.PLAY_PAUSE".equals(action)) {
                Intent i = new Intent(context, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("extra_from_launcher_bg", true);
                context.startActivity(i);
            }
        }
    }
}
