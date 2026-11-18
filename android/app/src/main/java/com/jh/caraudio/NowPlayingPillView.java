package com.jh.caraudio;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

/**
 * Cápsula de estado al pie de la carátula: PAUSED / PLAYING / MIXING ⇄ + 5 barras de ecualizador.
 * - Dark Mode: fondo oscuro translúcido, Cian en Playing y Magenta en Automix.
 * - Light Mode: fondo blanco (#FFFFFF / #F0FDFA en mix), Azul Cobalto (#2563EB) en Playing y Turquesa (#0D9488) en Automix.
 */
public class NowPlayingPillView extends View {

    private static final int DARK_PLAY = Color.rgb(0, 210, 255);
    private static final int DARK_PLAY_HI = Color.rgb(103, 232, 249);
    private static final int DARK_MIX = Color.rgb(244, 63, 94);
    private static final int DARK_MIX_HI = Color.rgb(251, 113, 133);

    private static final int LIGHT_PLAY = Color.rgb(29, 78, 216);
    private static final int LIGHT_PLAY_HI = Color.rgb(96, 165, 250);
    private static final int LIGHT_MIX = Color.rgb(124, 58, 237);
    private static final int LIGHT_MIX_HI = Color.rgb(167, 139, 250);
    private static final int MUTED = Color.rgb(100, 116, 139);

    private static final float[] BAR_PERIOD_S = {0.7f, 0.5f, 0.8f, 0.6f, 0.75f};
    private static final float[] BAR_DELAY_S = {0f, 0.15f, 0.3f, 0.2f, 0.05f};

    private static final float PAD_X = 18f;
    private static final float BAR_W = 5.5f;
    private static final float BAR_GAP = 4f;
    private static final float BAR_MAX_H = 22f;

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float density;

    private LinearGradient playGradient;
    private LinearGradient mixGradient;

    private boolean lightMode = false;
    private boolean playing = false;
    private boolean mixing = false;
    private final long startMs = SystemClock.uptimeMillis();

    public NowPlayingPillView(Context context) {
        this(context, null);
    }

    public NowPlayingPillView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1.5f * density);
        textPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        textPaint.setTextSize(15 * getResources().getDisplayMetrics().scaledDensity);
        textPaint.setLetterSpacing(0.05f);
    }

    public void setLightMode(boolean light) {
        if (this.lightMode == light) return;
        this.lightMode = light;
        rebuildGradients(getHeight());
        invalidate();
    }

    public void setPlaying(boolean playing) {
        if (this.playing == playing) return;
        this.playing = playing;
        invalidate();
    }

    public void setMixing(boolean mixing) {
        if (this.mixing == mixing) return;
        this.mixing = mixing;
        invalidate();
    }

    private String label() {
        if (mixing) return "MIXING ⇄";
        return playing ? "PLAYING" : "PAUSED";
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float textW = textPaint.measureText("MIXING ⇄");
        int w = Math.round(PAD_X * density + textW + 12 * density + barsWidth() + PAD_X * density);
        int h = Math.round(38 * density);
        setMeasuredDimension(w, h);
    }

    private float barsWidth() {
        return 5 * BAR_W * density + 4 * BAR_GAP * density;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rebuildGradients(h);
    }

    private void rebuildGradients(int h) {
        if (h <= 0) return;
        float maxH = BAR_MAX_H * density;
        int barsBottom = Math.round(h / 2f + maxH / 2f);
        int p1 = lightMode ? LIGHT_PLAY : DARK_PLAY;
        int p2 = lightMode ? LIGHT_PLAY_HI : DARK_PLAY_HI;
        int m1 = lightMode ? LIGHT_MIX : DARK_MIX;
        int m2 = lightMode ? LIGHT_MIX_HI : DARK_MIX_HI;
        playGradient = new LinearGradient(0, barsBottom, 0, barsBottom - maxH, p1, p2, Shader.TileMode.CLAMP);
        mixGradient = new LinearGradient(0, barsBottom, 0, barsBottom - maxH, m1, m2, Shader.TileMode.CLAMP);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        boolean active = playing || mixing;
        int accent = mixing ? (lightMode ? LIGHT_MIX : DARK_MIX) : (lightMode ? LIGHT_PLAY : DARK_PLAY);

        float w = getWidth();
        float h = getHeight();
        float radius = h / 2f;

        if (lightMode) {
            bgPaint.setColor(mixing ? 0xFFF5F3FF : 0xFFFFFFFF);
        } else {
            bgPaint.setColor(Color.argb(228, 10, 14, 22));
        }

        rect.set(0.5f * density, 0.5f * density, w - 0.5f * density, h - 0.5f * density);
        canvas.drawRoundRect(rect, radius, radius, bgPaint);

        if (active) {
            borderPaint.setColor(withAlpha(accent, mixing ? 0.72f : 0.58f));
        } else {
            borderPaint.setColor(lightMode ? 0xFFD8E1EC : Color.argb(31, 255, 255, 255));
        }
        canvas.drawRoundRect(rect, radius, radius, borderPaint);

        String text = label();
        float textX = PAD_X * density;
        float textY = h / 2f - (textPaint.descent() + textPaint.ascent()) / 2f;
        textPaint.setColor(active ? accent : MUTED);
        canvas.drawText(text, textX, textY, textPaint);

        int barW = Math.round(BAR_W * density);
        int gap = Math.round(BAR_GAP * density);
        float maxH = BAR_MAX_H * density;
        float minH = 4 * density;
        int barsX = Math.round(w - PAD_X * density - barsWidth());
        int barsBottom = Math.round(h / 2f + maxH / 2f);
        float tSec = (SystemClock.uptimeMillis() - startMs) / 1000f;

        if (active) {
            barPaint.setShader(mixing ? mixGradient : playGradient);
        } else {
            barPaint.setShader(null);
            barPaint.setColor(lightMode ? 0xFFCBD5E1 : Color.argb(64, 255, 255, 255));
        }

        float corner = 1.5f * density;
        for (int i = 0; i < 5; i++) {
            float barH = minH;
            if (active) {
                float t = (tSec - BAR_DELAY_S[i]) / BAR_PERIOD_S[i];
                if (t < 0) t = 0;
                float cycle = t % 2f;
                float linear = cycle <= 1f ? cycle : 2f - cycle;
                float eased = (float) (0.5 - 0.5 * Math.cos(Math.PI * linear));
                barH = minH + (maxH - minH) * eased;
            }
            int left = barsX + i * (barW + gap);
            int top = barsBottom - Math.round(barH);
            rect.set(left, top, left + barW, barsBottom);
            canvas.drawRoundRect(rect, corner, corner, barPaint);
        }

        if (active) postInvalidateDelayed(FrameGrid.delayToNext());
    }

    private static int withAlpha(int color, float alpha) {
        return Color.argb(Math.round(255 * alpha), Color.red(color), Color.green(color), Color.blue(color));
    }
}
