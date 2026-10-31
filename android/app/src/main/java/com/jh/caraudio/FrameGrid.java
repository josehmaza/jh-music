package com.jh.caraudio;

import android.os.SystemClock;

/**
 * Rejilla de tiempo común para las animaciones propias (marquesinas, pastilla, ondas, ecualizador).
 *
 * En este radio cada repintado dibuja la ventana entera, así que importa más cuántos repintados
 * distintos hay por segundo que lo caro que sea cada uno. Si cada vista usa su propio temporizador
 * de 50 ms, nunca coinciden y 4 animaciones a 20 FPS suman ~60 repintados por segundo. Alineadas a
 * la misma rejilla caen en el mismo cuadro y suman 20.
 */
final class FrameGrid {

    /** Paso de la rejilla: ~20 FPS. */
    static final long STEP_MS = 50;

    private FrameGrid() {
    }

    /** Milisegundos hasta el próximo cuadro de la rejilla (al menos 1). */
    static long delayToNext() {
        return delayTo(0);
    }

    /** Milisegundos hasta el primer cuadro de la rejilla que cae a {@code waitMs} o después. */
    static long delayTo(long waitMs) {
        long now = SystemClock.uptimeMillis();
        long target = ((now + waitMs + STEP_MS - 1) / STEP_MS) * STEP_MS;
        return Math.max(1, target - now);
    }
}
