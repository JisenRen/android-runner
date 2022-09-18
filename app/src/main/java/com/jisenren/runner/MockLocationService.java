package com.jisenren.runner;

import static androidx.core.app.NotificationChannelCompat.DEFAULT_CHANNEL_ID;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MockLocationService extends Service {
    private final String TAG = "MockLocationService";
    private MockLocationProvider provider;
    private ExecutorService executor;
    private Future<?> future;

    public MockLocationService() {}

    private class MockLocationThread implements Runnable {
        double speed, sec_per_round, t1, t2, t3;
        int sleep_ms;
        final double x_deg = 116.30715, y0_deg = 39.9859, y1_deg = 39.9867, r_earth = 6.371e6;
        final double meter_per_deg = r_earth * Math.PI / 180.0;
        final double radius = 0.00045 * meter_per_deg * Math.cos(y0_deg*Math.PI/180.0);
        final double str_len = (y1_deg-y0_deg) * meter_per_deg;
        final double circumference = 2*(str_len + Math.PI*radius);
        public MockLocationThread(double speed_, int freq) {
            speed = speed_;
            sleep_ms = freq;
            sec_per_round = circumference / speed;
            t1 = str_len / speed;
            t2 = (str_len + Math.PI*radius) / speed;
            t3 = (2 * str_len + Math.PI*radius) / speed;
            Log.d(TAG, String.format("Time threshold: %.5f, %.5f, %.5f, %.5f", t1, t2, t3, sec_per_round));
        }
        public void run() {
            double run_time = 0.0, x, y, lat, lon;
            while (true) {
                try {
                    Thread.sleep(sleep_ms);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                run_time += sleep_ms * 1e-3;
                if (run_time>sec_per_round) run_time -= sec_per_round;
                if (run_time < t1) {
                    x = radius;
                    y = speed*run_time;
                } else if (run_time < t2) {
                    x = radius * Math.cos(Math.PI * (run_time-t1) / (t2-t1));
                    y = str_len + radius * Math.sin(Math.PI * (run_time-t1) / (t2-t1));
                } else if (run_time < t3) {
                    x = -radius;
                    y = str_len - speed*(run_time-t2);
                } else {
                    x = -radius * Math.cos(Math.PI * (run_time-t3) / (sec_per_round-t3));
                    y = -radius * Math.sin(Math.PI * (run_time-t3) / (sec_per_round-t3));
                }
                lon = x_deg + x / meter_per_deg / Math.cos(y1_deg*Math.PI/180.0);
                lat = y0_deg + y / meter_per_deg;
                provider.pushLocation(lat, lon);
                Log.d(TAG, String.format("Mock latitude: %.5f; Mock longitude: %.5f", lat, lon));
            }
        }
    }

    // @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        provider = new MockLocationProvider(LocationManager.GPS_PROVIDER, getBaseContext());
        Log.i(TAG, "Mock location service enabled!");
        NotificationChannel channel;
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                channel = new NotificationChannel("jisenren_channel", "Mock Location",
                        NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("The notification channel for starting the foreground service of mock location.");
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        notification = new NotificationCompat.Builder(this, channel.getId())
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Runner program")
                .setContentText("The location updating service started.")
                .build();
        } else {
            notification = new NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle("Runner program")
                    .setContentText("The location updating service started.")
                    .build();
        }
        startForeground(1337, notification);
        executor = Executors.newSingleThreadExecutor();
        Runnable task = new MockLocationThread(3.2, 500);
        future = executor.submit(task);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
        // throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Mock location service disabled!");
        future.cancel(true);
        executor.shutdown();
        provider.shutdown();
    }
}