package com.jh.caraudio;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.SystemClock;

/**
 * Mezcla automática (crossfade) entre la canción que suena y la siguiente de la cola.
 *
 * - Precarga la siguiente canción con anticipación (prepareAsync) para que un pendrive lento
 *   no deje silencio.
 * - Mezcla con curva de igual potencia (cos/sin): el volumen percibido no baja a mitad de la mezcla.
 * - Si la canción termina antes de lo previsto (duración mal informada en MP3 VBR), la siguiente
 *   ya preparada entra al instante.
 *
 * Todo corre en el hilo principal (Handler); los callbacks de MediaPlayer llegan a ese mismo hilo.
 */
final class AutoMixer {

    /** Lo que el mezclador necesita de la Activity. */
    interface Host {
        MediaPlayer getCurrentPlayer();

        /** Reproduciendo de verdad (no en pausa ni arrastrando la barra de progreso). */
        boolean isPlaybackActive();

        /** Siguiente canción de la cola, o null si no corresponde mezclar (repetir, cola de 1...). */
        NextTrack pickNextTrack();

        /** La siguiente elegida sigue siendo válida (la cola no cambió desde la precarga). */
        boolean isStillNext(NextTrack next);

        /** Reproductor nuevo con los mismos listeners que el principal. */
        MediaPlayer createPlayer();

        /** La siguiente empezó a sonar: pasa a ser el reproductor principal y se actualiza la pantalla. */
        void onMixStarted(MediaPlayer incoming, NextTrack next);
    }

    static final class NextTrack {
        final int index;
        final Track track;

        NextTrack(int index, Track track) {
            this.index = index;
            this.track = track;
        }
    }

    static final int DEFAULT_MIX_MS = 5000;
    private static final int PRELOAD_LEAD_MS = 15000;     // precargar 15 s antes de empezar la mezcla
    private static final int LONG_TRACK_MS = 10 * 60 * 1000; // mixes largos (sets de DJ)
    private static final int LONG_TRACK_MIX_MS = 2000;
    private static final int EARLY_END_FADE_MS = 300;     // entrada al terminar antes de lo previsto
    private static final int TICK_MS = 250;
    private static final int FADE_STEP_MS = 50;           // pasos más largos se oyen "escalonados"

    private final Handler handler;
    private final Host host;

    private boolean enabled = false;
    private int mixDurationMs = DEFAULT_MIX_MS;

    // Precarga de la siguiente
    private MediaPlayer nextPlayer;
    private NextTrack nextTrack;
    private boolean nextReady = false;
    private MediaPlayer preloadedFor;   // reproductor principal para el que se hizo (o intentó) la precarga

