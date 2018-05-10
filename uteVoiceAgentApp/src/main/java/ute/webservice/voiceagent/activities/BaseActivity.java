package ute.webservice.voiceagent.activities;

/***********************************************************************************************************************
 * API.AI Android SDK -  API.AI libraries usage example
 * =================================================
 * <p/>
 * Copyright (C) 2015 by Speaktoit, Inc. (https://www.speaktoit.com)
 * https://www.api.ai
 * <p/>
 * **********************************************************************************************************************
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ***********************************************************************************************************************/

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;

import ute.webservice.voiceagent.util.Constants;
import ute.webservice.voiceagent.util.DataAsked;

public abstract class BaseActivity extends AppCompatActivity {

    private AIApplication app;

    private static final long PAUSE_CALLBACK_DELAY = 500;
    private static final int REQUEST_AUDIO_PERMISSIONS_ID = 33;

    private final Handler handler = new Handler();
    private Runnable pauseCallback = new Runnable() {
        @Override
        public void run() {
            app.onActivityPaused();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (AIApplication) getApplication();
    }

    @Override
    protected void onResume() {
        super.onResume();
        app.onActivityResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.postDelayed(pauseCallback, PAUSE_CALLBACK_DELAY);
    }

    protected void checkAudioRecordPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_AUDIO_PERMISSIONS_ID);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSIONS_ID: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    public void onRetrieval(String result, DataAsked dataAsked, Context context, String query){
        if (dataAsked.isIncomplete()){
            if (dataAsked.getCurrentAction().equals(Constants.GET_CENSUS)){
                // TODO: Send to the activity that will prompt for a unit name
                Intent intent = new Intent(context, OpenBedsActivity.class);
                intent.putExtra("query", query);
                intent.putExtra("result", result);
                startActivity(intent);
            }
            else if (dataAsked.getCurrentAction().equals(Constants.GET_SURGERY_COST)){
                Intent intent = new Intent(context, ProceduresListActivity.class);
                startActivity(intent);
            }
            else if (dataAsked.getCurrentAction().equals(Constants.GET_ONCALL)){
                Intent intent = new Intent(context, ProceduresListActivity.class);
                startActivity(intent);
            }
        }
        else {
            if (dataAsked.getCurrentAction().equals(Constants.GET_CENSUS)
                    || dataAsked.getCurrentAction().equalsIgnoreCase(Constants.GET_SURGERY_COST)) {
                // open a ResultsActivity with the query and the corresponding result
                Intent intent = new Intent(context, ResultsActivity.class);
                intent.putExtra("query", query);
                intent.putExtra("result", result);
                startActivity(intent);
            }
        }
    }

    public void onCallRetrieval(HashMap<String, ArrayList<String>> numbers, Context context, String query){
        for (String name : numbers.keySet()){
            for (String number : numbers.get(name)){
                System.out.println(name + " -> " + number);
            }
        }

        Intent intent = new Intent(context, OnCallActivity.class);
        intent.putExtra("query", query);
        intent.putExtra("phoneNumMap", numbers);
        startActivity(intent);
    }
}
