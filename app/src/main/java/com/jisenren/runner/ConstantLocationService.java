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

public class ConstantLocationService extends Service {
    private final String TAG = "ConstantLocationService";
    private MockLocationProvider provider;
    private ExecutorService executor;
    private Future<?> future;

    public ConstantLocationService() {}

    private class ConstantLocationThread implements Runnable {
        double latitude;
        double longitude;
        int sleep_ms;
        
        public ConstantLocationThread(double lat, double lon, int freq) {
            latitude = lat;
            longitude = lon;
            sleep_ms = freq;
            Log.d(TAG, String.format("Constant location thread initialized with lat: %.5f, lon: %.5f", lat, lon));
        }
        
        public void run() {
            while (true) {
                try {
                    Thread.sleep(sleep_ms);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
                provider.pushLocation(latitude, longitude);
                Log.d(TAG, String.format("Constant latitude: %.5f; Constant longitude: %.5f", latitude, longitude));
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        
        // Get latitude and longitude from intent, with defaults if not provided
        double latitude = intent.getDoubleExtra("latitude", 39.9859);
        double longitude = intent.getDoubleExtra("longitude", 116.30715);
        
        provider = new MockLocationProvider(LocationManager.GPS_PROVIDER, getBaseContext());
        Log.i(TAG, String.format("Constant location service enabled with lat: %.5f, lon: %.5f", latitude, longitude));
        
        NotificationChannel channel;
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("constant_location_channel", "Constant Location",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("The notification channel for starting the foreground service of constant location.");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            notification = new NotificationCompat.Builder(this, channel.getId())
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle("Constant Location Service")
                    .setContentText("The constant location service started.")
                    .build();
        } else {
            notification = new NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle("Constant Location Service")
                    .setContentText("The constant location service started.")
                    .build();
        }
        startForeground(1338, notification);
        executor = Executors.newSingleThreadExecutor();
        Runnable task = new ConstantLocationThread(latitude, longitude, 500);
        future = executor.submit(task);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Constant location service disabled!");
        if (future != null) {
            future.cancel(true);
        }
        if (executor != null) {
            executor.shutdown();
        }
        if (provider != null) {
            provider.shutdown();
        }
    }
}

