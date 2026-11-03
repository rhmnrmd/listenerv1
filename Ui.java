package id.garrypedia.listener;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

/** Token desain Garry Pedia (sama dengan m.css di web) dan pembuat komponen. */
final class Ui {
    static final int P = 0xFF6D28D9, P2 = 0xFF8B5CF6, DEEP = 0xFF4C1D95, BG = 0xFFF7F5FD, TINT = 0xFFEFE9FE,
            INK = 0xFF1B1630, MUTE = 0xFF6F6A86, LINE = 0xFFE8E3F6, OK = 0xFF15803D, BAD = 0xFFB91C1C,
            WARN = 0xFFB45309, WHITE = 0xFFFFFFFF;
    static final int[] GRAD = {0xFF5B21B6, 0xFF7C3AED, 0xFF8B5CF6};

    private static Typeface disp, body;

    private Ui() {}

    static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    /** Bricolage Grotesque (judul). Jatuh ke font sistem tebal bila gagal dimuat. */
    static Typeface display(Context c) {
        if (disp == null) {
            try {
                disp = ResourcesCompat.getFont(c.getApplicationContext(), R.font.bricolage_bold);
            } catch (Throwable ignored) {
            }
            if (disp == null) disp = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
        }
        return disp;
    }

    /** Inter bila file res/font/inter.ttf ditambahkan, selain itu font sistem (sama seperti fallback di web). */
    static Typeface body(Context c) {
        if (body == null) {
            int id = c.getResources().getIdentifier("inter", "font", c.getPackageName());
            if (id != 0) {
                try {
                    body = ResourcesCompat.getFont(c.getApplicationContext(), id);
                } catch (Throwable ignored) {
                }
            }
            if (body == null) body = Typeface.DEFAULT;
        }
        return body;
    }

    static GradientDrawable shape(Context c, int fill, int stroke, float radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(c, radiusDp));
        if (stroke != 0) g.setStroke(Math.max(1, dp(c, 1)), stroke);
        return g;
    }

    static GradientDrawable gradient(Context c, float radiusDp) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR, GRAD);
        g.setCornerRadius(dp(c, radiusDp));
        return g;
    }

    static LinearLayout.LayoutParams lp(Context c, int w, int h, float l, float t, float r, float b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h);
        p.setMargins(dp(c, l), dp(c, t), dp(c, r), dp(c, b));
        return p;
    }

    static TextView text(Context c, String s, float sp, int color, boolean useDisplay, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (useDisplay) t.setTypeface(display(c));
        else t.setTypeface(body(c), bold ? Typeface.BOLD : Typeface.NORMAL);
        return t;
    }

    static TextView label(Context c, String s) {
        TextView t = text(c, s, 14, INK, false, true);
        t.setLayoutParams(lp(c, -1, -2, 0, 16, 0, 6));
        return t;
    }

    static TextView tag(Context c, String s, int bg, int fg) {
        TextView t = text(c, s, 12, fg, false, true);
        t.setPadding(dp(c, 9), dp(c, 2), dp(c, 9), dp(c, 2));
        t.setBackground(shape(c, bg, 0, 99));
        return t;
    }

    /** Panel putih berbingkai tipis, seperti .panel di web. */
    static LinearLayout panel(Context c, String title, String subtitle) {
        LinearLayout p = new LinearLayout(c);
        p.setOrientation(LinearLayout.VERTICAL);
        p.setBackground(shape(c, WHITE, LINE, 26));
        p.setPadding(dp(c, 22), dp(c, 22), dp(c, 22), dp(c, 22));
        p.setLayoutParams(lp(c, -1, -2, 0, 0, 0, 18));
        TextView h = text(c, title, 24, INK, true, false);
        p.addView(h);
        if (subtitle != null) {
            TextView s = text(c, subtitle, 14, MUTE, false, false);
            s.setLayoutParams(lp(c, -1, -2, 0, 4, 0, 0));
            p.addView(s);
        }
        return p;
    }

    static Button button(Context c, String label, boolean primary) {
        Button b = new Button(c);
        b.setText(label);
        b.setAllCaps(false);
        b.setStateListAnimator(null);
        b.setMinHeight(0);
        b.setMinimumHeight(0);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setGravity(Gravity.CENTER);
        b.setTextSize(primary ? 17 : 15);
        b.setTypeface(body(c), Typeface.BOLD);
        b.setTextColor(primary ? WHITE : P);
        int v = dp(c, primary ? 16 : 12);
        b.setPadding(dp(c, 18), v, dp(c, 18), v);
        android.graphics.drawable.Drawable bg = primary ? gradient(c, 14) : shape(c, TINT, 0, 12);
        b.setBackground(new RippleDrawable(ColorStateList.valueOf(primary ? 0x33FFFFFF : 0x226D28D9), bg, null));
        return b;
    }

    static EditText input(Context c, String hint, int inputType, boolean multiline) {
        EditText e = new EditText(c);
        e.setHint(hint);
        e.setTextSize(16);
        e.setTextColor(INK);
        e.setHintTextColor(0xFF9A95B0);
        e.setTypeface(body(c));
        if (multiline) {
            e.setMinLines(2);
            e.setGravity(Gravity.TOP | Gravity.START);
            e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        } else {
            e.setSingleLine(true);
            e.setInputType(inputType);
        }
        e.setBackground(shape(c, BG, LINE, 14));
        e.setPadding(dp(c, 14), dp(c, 14), dp(c, 14), dp(c, 14));
        return e;
    }
}