    // Mezcla en curso
    private MediaPlayer outgoing;
    private long fadeStartMs;
    private int fadeDurationMs;

    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            tick();
            handler.postDelayed(this, TICK_MS);
        }
    };

    private final Runnable fadeRunnable = new Runnable() {
        @Override
        public void run() {
            stepFade();
        }
    };

    AutoMixer(Handler handler, Host host) {
        this.handler = handler;
        this.host = host;
    }

    boolean isEnabled() {
        return enabled;
    }

    void setEnabled(boolean on) {
        enabled = on;
        handler.removeCallbacks(tickRunnable);
        if (on) {
            handler.post(tickRunnable);
        } else {
            finishMixNow();
            cancelPreload();
        }
    }

    boolean isMixing() {
        return outgoing != null;
    }

    boolean isOutgoing(MediaPlayer mp) {
        return mp != null && mp == outgoing;
    }

    /** Reproducción iniciada a mano (tocar canción, Siguiente/Anterior...): descartar mezcla y precarga. */
    void onManualPlayback() {
        finishMixNow();
        cancelPreload();
    }

    /** Cambió algo que afecta cuál es la siguiente (aleatorio, repetir, cola). */
    void invalidateNext() {
        cancelPreload();
    }

    /** La canción que salía terminó antes que el fundido. */
    void onOutgoingCompleted() {
        endMix();
    }

    /**
     * La canción principal terminó y no hubo mezcla (duración mal informada o mezcla no aplicable).
     * Si la siguiente ya está preparada, la arranca al instante. Devuelve false si no había nada listo.
     */
    boolean startPreparedNextNow() {
        MediaPlayer current = host.getCurrentPlayer();
        if (!enabled || !nextReady || nextPlayer == null || !host.isStillNext(nextTrack)) {
            cancelPreload();
            return false;
        }
        startMix(current, EARLY_END_FADE_MS);
        return true;
    }

    /** Termina la mezcla en curso de inmediato: queda solo la canción nueva, a volumen completo. */
    void finishMixNow() {
        handler.removeCallbacks(fadeRunnable);
        if (outgoing != null) {
            releaseQuietly(outgoing);
            outgoing = null;
        }
        MediaPlayer current = host.getCurrentPlayer();
        if (current != null) {
            try {
                current.setVolume(1f, 1f);
            } catch (Exception ignored) {}
        }
    }

    void release() {
        handler.removeCallbacks(tickRunnable);
        finishMixNow();
        cancelPreload();
    }

    // ------------------------------------------------------------------

    private void tick() {
        if (!enabled || isMixing()) return;
        MediaPlayer current = host.getCurrentPlayer();
        if (current == null || !host.isPlaybackActive()) return;

        int durationMs;
        int positionMs;
        try {
            durationMs = current.getDuration();
            positionMs = current.getCurrentPosition();
        } catch (Exception e) {
            return;
        }
        if (durationMs <= 0) return;

        int mixMs = mixDurationFor(durationMs, 0);
        if (mixMs == 0) return;
        int remainingMs = durationMs - positionMs;

        if (preloadedFor != current && remainingMs <= mixMs + PRELOAD_LEAD_MS) {
            preload(current);
        }

        if (nextReady && remainingMs <= mixMs) {
            if (!host.isStillNext(nextTrack)) {
                cancelPreload();
                return;
            }
            int nextDurationMs = 0;
            try {
                nextDurationMs = nextPlayer.getDuration();
            } catch (Exception ignored) {}
            int finalMixMs = mixDurationFor(durationMs, nextDurationMs);
            if (finalMixMs == 0) return; // la siguiente es muy corta: entrará al terminar (respaldo)
            startMix(current, Math.max(EARLY_END_FADE_MS, Math.min(finalMixMs, remainingMs)));
        }
    }

    /** Reglas de duración de la mezcla; 0 = no mezclar. */
    private int mixDurationFor(int currentDurationMs, int nextDurationMs) {
        int mix = mixDurationMs;
        if (currentDurationMs >= LONG_TRACK_MS) mix = Math.min(mix, LONG_TRACK_MIX_MS);
        if (currentDurationMs < mix * 3) return 0;               // la mezcla se comería la canción
        if (nextDurationMs > 0 && nextDurationMs < mix * 3) return 0;
        return mix;
    }

    private void preload(MediaPlayer forPlayer) {
        cancelPreload();
        preloadedFor = forPlayer; // aunque falle, no reintentar en cada tick para esta misma canción
        NextTrack next = host.pickNextTrack();
        if (next == null) return;

        final MediaPlayer player = host.createPlayer();
        nextPlayer = player;
        nextTrack = next;
        try {
            player.setDataSource(next.track.getFilePath());
            player.setOnPreparedListener(mp -> {
                if (mp == nextPlayer) nextReady = true;
            });
            player.setOnErrorListener((mp, what, extra) -> {
                if (mp == nextPlayer) {
                    // Archivo dañado o pendrive retirado: sin mezcla, el respaldo pasará a la siguiente
                    discardNextPlayer();
                    return true;
                }
                // Ya es el reproductor principal: comportamiento normal (onCompletion pasa a la siguiente)
                return false;
            });
            player.prepareAsync();
        } catch (Exception e) {
            discardNextPlayer();
        }
    }

    private void startMix(MediaPlayer current, int durationMs) {
        MediaPlayer incoming = nextPlayer;
        NextTrack next = nextTrack;
        nextPlayer = null;
        nextTrack = null;
        nextReady = false;

        try {
            incoming.setVolume(0f, 0f);
            incoming.start();
        } catch (Exception e) {
            releaseQuietly(incoming);
            return;
        }

        outgoing = current;
        fadeStartMs = SystemClock.uptimeMillis();
        fadeDurationMs = durationMs;
        preloadedFor = null;

        host.onMixStarted(incoming, next);
        handler.post(fadeRunnable);
    }

    private void stepFade() {
        MediaPlayer incoming = host.getCurrentPlayer();
        if (outgoing == null || incoming == null) {
            finishMixNow();
            return;
        }
        float t = Math.min(1f, (SystemClock.uptimeMillis() - fadeStartMs) / (float) fadeDurationMs);
        // Igual potencia: cos²+sin²=1, sin hueco de volumen a mitad de la mezcla
        float outVolume = (float) Math.cos(t * Math.PI / 2);
        float inVolume = (float) Math.sin(t * Math.PI / 2);
        try {
            outgoing.setVolume(outVolume, outVolume);
            incoming.setVolume(inVolume, inVolume);
        } catch (Exception ignored) {}

        if (t >= 1f) {
            endMix();
        } else {
            handler.postDelayed(fadeRunnable, FADE_STEP_MS);
        }
    }

    private void endMix() {
        handler.removeCallbacks(fadeRunnable);
        if (outgoing != null) {
            releaseQuietly(outgoing);
            outgoing = null;
        }
        MediaPlayer current = host.getCurrentPlayer();
        if (current != null) {
            try {
                current.setVolume(1f, 1f);
            } catch (Exception ignored) {}
        }
    }

    private void cancelPreload() {
        discardNextPlayer();
        preloadedFor = null;
    }

    private void discardNextPlayer() {
        if (nextPlayer != null) {
            releaseQuietly(nextPlayer);
            nextPlayer = null;
        }
        nextTrack = null;
        nextReady = false;
    }

    private static void releaseQuietly(MediaPlayer mp) {
        try {
            mp.release();
        } catch (Exception ignored) {}
    }
}
