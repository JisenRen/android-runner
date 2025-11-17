package com.jisenren.runner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class MainFragment extends Fragment implements View.OnClickListener {
    // Cycling location service (original)
    private boolean cyclingWorking = false;
    private Button cyclingButton;
    
    // Constant location service (new)
    private boolean constantWorking = false;
    private EditText latitudeInput;
    private EditText longitudeInput;
    private CheckBox randomWalkingCheckbox;
    private EditText stepLengthInput;
    private Button startButton;
    private Button stopButton;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private double stepLength = 0.0;

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
        
        // Setup cycling location button (original)
        cyclingButton = view.findViewById(R.id.button);
        cyclingButton.setText(R.string.turn_on);
        cyclingButton.setOnClickListener(this);
        
        // Setup constant location inputs and buttons (new)
        latitudeInput = view.findViewById(R.id.latitude_input);
        longitudeInput = view.findViewById(R.id.longitude_input);
        randomWalkingCheckbox = view.findViewById(R.id.random_walking_checkbox);
        stepLengthInput = view.findViewById(R.id.step_length_input);
        startButton = view.findViewById(R.id.start_button);
        stopButton = view.findViewById(R.id.stop_button);
        
        // Enable/disable step length input based on checkbox state
        randomWalkingCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                stepLengthInput.setEnabled(isChecked);
            }
        });
        
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleConstantStartButtonClick();
            }
        });
        
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleConstantStopButtonClick();
            }
        });
        
        return view;
    }

    // Handle cycling location button click (original service)
    @SuppressLint("MissingPermission")
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button) {
            Intent intent = new Intent(requireActivity(), MockLocationService.class);
            if (cyclingWorking) {
                cyclingButton.setText(R.string.turn_on);
                requireActivity().stopService(intent);
                cyclingWorking = false;
            } else {
                cyclingButton.setText(R.string.turn_off);
                requireActivity().startForegroundService(intent);
                cyclingWorking = true;
            }
        }
    }

    // Constant location service methods
    private boolean validateAndSaveInputs() {
        String latStr = latitudeInput.getText().toString().trim();
        String lonStr = longitudeInput.getText().toString().trim();
        boolean randomWalkingEnabled = randomWalkingCheckbox.isChecked();
        String stepLengthStr = stepLengthInput.getText().toString().trim();
        
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
        
        // Validate step length if random walking is enabled
        if (randomWalkingEnabled) {
            if (TextUtils.isEmpty(stepLengthStr)) {
                Toast.makeText(requireContext(), R.string.step_length_required, Toast.LENGTH_LONG).show();
                return false;
            }
            
            try {
                stepLength = Double.parseDouble(stepLengthStr);
                if (stepLength <= 0.0) {
                    Toast.makeText(requireContext(), R.string.invalid_step_length, Toast.LENGTH_LONG).show();
                    return false;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), R.string.invalid_step_length, Toast.LENGTH_LONG).show();
                return false;
            }
        }
        
        return true;
    }

    @SuppressLint("MissingPermission")
    private void handleConstantStartButtonClick() {
        if (!validateAndSaveInputs()) {
            return;
        }
        
        Intent intent = new Intent(requireActivity(), ConstantLocationService.class);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        intent.putExtra("randomWalking", randomWalkingCheckbox.isChecked());
        if (randomWalkingCheckbox.isChecked()) {
            intent.putExtra("stepLength", stepLength);
        }
        requireActivity().startForegroundService(intent);
        
        constantWorking = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        latitudeInput.setEnabled(false);
        longitudeInput.setEnabled(false);
        randomWalkingCheckbox.setEnabled(false);
        stepLengthInput.setEnabled(false);
    }

    private void handleConstantStopButtonClick() {
        Intent intent = new Intent(requireActivity(), ConstantLocationService.class);
        requireActivity().stopService(intent);
        
        constantWorking = false;
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        latitudeInput.setEnabled(true);
        longitudeInput.setEnabled(true);
        randomWalkingCheckbox.setEnabled(true);
        stepLengthInput.setEnabled(randomWalkingCheckbox.isChecked());
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        // Stop both services if they are running
        if (cyclingWorking) {
            Intent intent = new Intent(requireActivity(), MockLocationService.class);
            requireActivity().stopService(intent);
        }
        if (constantWorking) {
            Intent intent = new Intent(requireActivity(), ConstantLocationService.class);
            requireActivity().stopService(intent);
        }
        super.onDestroy();
    }
}