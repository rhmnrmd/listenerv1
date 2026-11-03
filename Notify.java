package id.garrypedia.listener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Klien HTTP murni untuk POST {server}/api/notify (header x-secret, isi teks biasa). */
final class Notify {
    static final class Res {
        int code;        // 0 bila tidak ada balasan
        String body = "";
        String err;      // pesan kesalahan jaringan atau balasan tak terbaca
        boolean matched;
        String orderId = "";
        long nominal = -1;
    }

    private static final Pattern P_MATCHED = Pattern.compile("\"matched\"\\s*:\\s*true");
    private static final Pattern P_ID = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern P_NOM = Pattern.compile("\"nominal\"\\s*:\\s*(\\d+)");

    private Notify() {}

    static Res post(String base, String secret, String text) {
        Res r = new Res();
        if (base == null || base.isEmpty()) { r.err = "URL server belum diisi"; return r; }
        if (secret == null || secret.isEmpty()) { r.err = "Secret belum diisi"; return r; }
        HttpURLConnection h = null;
        try {
            h = (HttpURLConnection) new URL(base + "/api/notify").openConnection();
            h.setRequestMethod("POST");
            h.setConnectTimeout(10_000);
            h.setReadTimeout(15_000);
            h.setDoOutput(true);
            h.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            h.setRequestProperty("x-secret", secret);
            byte[] b = text.getBytes(StandardCharsets.UTF_8);
            h.setFixedLengthStreamingMode(b.length);
            try (OutputStream o = h.getOutputStream()) { o.write(b); }
            r.code = h.getResponseCode();
            InputStream in = r.code >= 400 ? h.getErrorStream() : h.getInputStream();
            r.body = read(in);
            if (r.code == 200) {
                r.matched = P_MATCHED.matcher(r.body).find();
                Matcher mi = P_ID.matcher(r.body);
                if (mi.find()) r.orderId = mi.group(1);
                Matcher mn = P_NOM.matcher(r.body);
                if (mn.find()) r.nominal = Long.parseLong(mn.group(1));
                if (!r.body.contains("\"matched\"")) r.err = "Balasan server tidak dikenali";
            }
        } catch (Exception e) {
            r.err = e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage());
        } finally {
            if (h != null) h.disconnect();
        }
        return r;
    }

    private static String read(InputStream in) throws Exception {
        if (in == null) return "";
        try (InputStream i = in) {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            byte[] buf = new byte[2048];
            int n, total = 0;
            while ((n = i.read(buf)) > 0 && total < 65_536) { bo.write(buf, 0, n); total += n; }
            return bo.toString("UTF-8");
        }
    }
}
