package com.survivingwithandroid.androidthings.nearby;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.thanosfisherman.wifiutils.WifiUtils;

import net.glxn.qrgen.android.QRCode;

import java.util.Random;

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

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ConnectionsClient client;
    private ImageView mivQrCode;
    protected String SERVICE_ID = "123123123";
    private TextView mtvDeviceId;

    private Handler mHandler;
    private ProgressDialog progressDialog;
    private NearbyAdvManager advManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        Log.i(TAG, "Starting Android Things app...");

        mtvDeviceId = (TextView) findViewById(R.id.tvDeviceId);
        mivQrCode = (ImageView) findViewById(R.id.ivQrCode);

        SERVICE_ID = generateRandomId();
        mtvDeviceId.setText(SERVICE_ID);

         advManager = new NearbyAdvManager(this, new NearbyAdvManager.EventListener() {
          @Override
          public void onMessage(String message) {

              Toast.makeText(MainActivity.this, "message:"+message, Toast.LENGTH_SHORT).show();

              String credentials[] = message.split("-");
              String username = credentials[0];
              String password = credentials[1];

                      WifiUtils.withContext(getApplicationContext())
                              .connectWith(username, password)
                              .onConnectionResult(MainActivity.this::checkResult)
                              .start();


          }
        },SERVICE_ID);

        generateQrcode();

    }

    @Override
    public void onBackPressed() {
        advManager.stopDiscovery();
        super.onBackPressed();
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        advManager.stopDiscovery();
        super.onDestroy();
    }

    private String generateRandomId(){
        Random rand = new Random();
        String ranNumber  = String.valueOf(rand.nextInt(100000));

        Log.d(TAG, "generateRandomId: "+ranNumber);

        return ranNumber;

    }

    /***
     * Generate qr code and display with imageview
     */
    private void generateQrcode(){

        Bitmap myBitmap=QRCode.from("uno-"+SERVICE_ID).withSize(500,500).bitmap();
        mivQrCode.setImageBitmap(myBitmap);

    }

    private void checkResult(boolean isSuccess)
    {
        if (isSuccess)
            Toast.makeText(MainActivity.this, "CONNECTED YAY", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(MainActivity.this, "COULDN'T CONNECT", Toast.LENGTH_SHORT).show();
    }

}
