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

import android.app.Application;
import android.content.Context;
import android.util.Log;

import ai.api.util.BluetoothController;
import ute.webservice.voiceagent.settings.SettingsManager;

public class AIApplication extends Application {

    private static final String TAG = AIApplication.class.getSimpleName();

    private int activitiesCount;
    private SettingsManager settingsManager;

    @Override
    public void onCreate() {
        super.onCreate();
        settingsManager = new SettingsManager(this);
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    protected void onActivityResume() {
        if (activitiesCount++ == 0) { // on become foreground
        }
    }

    protected void onActivityPaused() {
        if (--activitiesCount == 0) { // on become background
        }
    }

    private boolean isInForeground() {
        return activitiesCount > 0;
    }
}
