package id.garrypedia.listener;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/** Mengulang pengiriman yang gagal karena jaringan atau server, saat koneksi tersedia. */
public class SendWorker extends Worker {
    private static final int MAX_TRIES = 10;

    public SendWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context c = getApplicationContext();
        String text = getInputData().getString("text");
        String pkg = getInputData().getString("pkg");
        long amount = getInputData().getLong("amt", -1);
        if (text == null) return Result.failure();

        Notify.Res r = Sender.post(c, text);
        Sender.Outcome o = Sender.record(c, r, amount, pkg);
        if (o == Sender.Outcome.RETRY) {
            if (getRunAttemptCount() >= MAX_TRIES) {
                Store.log(c, "gagal", amount, pkg, "Menyerah setelah " + MAX_TRIES + " percobaan");
                return Result.failure();
            }
            return Result.retry();
        }
        return Result.success();
    }
}
