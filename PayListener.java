package id.garrypedia.listener;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Membaca notifikasi masuk, menyaringnya, lalu mengirim ke gateway. */
public class PayListener extends NotificationListenerService {
    private static final ExecutorService EX = Executors.newSingleThreadExecutor();

    @Override
    public void onListenerDisconnected() {
        try {
            requestRebind(new ComponentName(this, PayListener.class));
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            if (sbn == null || getPackageName().equals(sbn.getPackageName())) return;
            Notification n = sbn.getNotification();
            if (n == null || (n.flags & Notification.FLAG_GROUP_SUMMARY) != 0) return;
            Bundle e = n.extras;
            if (e == null) return;
            String title = str(e.getCharSequence(Notification.EXTRA_TITLE));
            String text = str(e.getCharSequence(Notification.EXTRA_TEXT));
            String big = str(e.getCharSequence(Notification.EXTRA_BIG_TEXT));
            String sub = str(e.getCharSequence(Notification.EXTRA_SUB_TEXT));
            StringBuilder sb = new StringBuilder();
            if (!title.isEmpty()) sb.append(title).append('\n');
            sb.append(big.isEmpty() ? text : big);
            if (!sub.isEmpty()) sb.append('\n').append(sub);
            final String full = sb.toString().trim();
            final String pkg = sbn.getPackageName();
            if (full.isEmpty()) return;
            final Context c = getApplicationContext();
            EX.execute(() -> process(c, pkg, full));
        } catch (Throwable ignored) {
        }
    }

    private static String str(CharSequence s) { return s == null ? "" : s.toString(); }

    private static void process(Context c, String pkg, String full) {
        try {
            List<String> inc = Rules.split(Store.include(c), true);
            List<String> exc = Rules.split(Store.exclude(c), true);
            String text = Rules.pickText(full, inc);
            if (text.length() > 1500) text = text.substring(0, 1500);
            long amount = Rules.parseAmount(text);
            if (amount <= 0) return;
            if (Rules.isDuplicate(pkg + "|" + text, System.currentTimeMillis())) return;

            Rules.Decision d = Rules.decide(pkg, text, Store.allApps(c), Store.packages(c), inc, exc);
            if (d == Rules.Decision.NO_AMOUNT) return;
            if (d == Rules.Decision.WRONG_APP) {
                Store.log(c, "diabaikan", amount, pkg, "Aplikasi belum dipilih");
                return;
            }
            if (d == Rules.Decision.FILTERED) {
                Store.log(c, "diabaikan", amount, pkg, "Tidak lolos filter kata kunci");
                return;
            }

            Notify.Res r = Sender.post(c, text);
            if (Sender.record(c, r, amount, pkg) == Sender.Outcome.RETRY) {
                Store.log(c, "antre", amount, pkg, r.err != null ? r.err : "Server " + r.code + ", akan dicoba lagi");
                enqueueRetry(c, text, pkg, amount);
            }
        } catch (Throwable ignored) {
        }
    }

    private static void enqueueRetry(Context c, String text, String pkg, long amount) {
        Data data = new Data.Builder().putString("text", text).putString("pkg", pkg).putLong("amt", amount).build();
        Constraints cs = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest w = new OneTimeWorkRequest.Builder(SendWorker.class)
                .setConstraints(cs)
                .setInputData(data)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.SECONDS)
                .build();
        WorkManager.getInstance(c).enqueue(w);
    }
}
