package id.garrypedia.listener;

import android.content.Context;

/** Menghubungkan klien HTTP dengan riwayat. */
final class Sender {
    enum Outcome { DONE, RETRY }

    private Sender() {}

    static Notify.Res post(Context c, String text) {
        return Notify.post(Store.url(c), Store.secret(c), text);
    }

    /**
     * Mencatat hasil akhir ke riwayat. Kesalahan jaringan atau server (5xx) tidak dicatat di sini
     * dan mengembalikan RETRY agar pemanggil menjadwalkan ulang.
     */
    static Outcome record(Context c, Notify.Res r, long amount, String pkg) {
        if (r.code == 0 && r.err != null && (r.err.startsWith("URL") || r.err.startsWith("Secret"))) {
            Store.log(c, "gagal", amount, pkg, r.err);
            return Outcome.DONE;
        }
        if (r.code == 0 || r.code >= 500 || (r.code == 200 && r.err != null)) return Outcome.RETRY;
        if (r.code == 200) {
            if (r.matched) Store.log(c, "cocok", amount, pkg, r.orderId.isEmpty() ? "Order lunas" : "Order " + r.orderId);
            else Store.log(c, "tidak_cocok", amount, pkg, "Tidak ada order menunggu dengan total ini");
            return Outcome.DONE;
        }
        if (r.code == 401) Store.log(c, "gagal", amount, pkg, "Secret salah (401)");
        else Store.log(c, "gagal", amount, pkg, "Ditolak server (" + r.code + ")");
        return Outcome.DONE;
    }
}
