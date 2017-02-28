package com.na.android;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.na.android.utils.NaPermissionUtils;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NaPermissionUtils.NaPermissionCallbacks{

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean flag = NaPermissionUtils.hasPermissions(this, Manifest.permission.SEND_SMS, Manifest.permission.CAMERA);
        Log.e(TAG, "SEND_SMS flag=" + flag);
        if (!flag){
            NaPermissionUtils.requestPermissions(this, "SEND_SMS", 100, Manifest.permission.SEND_SMS, Manifest.permission.CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e(TAG, "onRequestPermissionsResult");
        NaPermissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

        for(String perm:perms){
            Log.e(TAG, "onPermissionsGranted perm=" + perm);
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        for(String perm:perms){
            Log.e(TAG, "onPermissionsGranted perm=" + perm);
        }
    }
}
