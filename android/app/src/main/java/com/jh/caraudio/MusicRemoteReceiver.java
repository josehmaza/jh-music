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
