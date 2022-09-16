package com.jisenren.runner;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

public class MainActivity extends AbstractPermissionActivity {
    private static final String[] PERMS=
            {Manifest.permission.ACCESS_FINE_LOCATION};

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    protected String[] getDesiredPermissions() {
        return(PERMS);
    }

    @Override
    protected void onPermissionDenied() {
        Toast.makeText(this, R.string.perm_denied, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onReady() {
        if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content,
                            new MainFragment()).commit();
        }
    }
}