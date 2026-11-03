package id.garrypedia.listener;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final int GREEN = 0xFF10B981, AMBER = 0xFFF59E0B;

    private TextView heroNum, heroPill;
    private LinearLayout statusBox, logBox;
    private EditText fUrl, fSecret, fInc, fExc, fCustom;
    private SwitchCompat swAll;
    private TextView msgServer, msgSave;
    private final List<AppCompatCheckBox> boxes = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            refresh();
            handler.postDelayed(this, 4000);
        }
    };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(build());
        loadFields();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(tick);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(tick);
    }

    // ---------------------------------------------------------------- tampilan

    private View build() {
        final Context c = this;
        ScrollView sv = new ScrollView(c);
        sv.setBackgroundColor(Ui.WHITE);
        sv.setFillViewport(true);
        LinearLayout root = new LinearLayout(c);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(c, 18), Ui.dp(c, 14), Ui.dp(c, 18), Ui.dp(c, 34));
        sv.addView(root);

        // Bilah atas: logo, nama merek, penanda "Listener"
        LinearLayout top = new LinearLayout(c);
        top.setGravity(Gravity.CENTER_VERTICAL);
        ImageView logo = new ImageView(c);
        logo.setImageResource(R.drawable.ic_logo);
        logo.setContentDescription("Garry Pedia");
        top.addView(logo, new LinearLayout.LayoutParams(Ui.dp(c, 34), Ui.dp(c, 34)));
        TextView brand = Ui.text(c, "Garry Pedia", 21, Ui.P, true, false);
        brand.setLayoutParams(Ui.lp(c, -2, -2, 9, 0, 0, 0));
        top.addView(brand);
        View sp = new View(c);
        top.addView(sp, new LinearLayout.LayoutParams(0, 1, 1f));
        top.addView(Ui.tag(c, "Listener", Ui.TINT, Ui.P));
        root.addView(top, Ui.lp(c, -1, -2, 0, 0, 0, 16));

        // Kartu utama
        HeroCard hero = new HeroCard(c);
        hero.setPadding(Ui.dp(c, 24), Ui.dp(c, 24), Ui.dp(c, 24), Ui.dp(c, 26));
        TextView small = Ui.text(c, "Order terkonfirmasi hari ini", 16, 0xE6FFFFFF, false, false);
        hero.addView(small);
        heroNum = Ui.text(c, "0", 56, Ui.WHITE, true, false);
        heroNum.setLayoutParams(Ui.lp(c, -2, -2, 0, 2, 0, 12));
        hero.addView(heroNum);
        heroPill = Ui.text(c, "", 15, Ui.INK, false, true);
        heroPill.setPadding(Ui.dp(c, 14), Ui.dp(c, 8), Ui.dp(c, 14), Ui.dp(c, 8));
        heroPill.setBackground(Ui.shape(c, Ui.WHITE, 0, 12));
        heroPill.setCompoundDrawablePadding(Ui.dp(c, 8));
        hero.addView(heroPill, new LinearLayout.LayoutParams(-2, -2));
        root.addView(hero, Ui.lp(c, -1, -2, 0, 0, 0, 18));

        // Persiapan
        LinearLayout prep = Ui.panel(c, "Persiapan", "Tiga hal agar pembayaran terbaca otomatis.");
        statusBox = new LinearLayout(c);
        statusBox.setOrientation(LinearLayout.VERTICAL);
        prep.addView(statusBox, Ui.lp(c, -1, -2, 0, 8, 0, 0));
        TextView bPerm = Ui.button(c, "Buka izin notifikasi", true);
        bPerm.setOnClickListener(v -> openSettings(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        prep.addView(bPerm, Ui.lp(c, -1, -2, 0, 12, 0, 0));
        TextView bBat = Ui.button(c, "Atur hemat baterai", false);
        bBat.setOnClickListener(v -> openSettings(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
        prep.addView(bBat, Ui.lp(c, -1, -2, 0, 10, 0, 0));
        TextView bInfo = Ui.button(c, "Izin tidak bisa dicentang? Buka info aplikasi", false);
        bInfo.setOnClickListener(v -> startActivitySafe(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getPackageName()))));
        prep.addView(bInfo, Ui.lp(c, -1, -2, 0, 10, 0, 0));
        TextView hint = Ui.text(c, "Android 13 ke atas: di info aplikasi, buka menu titik tiga lalu pilih Izinkan setelan terbatas.", 13, Ui.MUTE, false, false);
        hint.setLayoutParams(Ui.lp(c, -1, -2, 0, 10, 0, 0));
        prep.addView(hint);
        root.addView(prep);

        // Server
        LinearLayout server = Ui.panel(c, "Server", "Alamat gateway dan secret dari dashboard admin.");
        server.addView(Ui.label(c, "Alamat server"));
        fUrl = Ui.input(c, "https://domainmu.com", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI, false);
        server.addView(fUrl);
        server.addView(Ui.label(c, "Secret notifikasi"));
        fSecret = Ui.input(c, "Salin dari dashboard admin", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD, false);
        server.addView(fSecret);
        msgServer = Ui.text(c, "", 14, Ui.MUTE, false, false);
        msgServer.setLayoutParams(Ui.lp(c, -1, -2, 0, 10, 0, 0));
        server.addView(msgServer);
        LinearLayout row = new LinearLayout(c);
        TextView bSave1 = Ui.button(c, "Simpan", true);
        bSave1.setOnClickListener(v -> save(msgServer));
        row.addView(bSave1, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView bTest = Ui.button(c, "Tes koneksi", false);
        bTest.setOnClickListener(v -> testConnection());
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -1, 1f);
        tp.setMargins(Ui.dp(c, 10), 0, 0, 0);
        row.addView(bTest, tp);
        server.addView(row, Ui.lp(c, -1, -2, 0, 8, 0, 0));
        root.addView(server);

        // Aplikasi yang dipantau
        LinearLayout apps = Ui.panel(c, "Aplikasi dipantau", "Notifikasi dari aplikasi yang dicentang akan dibaca.");
        swAll = new SwitchCompat(c);
        swAll.setText("Pantau semua aplikasi");
        swAll.setTextSize(15);
        swAll.setTextColor(Ui.INK);
        swAll.setTypeface(Ui.body(c), android.graphics.Typeface.BOLD);
        apps.addView(swAll, Ui.lp(c, -1, -2, 0, 12, 0, 4));
        for (String[] a : Known.APPS) {
            AppCompatCheckBox cb = new AppCompatCheckBox(c);
            cb.setText(a[1]);
            cb.setTag(a[0]);
            cb.setTextSize(15);
            cb.setTextColor(Ui.INK);
            cb.setTypeface(Ui.body(c));
            boxes.add(cb);
            apps.addView(cb, Ui.lp(c, -1, -2, 0, 0, 0, 0));
        }
        apps.addView(Ui.label(c, "Paket tambahan (pisahkan dengan koma)"));
        fCustom = Ui.input(c, "contoh: com.aplikasi.merchant", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI, false);
        apps.addView(fCustom);
        TextView ah = Ui.text(c, "Aplikasi yang belum dipilih tapi mengirim notifikasi bernominal akan muncul di Riwayat sebagai Diabaikan. Ketuk barisnya untuk menambahkannya.", 13, Ui.MUTE, false, false);
        ah.setLayoutParams(Ui.lp(c, -1, -2, 0, 8, 0, 0));
        apps.addView(ah);
        root.addView(apps);

        // Filter
        LinearLayout filt = Ui.panel(c, "Filter notifikasi", "Hanya pembayaran masuk yang dikirim. Pisahkan kata dengan koma.");
        filt.addView(Ui.label(c, "Kirim jika memuat salah satu"));
        fInc = Ui.input(c, "terima, masuk, ...", 0, true);
        filt.addView(fInc);
        filt.addView(Ui.label(c, "Abaikan jika memuat salah satu"));
        fExc = Ui.input(c, "keluar, transfer ke, ...", 0, true);
        filt.addView(fExc);
        TextView bReset = Ui.button(c, "Kembalikan kata kunci bawaan", false);
        bReset.setOnClickListener(v -> {
            fInc.setText(Rules.DEF_INCLUDE);
            fExc.setText(Rules.DEF_EXCLUDE);
        });
        filt.addView(bReset, Ui.lp(c, -1, -2, 0, 14, 0, 0));
        msgSave = Ui.text(c, "", 14, Ui.MUTE, false, false);
        msgSave.setLayoutParams(Ui.lp(c, -1, -2, 0, 12, 0, 0));
        filt.addView(msgSave);
        TextView bSave2 = Ui.button(c, "Simpan pengaturan", true);
        bSave2.setOnClickListener(v -> save(msgSave));
        filt.addView(bSave2, Ui.lp(c, -1, -2, 0, 6, 0, 0));
        root.addView(filt);

        // Riwayat
        LinearLayout hist = Ui.panel(c, "Riwayat", "40 kejadian terakhir di perangkat ini.");
        logBox = new LinearLayout(c);
        logBox.setOrientation(LinearLayout.VERTICAL);
        hist.addView(logBox, Ui.lp(c, -1, -2, 0, 8, 0, 0));
        TextView bClear = Ui.button(c, "Hapus riwayat", false);
        bClear.setOnClickListener(v -> {
            Store.clearLogs(c);
            refresh();
        });
        hist.addView(bClear, Ui.lp(c, -1, -2, 0, 12, 0, 0));
        root.addView(hist);

        TextView foot = Ui.text(c, "© Garry Pedia", 14, Ui.MUTE, false, false);
        foot.setGravity(Gravity.CENTER);
        root.addView(foot, Ui.lp(c, -1, -2, 0, 4, 0, 0));
        return sv;
    }

    private void loadFields() {
        fUrl.setText(Store.url(this));
        fSecret.setText(Store.secret(this));
        swAll.setChecked(Store.allApps(this));
        Set<String> sel = Store.packages(this);
        for (AppCompatCheckBox cb : boxes) cb.setChecked(sel.contains((String) cb.getTag()));
        StringBuilder extra = new StringBuilder();
        for (String p : sel) {
            if (!Known.isKnown(p)) {
                if (extra.length() > 0) extra.append(", ");
                extra.append(p);
            }
        }
        fCustom.setText(extra.toString());
        fInc.setText(Store.include(this));
        fExc.setText(Store.exclude(this));
    }

    // ---------------------------------------------------------------- aksi

    private void save(TextView msg) {
        String url = Store.cleanUrl(fUrl.getText().toString());
        if (url.isEmpty() || !url.startsWith("https://")) {
            say(msg, "Alamat server harus diawali https://", Ui.BAD);
            return;
        }
        Set<String> pkgs = new LinkedHashSet<>();
        for (AppCompatCheckBox cb : boxes) if (cb.isChecked()) pkgs.add((String) cb.getTag());
        pkgs.addAll(Rules.split(fCustom.getText().toString(), false));
        Store.save(this, url, fSecret.getText().toString(), swAll.isChecked(), pkgs,
                fInc.getText().toString(), fExc.getText().toString());
        say(msg, "Tersimpan.", Ui.OK);
        refresh();
    }

    private void testConnection() {
        final String url = Store.cleanUrl(fUrl.getText().toString());
        final String secret = fSecret.getText().toString().trim();
        if (url.isEmpty() || !url.startsWith("https://")) {
            say(msgServer, "Alamat server harus diawali https://", Ui.BAD);
            return;
        }
        say(msgServer, "Menghubungi server...", Ui.MUTE);
        new Thread(() -> {
            final Notify.Res r = Notify.post(url, secret, "Tes koneksi Garry Pedia Rp 0");
            runOnUiThread(() -> {
                if (r.code == 200 && r.err == null) say(msgServer, "Tersambung. Secret benar.", Ui.OK);
                else if (r.code == 401) say(msgServer, "Secret salah. Salin ulang dari dashboard admin.", Ui.BAD);
                else if (r.err != null) say(msgServer, "Gagal: " + r.err, Ui.BAD);
                else say(msgServer, "Server menjawab " + r.code, Ui.BAD);
            });
        }).start();
    }

    private void openSettings(String action) {
        startActivitySafe(new Intent(action));
    }

    private void startActivitySafe(Intent i) {
        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Halaman pengaturan tidak ditemukan di perangkat ini", Toast.LENGTH_LONG).show();
        }
    }

    private static void say(TextView t, String s, int color) {
        t.setText(s);
        t.setTextColor(color);
    }

    // ---------------------------------------------------------------- penyegaran

    private boolean listenerEnabled() {
        return NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName());
    }

    private boolean batteryOk() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return pm != null && pm.isIgnoringBatteryOptimizations(getPackageName());
    }

    private void refresh() {
        boolean perm = listenerEnabled();
        boolean serverOk = !Store.url(this).isEmpty() && !Store.secret(this).isEmpty();
        heroNum.setText(String.valueOf(Store.countToday(this, "cocok")));

        int dot;
        String label;
        if (!perm) { dot = AMBER; label = "Izin belum aktif"; }
        else if (!serverOk) { dot = AMBER; label = "Secret belum diisi"; }
        else { dot = GREEN; label = "Listener aktif"; }
        heroPill.setText(label);
        android.graphics.drawable.GradientDrawable d = new android.graphics.drawable.GradientDrawable();
        d.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        d.setColor(dot);
        d.setSize(Ui.dp(this, 9), Ui.dp(this, 9));
        heroPill.setCompoundDrawablesRelativeWithIntrinsicBounds(d, null, null, null);

        statusBox.removeAllViews();
        statusBox.addView(statusRow("Izin akses notifikasi", perm ? "Aktif" : "Belum diberikan", perm));
        statusBox.addView(statusRow("Server dan secret", serverOk ? "Terisi" : "Secret belum diisi", serverOk));
        boolean bat = batteryOk();
        statusBox.addView(statusRow("Hemat baterai", bat ? "Dikecualikan" : "Disarankan dikecualikan agar tidak dimatikan sistem", bat));

        renderLogs();
    }

    private View statusRow(String title, String note, boolean ok) {
        Context c = this;
        LinearLayout r = new LinearLayout(c);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(0, Ui.dp(c, 8), 0, Ui.dp(c, 8));
        View dot = new View(c);
        dot.setBackground(Ui.shape(c, ok ? GREEN : AMBER, 0, 99));
        r.addView(dot, Ui.lp(c, Ui.dp(c, 10), Ui.dp(c, 10), 0, 0, 12, 0));
        LinearLayout col = new LinearLayout(c);
        col.setOrientation(LinearLayout.VERTICAL);
        TextView t = Ui.text(c, title, 15, Ui.INK, false, true);
        TextView n = Ui.text(c, note, 13, ok ? Ui.OK : Ui.WARN, false, false);
        col.addView(t);
        col.addView(n);
        r.addView(col, new LinearLayout.LayoutParams(0, -2, 1f));
        return r;
    }

    private void renderLogs() {
        Context c = this;
        logBox.removeAllViews();
        List<Store.Entry> list = Store.logs(c);
        if (list.isEmpty()) {
            logBox.addView(Ui.text(c, "Belum ada kejadian. Notifikasi pembayaran yang terbaca akan muncul di sini.", 14, Ui.MUTE, false, false));
            return;
        }
        SimpleDateFormat f = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
        for (int i = 0; i < list.size(); i++) {
            final Store.Entry e = list.get(i);
            LinearLayout item = new LinearLayout(c);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setPadding(0, Ui.dp(c, 12), 0, Ui.dp(c, 12));
            LinearLayout col = new LinearLayout(c);
            col.setOrientation(LinearLayout.VERTICAL);
            col.addView(Ui.text(c, rupiah(e.amount), 16, Ui.INK, false, true));
            boolean addable = e.note != null && e.note.startsWith("Aplikasi belum");
            String sub = Known.label(e.pkg) + " \u00B7 " + f.format(new Date(e.t)) + (e.note == null || e.note.isEmpty() ? "" : " \u00B7 " + e.note)
                    + (addable ? " \u00B7 ketuk untuk menambah" : "");
            col.addView(Ui.text(c, sub, 13, Ui.MUTE, false, false));
            item.addView(col, new LinearLayout.LayoutParams(0, -2, 1f));
            item.addView(statusTag(e.status));
            if (addable) {
                item.setOnClickListener(v -> {
                    String cur = fCustom.getText().toString().trim();
                    if (!Rules.split(cur, false).contains(e.pkg)) {
                        fCustom.setText(cur.isEmpty() ? e.pkg : cur + ", " + e.pkg);
                    }
                    Toast.makeText(this, "Ditambahkan ke Paket tambahan. Tekan Simpan pengaturan.", Toast.LENGTH_LONG).show();
                });
            }
            logBox.addView(item);
            if (i < list.size() - 1) {
                View line = new View(c);
                line.setBackgroundColor(Ui.LINE);
                logBox.addView(line, new LinearLayout.LayoutParams(-1, Math.max(1, Ui.dp(c, 1))));
            }
        }
    }

    private TextView statusTag(String s) {
        Context c = this;
        switch (s) {
            case "cocok": return Ui.tag(c, "Cocok", 0xFFDCFCE7, Ui.OK);
            case "antre": return Ui.tag(c, "Antre", 0xFFFEF3C7, Ui.WARN);
            case "gagal": return Ui.tag(c, "Gagal", 0xFFFEE2E2, Ui.BAD);
            case "tidak_cocok": return Ui.tag(c, "Tidak cocok", Ui.TINT, Ui.P);
            default: return Ui.tag(c, "Diabaikan", Ui.TINT, Ui.MUTE);
        }
    }

    private static String rupiah(long v) {
        return String.format(Locale.forLanguageTag("id-ID"), "Rp%,d", v);
    }
}
