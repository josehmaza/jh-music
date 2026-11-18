package com.jh.caraudio;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;

/**
 * Fondo de toda la app con el color dinámico de la carátula (tanto en Tema Oscuro como en Tema Claro):
 * - En Dark Mode: base #07090C con degradado profundo del color de la carátula (azul petróleo sin carátula).
 * - En Light Mode: base gris hielo #E8EEF6 con degradado pastel suave del color de la carátula
 *   (azul cielo pastel sin carátula; turquesa pastel durante Automix).
 */
final class AmbientTheme {

    static final int DEFAULT_FADE_MS = 1200;
    private static final long FRAME_MS = 50; // ~20 fps

    private static final float[] THEME_DEFAULT_DARK = {23, 60, 92};
    private static final float[] GLOW_DEFAULT_DARK = {56, 189, 248};
    private static final int BASE_DARK = Color.rgb(7, 9, 12);

    private static final float[] THEME_DEFAULT_LIGHT = {186, 214, 248};  // bruma azul hielo ultra sutil
    private static final float[] GLOW_DEFAULT_LIGHT = {37, 99, 235};     // azul cobalto suave
    private static final float[] MIX_LIGHT_RGB = {196, 181, 253};        // bruma violeta lavanda Automix
    private static final float[] MIX_GLOW_LIGHT = {124, 58, 237};        // violeta glow Automix
    private static final int BASE_LIGHT = Color.rgb(236, 241, 248);      // #ECF1F8 porcelana hielo

