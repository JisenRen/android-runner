package com.jisenren.runner;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

public class SensorListenService extends Service implements SensorEventListener {
    private final String TAG = "SensorListenService";
    private SensorManager manager = null;

    public SensorListenService() {}

    @Override
    public IBinder onBind(Intent intent) {
        return null;
        // throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "Service started!");
        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service stopped!");
        manager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        String name = sensorEvent.sensor.getName();
        float x, y, z;
        x = sensorEvent.values[0];
        y = sensorEvent.values.length >= 2 ? sensorEvent.values[1] : 0.0f;
        z = sensorEvent.values.length >= 3 ? sensorEvent.values[2] : 0.0f;
        Log.d(TAG, String.format("%s: (%.5f, %.5f, %.5f) %.5f", name, x, y, z,
                Math.sqrt(x*x+y*y+z*z)));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // unused
    }
}