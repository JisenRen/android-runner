package com.jisenren.runner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class MainFragment extends Fragment implements View.OnClickListener {
    boolean working = false;
    // LocationManager mgr;
    public final String TAG = "LocationUpdate";
    public MainFragment() {}

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Intent intent = new Intent(requireActivity(), LocationUpdateService.class);
        // requireActivity().startService(intent);
        // mgr = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.mainfrag, container, false);
        Button button = view.findViewById(R.id.button);
        button.setText(R.string.turn_on);
        button.setOnClickListener(this);
        return view;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStart() {
        super.onStart();
        // mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onClick(View view) {
        Intent intent = new Intent(requireActivity(), MockLocationService.class);
        if (working) {
            ((Button) view.findViewById(R.id.button)).setText(R.string.turn_on);
            requireActivity().stopService(intent);
            working = false;
        } else {
            ((Button) view.findViewById(R.id.button)).setText(R.string.turn_off);
            requireActivity().startService(intent);
            working = true;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // mgr.removeUpdates(this);
    }

    @Override
    public void onDestroy() {
        // Intent intent = new Intent(requireActivity(), LocationUpdateService.class);
        // requireActivity().stopService(intent);
        super.onDestroy();
    }

    /*    @SuppressLint("DefaultLocale")
    @Override
    public void onLocationChanged(@NonNull Location location) {
        double lat = location.getLatitude(), lon = location.getLongitude();
        // Log.d(TAG, String.format("Longitude: %.5f; Latitude: %.5f.", lon, lat));
        TextView view;
        view = requireActivity().findViewById(R.id.longitude);
        view.setText(String.format("%.5f", lon));
        view = requireActivity().findViewById(R.id.latitude);
        view.setText(String.format("%.5f", lat));
    } */
}