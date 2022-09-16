package com.jisenren.runner;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

public class LocationUpdateService extends Service implements LocationListener {
    private LocationManager mgr;
    private final String TAG = "LocationUpdateService";

    public LocationUpdateService() {}

    @Override
    public void onCreate() {
        mgr = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "Location tracker started!");
        /* Notification notification = new NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Runner program")
                .setContentText("The location updating service started.")
                .build();
        startForeground(1337, notification); */
        mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        return(START_STICKY);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Location tracker stopped!");
        mgr.removeUpdates(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
        // throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        double lat = location.getLatitude(), lon = location.getLongitude();
        Log.d(TAG, String.format("Longitude: %.5f; Latitude: %.5f.", lon, lat));
    }
}