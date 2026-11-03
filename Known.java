package id.garrypedia.listener;

import java.util.LinkedHashSet;
import java.util.Set;

/** Aplikasi dompet/bank yang umum. Aplikasi lain bisa ditambah lewat kolom "Paket tambahan". */
final class Known {
    static final String[][] APPS = {
            {"id.dana", "DANA"},
            {"com.gojek.app", "Gojek / GoPay"},
            {"com.gojek.resto", "GoBiz (merchant GoPay)"},
            {"ovo.id", "OVO"},
            {"com.shopee.id", "Shopee / ShopeePay"},
            {"com.telkom.mwallet", "LinkAja"},
            {"com.bca", "BCA mobile"},
            {"id.co.bri.brimo", "BRImo"},
            {"id.bmri.livin", "Livin' by Mandiri"},
            {"src.com.bni", "BNI Mobile"},
    };

    private Known() {}

    static String label(String pkg) {
        for (String[] a : APPS) if (a[0].equals(pkg)) return a[1];
        return pkg;
    }

    static boolean isKnown(String pkg) {
        for (String[] a : APPS) if (a[0].equals(pkg)) return true;
        return false;
    }

    static Set<String> defaults() {
        Set<String> s = new LinkedHashSet<>();
        for (String[] a : APPS) s.add(a[0]);
        return s;
    }
}
