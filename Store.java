package id.garrypedia.listener;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Pengaturan dan riwayat, disimpan di SharedPreferences. */
final class Store {
    static final class Entry {
        long t;
        String status;   // cocok | tidak_cocok | antre | gagal | diabaikan
        long amount;
        String pkg;
        String note;
    }

    private static final String FILE = "cfg";
    private static final int MAX_LOG = 40;

    private Store() {}

    private static SharedPreferences sp(Context c) {
        return c.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    static String cleanUrl(String u) {
        u = u == null ? "" : u.trim();
        while (u.endsWith("/")) u = u.substring(0, u.length() - 1);
        return u;
    }

    static String url(Context c) {
        String u = sp(c).getString("url", null);
        if (u == null || u.trim().isEmpty()) u = BuildConfig.DEFAULT_URL;
        return cleanUrl(u);
    }

    static String secret(Context c) {
        String s = sp(c).getString("secret", null);
        if (s == null || s.trim().isEmpty()) s = BuildConfig.DEFAULT_SECRET;
        return s == null ? "" : s.trim();
    }

    static boolean allApps(Context c) { return sp(c).getBoolean("all", false); }

    static Set<String> packages(Context c) {
        String s = sp(c).getString("pkgs", null);
        if (s == null) return Known.defaults();
        return new LinkedHashSet<>(Rules.split(s, false));
    }

    static String include(Context c) { return sp(c).getString("inc", Rules.DEF_INCLUDE); }

    static String exclude(Context c) { return sp(c).getString("exc", Rules.DEF_EXCLUDE); }

    static void save(Context c, String url, String secret, boolean all, Set<String> pkgs, String inc, String exc) {
        StringBuilder sb = new StringBuilder();
        for (String p : pkgs) { if (sb.length() > 0) sb.append(','); sb.append(p); }
        sp(c).edit()
                .putString("url", cleanUrl(url))
                .putString("secret", secret == null ? "" : secret.trim())
                .putBoolean("all", all)
                .putString("pkgs", sb.toString())
                .putString("inc", inc == null ? "" : inc.trim())
                .putString("exc", exc == null ? "" : exc.trim())
                .apply();
    }

    static synchronized void log(Context c, String status, long amount, String pkg, String note) {
        try {
            JSONArray old = new JSONArray(sp(c).getString("log", "[]"));
            JSONArray out = new JSONArray();
            JSONObject e = new JSONObject();
            e.put("t", System.currentTimeMillis());
            e.put("s", status);
            e.put("a", amount);
            e.put("p", pkg == null ? "" : pkg);
            e.put("n", note == null ? "" : note);
            out.put(e);
            for (int i = 0; i < old.length() && out.length() < MAX_LOG; i++) out.put(old.get(i));
            sp(c).edit().putString("log", out.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    static synchronized List<Entry> logs(Context c) {
        List<Entry> list = new ArrayList<>();
        try {
            JSONArray a = new JSONArray(sp(c).getString("log", "[]"));
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                Entry e = new Entry();
                e.t = o.optLong("t");
                e.status = o.optString("s");
                e.amount = o.optLong("a");
                e.pkg = o.optString("p");
                e.note = o.optString("n");
                list.add(e);
            }
        } catch (Exception ignored) {
        }
        return list;
    }

    /** Jumlah entri berstatus tertentu sejak pukul 00.00 hari ini. */
    static int countToday(Context c, String status) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();
        int n = 0;
        for (Entry e : logs(c)) if (e.t >= start && e.status.equals(status)) n++;
        return n;
    }

    static synchronized void clearLogs(Context c) { sp(c).edit().remove("log").apply(); }
}
