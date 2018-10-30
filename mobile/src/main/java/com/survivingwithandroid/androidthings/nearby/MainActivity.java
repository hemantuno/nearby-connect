package com.survivingwithandroid.androidthings.nearby;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;
import com.skyfishjy.library.RippleBackground;
import com.survivingwithandroid.androidthings.nearby.adapter.CustomAdapter;
import com.thanosfisherman.wifiutils.WifiUtils;

import java.util.List;

import androidx.annotation.NonNull;


import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.CAMERA;

/*
 * Copyright (C) 2018 Francesco Azzola
 *  Surviving with Android (https://www.survivingwithandroid.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "NearbyApp";
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 101;
    private static final int PERMISSION_REQUEST_CODE = 102;
    private NearbyDsvManager dsvManager;

    private EditText metPassword;
    private Button mbtnConnect;
    private Context context;
    private CodeScanner mCodeScanner;

    private  String SERVICE_ID = "";

    String[] perms = {"android.permission.FINE_LOCATION", "android.permission.CAMERA"};
    private CustomAdapter mwifiListAdapter;
    private Spinner mSpinner;
    private List<ScanResult> mWifiLists;
    private String ssid;
    private EditText metSsid;
    private RippleBackground rippleBackground;
    private FrameLayout scannerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rippleBackground= findViewById(R.id.content);
        scannerLayout = findViewById(R.id.frameLayout);


        mSpinner = findViewById(R.id.spinner);


        context = getApplicationContext();

        scanQrCode();

        mbtnConnect = findViewById(R.id.btn);
        metSsid = findViewById(R.id.etSsid);
        metPassword = findViewById(R.id.ed);
        // set click false
        mbtnConnect.setEnabled(false);

        mbtnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String sid = metSsid.getText().toString();
                String password = metPassword.getText().toString();
                String data = sid+"-"+password;
                dsvManager.sendData(data);

            }
        });

        // Get wifi list

        WifiUtils.withContext(getApplicationContext()).enableWifi();
        WifiUtils.withContext(getApplicationContext()).scanWifi(MainActivity.this::getScanResults).start();

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                 ssid = mWifiLists.get(position).SSID;
                 metSsid.setText(ssid);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });


    }
    @Override
    protected void onStart() {
        super.onStart();
        requestPermission();

    }
    @Override
    protected void onResume() {
        super.onResume();
        // Check permission
        if(checkPermission()){
            mCodeScanner.startPreview();
        }

    }

    @Override
    protected void onPause() {
        if(checkPermission()){
            mCodeScanner.releaseResources();
        }
        super.onPause();
    }

    private void scanQrCode(){
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);

        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        Toast.makeText(context, result.getText(), Toast.LENGTH_SHORT).show();

                        scannerLayout.setVisibility(View.GONE);
                        rippleBackground.setVisibility(View.VISIBLE);
                        String qrcode[] = result.getText().split("-");
                        try {
                            SERVICE_ID = qrcode[1];
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if(!TextUtils.isEmpty(SERVICE_ID)){
                            rippleBackground.startRippleAnimation();
                            dsvManager = new NearbyDsvManager(context,listener,SERVICE_ID);
                        }


                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
    }
    private void checkResult(boolean isSuccess)
    {
        if (isSuccess)
            Toast.makeText(MainActivity.this, "CONNECTED YAY", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(MainActivity.this, "COULDN'T CONNECT", Toast.LENGTH_SHORT).show();
    }
    private void getScanResults(@NonNull final List<ScanResult> results)
    {
        if (results.isEmpty())
        {
            Log.i(TAG, "SCAN RESULTS IT'S EMPTY");
            mSpinner.setVisibility(View.INVISIBLE);
            return;
        }
        Log.i(TAG, "GOT SCAN RESULTS " + results);

        mSpinner.setVisibility(View.VISIBLE);

        mWifiLists = results;

        // Default zero position wifi ssid
        ssid = mWifiLists.get(0).SSID;
        metSsid.setText(ssid); // set ssid int edittext box
        mwifiListAdapter=new CustomAdapter(getApplicationContext(),results);
        mSpinner.setAdapter(mwifiListAdapter);
    }





    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{ACCESS_COARSE_LOCATION, CAMERA}, PERMISSION_REQUEST_CODE);

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "On Request Permission"+requestCode);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted && cameraAccepted){
                        // Permission Granted, Now you can access location data and camera.

                        mCodeScanner.startPreview();

                    }

                    else {

                        // Permission Denied, You cannot access location data and camera.

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(ACCESS_COARSE_LOCATION)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{ACCESS_COARSE_LOCATION, CAMERA},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }


                break;
        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


    private NearbyDsvManager.EventListener listener = new NearbyDsvManager.EventListener() {
       @Override
       public void onDiscovered() {

           Toast.makeText(MainActivity.this, "Endpoint discovered", Toast.LENGTH_LONG).show();

       }

       @Override
       public void startDiscovering() {
           Toast.makeText(MainActivity.this, "Start discovering...", Toast.LENGTH_LONG).show();
       }

       @Override
       public void onConnected() {
           Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_LONG).show();
           mbtnConnect.setEnabled(true);
           rippleBackground.stopRippleAnimation();
       }
   };


    public  boolean setSsidAndPassword(Context context, String ssid, String ssidPassword) {

        return true;
    }



}
