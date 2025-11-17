package com.jisenren.runner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class MainFragment extends Fragment {
    private boolean working = false;
    private EditText latitudeInput;
    private EditText longitudeInput;
    private Button startButton;
    private Button stopButton;
    private double latitude = 0.0;
    private double longitude = 0.0;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mainfrag, container, false);
        
        latitudeInput = view.findViewById(R.id.latitude_input);
        longitudeInput = view.findViewById(R.id.longitude_input);
        startButton = view.findViewById(R.id.start_button);
        stopButton = view.findViewById(R.id.stop_button);
        
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleStartButtonClick();
            }
        });
        
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleStopButtonClick();
            }
        });
        
        return view;
    }

    private boolean validateAndSaveInputs() {
        String latStr = latitudeInput.getText().toString().trim();
        String lonStr = longitudeInput.getText().toString().trim();
        
        if (TextUtils.isEmpty(latStr) || TextUtils.isEmpty(lonStr)) {
            Toast.makeText(requireContext(), R.string.invalid_input, Toast.LENGTH_LONG).show();
            return false;
        }
        
        try {
            latitude = Double.parseDouble(latStr);
            longitude = Double.parseDouble(lonStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), R.string.invalid_input, Toast.LENGTH_LONG).show();
            return false;
        }
        
        if (latitude < -90.0 || latitude > 90.0) {
            Toast.makeText(requireContext(), R.string.invalid_latitude, Toast.LENGTH_LONG).show();
            return false;
        }
        
        if (longitude < -180.0 || longitude > 180.0) {
            Toast.makeText(requireContext(), R.string.invalid_longitude, Toast.LENGTH_LONG).show();
            return false;
        }
        
        return true;
    }

    @SuppressLint("MissingPermission")
    private void handleStartButtonClick() {
        if (!validateAndSaveInputs()) {
            return;
        }
        
        Intent intent = new Intent(requireActivity(), MockLocationService.class);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        requireActivity().startForegroundService(intent);
        
        working = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        latitudeInput.setEnabled(false);
        longitudeInput.setEnabled(false);
    }

    private void handleStopButtonClick() {
        Intent intent = new Intent(requireActivity(), MockLocationService.class);
        requireActivity().stopService(intent);
        
        working = false;
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        latitudeInput.setEnabled(true);
        longitudeInput.setEnabled(true);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (working) {
            Intent intent = new Intent(requireActivity(), MockLocationService.class);
            requireActivity().stopService(intent);
        }
        super.onDestroy();
    }
}