    private final View root;
    private final AlbumGlowView glow;
    private final Handler handler;
    private final ColorDrawable baseLayer = new ColorDrawable(BASE_DARK);
    private final GradientDrawable linear = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{0, 0, 0});
    private final GradientDrawable radial = new GradientDrawable();

    private boolean lightMode = false;
    private boolean mixing = false;
    private float[] lastCoverRgb = null;

    private final float[] theme = THEME_DEFAULT_DARK.clone();
    private final float[] glowRgb = GLOW_DEFAULT_DARK.clone();
    private float[] fromTheme, toTheme, fromGlow, toGlow;
    private long startMs;
    private int durationMs;
    private int frame;

    private final Runnable stepRunnable = new Runnable() {
        @Override
        public void run() {
            step();
        }
    };

    AmbientTheme(View root, AlbumGlowView glow, Handler handler) {
        this.root = root;
        this.glow = glow;
        this.handler = handler;
        radial.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        radial.setGradientCenter(0.78f, 0.08f);
        Drawable[] layers = {baseLayer, linear, radial};
        root.setBackground(new LayerDrawable(layers));
        root.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> {
            if (r - l != or - ol) render(true);
        });
        render(true);
    }

    void setLightMode(boolean light) {
        if (this.lightMode == light) return;
        this.lightMode = light;
        baseLayer.setColor(light ? BASE_LIGHT : BASE_DARK);
        if (glow != null) glow.setLightMode(light);
        applyTargets(320);
    }

    void setMixing(boolean mixing) {
        if (this.mixing == mixing) return;
        this.mixing = mixing;
        if (lightMode) {
            applyTargets(350);
        }
    }

    /** Nueva canción: color dinámico de su carátula (o el por defecto si no tiene). */
    void applyCover(Bitmap art, int fadeMs) {
        lastCoverRgb = art != null ? coverColor(art) : null;
        applyTargets(Math.max(200, fadeMs));
    }

    private void applyTargets(int fadeMs) {
        float[] target;
        float[] targetGlow;

        if (lightMode && mixing) {
            target = MIX_LIGHT_RGB;
            targetGlow = MIX_GLOW_LIGHT;
        } else if (lastCoverRgb != null) {
            target = lightMode ? toLightThemeColor(lastCoverRgb) : toDarkThemeColor(lastCoverRgb);
            targetGlow = lightMode ? toLightGlowColor(lastCoverRgb) : toDarkGlowColor(lastCoverRgb);
        } else {
            target = lightMode ? THEME_DEFAULT_LIGHT : THEME_DEFAULT_DARK;
            targetGlow = lightMode ? GLOW_DEFAULT_LIGHT : GLOW_DEFAULT_DARK;
        }

        fromTheme = theme.clone();
        fromGlow = glowRgb.clone();
        toTheme = target;
        toGlow = targetGlow;
        durationMs = Math.max(180, fadeMs);
        startMs = SystemClock.uptimeMillis();
        frame = 0;
        handler.removeCallbacks(stepRunnable);
        handler.post(stepRunnable);
    }

    private void step() {
        float t = Math.min(1f, (SystemClock.uptimeMillis() - startMs) / (float) durationMs);
        float e = (float) (0.5 - 0.5 * Math.cos(Math.PI * t));
        for (int i = 0; i < 3; i++) {
            theme[i] = fromTheme[i] + (toTheme[i] - fromTheme[i]) * e;
            glowRgb[i] = fromGlow[i] + (toGlow[i] - fromGlow[i]) * e;
        }
        frame++;
        render(t >= 1f || frame == 12);
        if (t < 1f) handler.postDelayed(stepRunnable, FRAME_MS);
    }

    private void render(boolean includeGlow) {
        int r = clampByte(theme[0]), g = clampByte(theme[1]), b = clampByte(theme[2]);
        int base = lightMode ? BASE_LIGHT : BASE_DARK;
        int topA = lightMode ? 48 : 128;
        int midA = lightMode ? 16 : 46;
        int radA = lightMode ? 88 : 204;

        linear.setColors(new int[]{Color.argb(topA, r, g, b), Color.argb(midA, r, g, b), base});
        radial.setColors(new int[]{Color.argb(radA, r, g, b), Color.argb(0, r, g, b)});
        int width = root.getWidth() > 0 ? root.getWidth() : 1024;
        radial.setGradientRadius(width * 0.56f);
        root.invalidate();
        if (includeGlow && glow != null) {
            glow.setGlowColor(Color.rgb(clampByte(glowRgb[0]), clampByte(glowRgb[1]), clampByte(glowRgb[2])));
        }
    }

    private static int clampByte(float v) {
        return Math.max(0, Math.min(255, Math.round(v)));
    }

    private static float[] coverColor(Bitmap art) {
        try {
            Bitmap small = Bitmap.createScaledBitmap(art, 24, 24, true);
            int[] px = new int[24 * 24];
            small.getPixels(px, 0, 24, 0, 0, 24, 24);
            if (small != art) small.recycle();
            double r = 0, g = 0, b = 0, total = 0;
            for (int c : px) {
                int R = Color.red(c), G = Color.green(c), B = Color.blue(c);
                int max = Math.max(R, Math.max(G, B)), min = Math.min(R, Math.min(G, B));
                double sat = max == 0 ? 0 : (max - min) / (double) max;
                double weight = 0.08 + sat * sat * (max / 255.0);
                r += R * weight;
                g += G * weight;
                b += B * weight;
                total += weight;
            }
            if (total <= 0) return null;
            return new float[]{(float) (r / total), (float) (g / total), (float) (b / total)};
        } catch (Exception e) {
            return null;
        }
    }

    private static float[] toDarkThemeColor(float[] rgb) {
        float[] hsl = rgbToHsl(rgb[0], rgb[1], rgb[2]);
        float sat = hsl[1] < 0.08f ? hsl[1] : Math.max(0.35f, Math.min(0.75f, hsl[1]));
        float light = Math.max(0.22f, Math.min(0.36f, hsl[2]));
        return hslToRgb(hsl[0], sat, light);
    }

    private static float[] toDarkGlowColor(float[] rgb) {
        float[] hsl = rgbToHsl(rgb[0], rgb[1], rgb[2]);
        if (hsl[1] < 0.08f) return new float[]{200, 210, 225};
        return hslToRgb(hsl[0], Math.max(0.6f, hsl[1]), 0.58f);
    }

    /** Pastel aéreo luminoso para fondo claro (texto grafito siempre legible). */
    private static float[] toLightThemeColor(float[] rgb) {
        float[] hsl = rgbToHsl(rgb[0], rgb[1], rgb[2]);
        float sat = hsl[1] < 0.08f ? hsl[1] : Math.max(0.45f, Math.min(0.82f, hsl[1]));
        return hslToRgb(hsl[0], sat, 0.78f);
    }

    private static float[] toLightGlowColor(float[] rgb) {
        float[] hsl = rgbToHsl(rgb[0], rgb[1], rgb[2]);
        if (hsl[1] < 0.08f) return new float[]{100, 116, 139};
        return hslToRgb(hsl[0], Math.max(0.55f, hsl[1]), 0.52f);
    }

    private static float[] rgbToHsl(float r, float g, float b) {
        r /= 255f;
        g /= 255f;
        b /= 255f;
        float max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b));
        float l = (max + min) / 2f;
        if (max == min) return new float[]{0, 0, l};
        float d = max - min;
        float s = l > 0.5f ? d / (2 - max - min) : d / (max + min);
        float h;
        if (max == r) h = (g - b) / d + (g < b ? 6 : 0);
        else if (max == g) h = (b - r) / d + 2;
        else h = (r - g) / d + 4;
        return new float[]{h / 6f, s, l};
    }

    private static float[] hslToRgb(float h, float s, float l) {
        if (s == 0) return new float[]{l * 255, l * 255, l * 255};
        float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
        float p = 2 * l - q;
        return new float[]{hue(p, q, h + 1f / 3) * 255, hue(p, q, h) * 255, hue(p, q, h - 1f / 3) * 255};
    }

    private static float hue(float p, float q, float t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1f / 6) return p + (q - p) * 6 * t;
        if (t < 1f / 2) return q;
        if (t < 2f / 3) return p + (q - p) * (2f / 3 - t) * 6;
        return p;
    }
}
