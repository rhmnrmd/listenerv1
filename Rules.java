package id.garrypedia.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Logika murni (tanpa Android): membaca nominal, menyaring notifikasi, dan membuang duplikat. */
final class Rules {
    static final String DEF_INCLUDE = "terima, diterima, menerima, masuk, received, pembayaran, payment, berhasil";
    static final String DEF_EXCLUDE = "keluar, dikirim, kirim ke, transfer ke, pembayaran ke, bayar ke, pembelian, "
            + "top up, topup, isi saldo, tarik, promo, cashback, voucher, diskon, gagal, batal, kedaluwarsa, expired";

    enum Decision { NO_AMOUNT, WRONG_APP, FILTERED, SEND }

    // Sama dengan server: /Rp\.?\s?([\d.,]+)/i  (spasi biasa, NBSP, dan narrow NBSP dianggap spasi)
    private static final Pattern RP = Pattern.compile("Rp\\.?[\\s\\u00A0\\u202F]?([\\d.,]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DEC = Pattern.compile("[.,]\\d{2}$");
    private static final long DUP_MS = 45_000L;
    private static final Map<String, Long> seen = new HashMap<>();

    private Rules() {}

    /** Nominal rupiah pertama pada teks, atau -1 jika tidak ada. Aturan pembacaannya sama dengan server. */
    static long parseAmount(String text) {
        if (text == null) return -1;
        Matcher m = RP.matcher(text);
        if (!m.find()) return -1;
        String n = DEC.matcher(m.group(1)).replaceFirst("").replaceAll("[.,]", "");
        if (n.isEmpty() || n.length() > 15) return -1;
        try { return Long.parseLong(n); } catch (NumberFormatException e) { return -1; }
    }

    static List<String> split(String csv, boolean lower) {
        List<String> out = new ArrayList<>();
        if (csv == null) return out;
        for (String s : csv.split("[,;\\n]")) {
            String t = s.trim();
            if (lower) t = t.toLowerCase();
            if (!t.isEmpty() && !out.contains(t)) out.add(t);
        }
        return out;
    }

    static boolean containsAny(String lowerText, List<String> words) {
        for (String w : words) if (lowerText.contains(w)) return true;
        return false;
    }

    /**
     * Bila ada beberapa baris, pilih baris yang memuat nominal DAN kata kunci masuk,
     * supaya "Saldo Rp..." di baris lain tidak terbaca sebagai nominal pembayaran.
     */
    static String pickText(String full, List<String> include) {
        if (full == null) return "";
        String[] lines = full.split("\\n");
        if (lines.length > 1) {
            for (String line : lines) {
                if (parseAmount(line) > 0 && (include.isEmpty() || containsAny(line.toLowerCase(), include))) return line.trim();
            }
        }
        return full.trim();
    }

    static Decision decide(String pkg, String text, boolean allApps, Set<String> apps, List<String> include, List<String> exclude) {
        if (parseAmount(text) <= 0) return Decision.NO_AMOUNT;
        if (!allApps && !apps.contains(pkg)) return Decision.WRONG_APP;
        String lower = text.toLowerCase();
        if (containsAny(lower, exclude)) return Decision.FILTERED;
        if (!include.isEmpty() && !containsAny(lower, include)) return Decision.FILTERED;
        return Decision.SEND;
    }

    /** True bila kunci yang sama sudah terlihat dalam 45 detik terakhir (aplikasi sering memposting ulang notifikasi). */
    static synchronized boolean isDuplicate(String key, long now) {
        seen.values().removeIf(t -> now - t > DUP_MS);
        if (seen.containsKey(key)) return true;
        seen.put(key, now);
        return false;
    }
}
