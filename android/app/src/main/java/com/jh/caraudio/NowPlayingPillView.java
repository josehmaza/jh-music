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
 * Cápsula de estado al pie de la carátula, igual que la web (.now-playing-pill):
 * texto PAUSED / PLAYING / MIXING ⇄ y 5 barras de ecualizador.
 *
 * - En pausa: barras bajas y grises, texto apagado, borde tenue.
 * - Reproduciendo: barras cian que suben y bajan, texto y borde cian con brillo.
 * - Mezclando (Automix): todo pasa a magenta.
 *
 * Se anima con postInvalidateDelayed (no con Animator) y solo mientras suena.
 */
public class NowPlayingPillView extends View {

    private static final long FRAME_MS = 33;
    private static final int CYAN = Color.rgb(0, 210, 255);
    private static final int CYAN_LIGHT = Color.rgb(165, 243, 252);   // #a5f3fc
    private static final int MAGENTA = Color.rgb(244, 63, 94);        // #f43f5e
    private static final int MAGENTA_LIGHT = Color.rgb(251, 207, 232); // #fbcfe8
    private static final int MUTED = Color.rgb(100, 116, 139);        // text-muted

    // Duración y retraso de cada barra (pillWave de la web, ida y vuelta)
    private static final float[] BAR_PERIOD_S = {0.7f, 0.5f, 0.8f, 0.6f, 0.75f};
    private static final float[] BAR_DELAY_S = {0f, 0.15f, 0.3f, 0.2f, 0.05f};

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float density;

    private boolean playing = false;
    private boolean mixing = false;
    private final long startMs = SystemClock.uptimeMillis();

    public NowPlayingPillView(Context context) {
        this(context, null);
    }

    public NowPlayingPillView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
        bgPaint.setColor(Color.argb(224, 10, 14, 22));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1 * density);
        glowPaint.setStyle(Paint.Style.STROKE);
        textPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        textPaint.setTextSize(10 * getResources().getDisplayMetrics().scaledDensity);
        textPaint.setLetterSpacing(0.05f);
        // El brillo (box-shadow / text-shadow de la web) usa sombras de software
        setLayerType(LAYER_TYPE_SOFTWARE, null);
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
        // Ancho según el texto más largo, para que la cápsula no cambie de tamaño al cambiar de estado
        float textW = textPaint.measureText("MIXING ⇄");
        int w = Math.round(12 * density + textW + 3 * density + 5 * density + barsWidth() + 12 * density);
        int h = Math.round(24 * density);
        setMeasuredDimension(w, h);
    }

    private float barsWidth() {
        return 5 * 3.5f * density + 4 * 3 * density;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        boolean active = playing || mixing;
        int accent = mixing ? MAGENTA : CYAN;
        int accentLight = mixing ? MAGENTA_LIGHT : CYAN_LIGHT;

        float w = getWidth();
        float h = getHeight();
        float radius = h / 2f;

        // Fondo cápsula
        rect.set(0.5f * density, 0.5f * density, w - 0.5f * density, h - 0.5f * density);
        canvas.drawRoundRect(rect, radius, radius, bgPaint);

        // Borde: tenue en pausa, cian (0.45) al reproducir, magenta (0.65) al mezclar, con brillo
        if (active) {
            glowPaint.setStrokeWidth(2 * density);
            glowPaint.setColor(withAlpha(accent, mixing ? 0.45f : 0.3f));
            glowPaint.setShadowLayer(7 * density, 0, 0, withAlpha(accent, mixing ? 0.45f : 0.3f));
            canvas.drawRoundRect(rect, radius, radius, glowPaint);
            borderPaint.setColor(withAlpha(accent, mixing ? 0.65f : 0.45f));
        } else {
            borderPaint.setColor(Color.argb(31, 255, 255, 255));
        }
        canvas.drawRoundRect(rect, radius, radius, borderPaint);

        // Texto de estado
        String text = label();
        float textX = 12 * density;
        float textY = h / 2f - (textPaint.descent() + textPaint.ascent()) / 2f;
        if (active) {
            textPaint.setColor(accent);
            textPaint.setShadowLayer(5 * density, 0, 0, withAlpha(accent, mixing ? 0.75f : 0.5f));
        } else {
            textPaint.setColor(MUTED);
            textPaint.clearShadowLayer();
        }
        canvas.drawText(text, textX, textY, textPaint);

        // Barras de ecualizador (alineadas abajo, 3-14 dp)
        float barW = 3.5f * density;
        float gap = 3 * density;
        float maxH = 14 * density;
        float minH = 3 * density;
        float barsX = w - 12 * density - barsWidth();
        float barsBottom = h / 2f + maxH / 2f;
        float tSec = (SystemClock.uptimeMillis() - startMs) / 1000f;
        if (active) {
            barPaint.setShader(new LinearGradient(0, barsBottom, 0, barsBottom - maxH, accent, accentLight, Shader.TileMode.CLAMP));
            barPaint.setShadowLayer(4 * density, 0, 0, withAlpha(accent, mixing ? 0.7f : 0.6f));
        } else {
            barPaint.setShader(null);
            barPaint.setColor(Color.argb(64, 255, 255, 255));
            barPaint.clearShadowLayer();
        }
        for (int i = 0; i < 5; i++) {
            float barH = minH;
            if (active) {
                // Ida y vuelta (alternate) con suavizado (ease-in-out)
                float t = (tSec - BAR_DELAY_S[i]) / BAR_PERIOD_S[i];
                if (t < 0) t = 0;
                float cycle = t % 2f;
                float linear = cycle <= 1f ? cycle : 2f - cycle;
                float eased = (float) (0.5 - 0.5 * Math.cos(Math.PI * linear));
                barH = minH + (maxH - minH) * eased;
            }
            float left = barsX + i * (barW + gap);
            rect.set(left, barsBottom - barH, left + barW, barsBottom);
            canvas.drawRoundRect(rect, 2 * density, 2 * density, barPaint);
        }

        if (active) postInvalidateDelayed(FRAME_MS);
    }

    private static int withAlpha(int color, float alpha) {
        return Color.argb(Math.round(255 * alpha), Color.red(color), Color.green(color), Color.blue(color));
    }
}
