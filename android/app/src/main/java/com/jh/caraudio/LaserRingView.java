package com.jh.caraudio;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

/**
 * Borde de precisión para el cuadro de la carátula (0 FPS en estado estable):
 * - Dark Mode: Cian (#00D2FF) en Playing → Magenta (#F43F5E) en Automix.
 * - Light Mode: Azul Cobalto (#2563EB) en Playing → Turquesa (#0D9488) en Automix.
 */
public class LaserRingView extends View {

    private static final long FADE_MS = 300;
    private static final long FRAME_MS = 33;

    private static final int DARK_PLAY = Color.rgb(0, 210, 255);
    private static final int DARK_MIX = Color.rgb(244, 63, 94);
    private static final int LIGHT_PLAY = Color.rgb(29, 78, 216);
    private static final int LIGHT_MIX = Color.rgb(124, 58, 237); // Violeta Índigo

    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF strokeRect = new RectF();

    private boolean lightMode = false;
    private boolean playing = false;
    private boolean mixing = false;

    private float currentAlpha = 0.12f;
    private float targetAlpha = 0.12f;

    private float mixProgress = 0f;
    private float targetMixProgress = 0f;

    private long lastFrameMs = 0;
    private float strokeWidth = 2f;
    private float cornerRadius = 20f;

    public LaserRingView(Context context) {
        this(context, null);
    }

    public LaserRingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;
        strokeWidth = 2f * density;
        cornerRadius = 20f * density;

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(strokeWidth);
    }

    public void setLightMode(boolean light) {
        if (this.lightMode == light) return;
        this.lightMode = light;
        invalidate();
    }

    public void setPlaying(boolean playing) {
        if (this.playing == playing) return;
        this.playing = playing;
        updateTargets();
    }

    public void setMixing(boolean mixing) {
        if (this.mixing == mixing) return;
        this.mixing = mixing;
        updateTargets();
    }

    private void updateTargets() {
        if (mixing) {
            targetAlpha = 0.90f;
            targetMixProgress = 1f;
        } else if (playing) {
            targetAlpha = 0.80f;
            targetMixProgress = 0f;
        } else {
            targetAlpha = lightMode ? 0.18f : 0.12f;
            targetMixProgress = 0f;
        }
        lastFrameMs = SystemClock.uptimeMillis();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float half = strokeWidth / 2f;
        strokeRect.set(half, half, w - half, h - half);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (strokeRect.isEmpty()) return;

        long now = SystemClock.uptimeMillis();
        float dt = lastFrameMs == 0 ? 0f : (now - lastFrameMs) / (float) FADE_MS;
        lastFrameMs = now;

        currentAlpha = approach(currentAlpha, targetAlpha, dt);
        mixProgress = approach(mixProgress, targetMixProgress, dt);

        int playCol = lightMode ? LIGHT_PLAY : DARK_PLAY;
        int mixCol = lightMode ? LIGHT_MIX : DARK_MIX;
        int color = interpolateColor(playCol, mixCol, mixProgress);
        int alphaInt = Math.round(255 * currentAlpha);
        borderPaint.setColor((color & 0x00FFFFFF) | (alphaInt << 24));

        canvas.drawRoundRect(strokeRect, cornerRadius, cornerRadius, borderPaint);

        boolean animating = (Math.abs(currentAlpha - targetAlpha) > 0.005f) ||
                            (Math.abs(mixProgress - targetMixProgress) > 0.005f);

        if (animating) {
            postInvalidateDelayed(FRAME_MS);
        } else {
            currentAlpha = targetAlpha;
            mixProgress = targetMixProgress;
            lastFrameMs = 0;
        }
    }

    private static float approach(float value, float target, float step) {
        if (step <= 0f) return value;
        if (value < target) return Math.min(target, value + step);
        if (value > target) return Math.max(target, value - step);
        return value;
    }

    private static int interpolateColor(int c1, int c2, float ratio) {
        float r1 = Color.red(c1), g1 = Color.green(c1), b1 = Color.blue(c1);
        float r2 = Color.red(c2), g2 = Color.green(c2), b2 = Color.blue(c2);
        int r = Math.round(r1 + (r2 - r1) * ratio);
        int g = Math.round(g1 + (g2 - g1) * ratio);
        int b = Math.round(b1 + (b2 - b1) * ratio);
        return Color.rgb(r, g, b);
    }
}
