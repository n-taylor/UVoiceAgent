package ai.api.voiceagent;

/***********************************************************************************************************************
 *
 * API.AI Android SDK -  API.AI libraries usage example
 * =================================================
 *
 * Copyright (C) 2015 by Speaktoit, Inc. (https://www.speaktoit.com)
 * https://www.api.ai
 *
 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************/

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
//import android.widget.Toolbar;


import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntityHC4;
import org.apache.http.client.methods.HttpPostHC4;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ai.api.AIServiceException;
import ai.api.RequestExtras;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.android.GsonFactory;
import ai.api.model.AIContext;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.ui.AIButton;

public class AIButtonSampleActivity extends BaseActivity implements AIButton.AIButtonListener {

    public static final String TAG = AIButtonSampleActivity.class.getName();

    private AIButton aiButton;
    private TextView resultTextView;
    private TextView queryTextView;

    private Gson gson = GsonFactory.getGson();
    private DataAsked dataasked;

    SharedData sessiondata;
    private String accountID;
    private int account_access;

    //Progress bar
    private ProgressDialog progress;

    //
    private AIDataService aiDataService;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aibutton_sample);

        //TextView
        resultTextView = (TextView) findViewById(R.id.resultTextView);
        resultTextView.setGravity(Gravity.RIGHT);
        queryTextView = (TextView) findViewById(R.id.querytextView);
        aiButton = (AIButton) findViewById(R.id.micButton);

        //Open shared data
        sessiondata = new SharedData(getApplicationContext());
        accountID = sessiondata.getKeyAccount();
        account_access = sessiondata.getKeyAccess();

        //Set up action bar by toolbar
        Toolbar settintTB= (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(settintTB);

        final AIConfiguration config = new AIConfiguration(Config.ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        config.setRecognizerStartSound(getResources().openRawResourceFd(R.raw.test_start));
        config.setRecognizerStopSound(getResources().openRawResourceFd(R.raw.test_stop));
        config.setRecognizerCancelSound(getResources().openRawResourceFd(R.raw.test_cancel));

        aiButton.initialize(config);
        aiButton.setResultsListener(this);

        //Save asked query
        dataasked = new DataAsked();

        //Build connection to server
        //apiConnect();

        //RetrieveFeedTask httpTask = new RetrieveFeedTask();
        //httpTask.execute();

        //Welcome message
        resultTextView.setText(Html.fromHtml("<b>Welcome, "+accountID+"!</b>"));


    }

    private void apiConnect(){
        progress = new ProgressDialog(this);
        progress.setMessage("Connecting...");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(true);
        progress.setProgress(0);
        progress.show();

        final int totalProgressTime = 100;
        final Thread t = new Thread() {
            @Override
            public void run() {
                int jumpTime = 0;

                while(jumpTime < totalProgressTime) {
                    try {
                        sendRequest();
                        sleep(200);
                        jumpTime += 5;
                        progress.setProgress(jumpTime);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();
    }


    private void sendRequest() {

        final String queryString = "Hello";

        final AsyncTask<String, Void, AIResponse> task = new AsyncTask<String, Void, AIResponse>() {

            private AIError aiError;

            @Override
            protected AIResponse doInBackground(final String... params) {
                final AIRequest request = new AIRequest();
                String query = params[0];
                //String event = params[1];

                if (!TextUtils.isEmpty(query))
                    request.setQuery(query);
                //if (!TextUtils.isEmpty(event))
                //    request.setEvent(new AIEvent(event));
                final String contextString = params[1];
                RequestExtras requestExtras = null;
                if (!TextUtils.isEmpty(contextString)) {
                    final List<AIContext> contexts = Collections.singletonList(new AIContext(contextString));
                    requestExtras = new RequestExtras(contexts, null);
                }

                try {
                    return aiDataService.request(request, requestExtras);
                } catch (final AIServiceException e) {
                    aiError = new AIError(e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final AIResponse response) {
                if (response != null) {
                    onResult(response);
                } else {
                    onError(aiError);
                }
            }
        };

        task.execute(queryString);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // use this method to disconnect from speech recognition service
        // Not destroying the SpeechRecognition object in onPause method would block other apps from using SpeechRecognition service
        aiButton.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // use this method to reinit connection to recognition service
        aiButton.resume();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_aibutton_sample, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public String getPhoneNumber(){
        String phoneNumber = " not assigned";
        try {
      LaxRedirectStrategy redirectStrategy = new LaxRedirectStrategy();
        CloseableHttpClient httpclient = HttpClients.custom()
                .setRedirectStrategy(redirectStrategy)
                .build();

//            Htt =  RequestBuilder.post()
//                .setUri("http://people.utah.edu/uWho/basic.hml")
//                .addParameter("searchTerm", "Karthi Jeyabalan")
//                .addParameter("searchRole", "0")
//                .build();


//          //TODO
            HttpPostHC4 httppost = new HttpPostHC4("http://people.utah.edu/uWho/basic.hml");
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("searchTerm", "Karthi Jeyabalan"));
            nameValuePairs.add(new BasicNameValuePair("searchRole", "0"));
            httppost.setEntity(new UrlEncodedFormEntityHC4(nameValuePairs));

          HttpHost hot = new HttpHost("http://people.utah.edu/");
//
            HttpResponse  httpResponse  =  httpclient.execute(hot,httppost);
           // httpclient.execute()
            StatusLine statusLine = httpResponse.getStatusLine();
            System.out.println("StatusLine : "+statusLine.toString());
            HttpEntity entity = httpResponse.getEntity();
            String reponseString = ( entity != null ? EntityUtils.toString(entity) : null);
//
           System.out.println( "response string " +reponseString);
//          //  String responseToString = HttpConnectionApacheCommon.readHttpResponseToString(httpResponse);
//            System.out.println("----------------------------------------");

            int startIndex = reponseString.indexOf("<td>801-");
            int endIndex = startIndex+16;
            //  System.out.println(startIndex+8);
             phoneNumber = reponseString.substring(startIndex+4,endIndex);

        } catch(Exception ex){
            System.out.println(ex.getMessage());

        }finally {
//            try{
//              //  httpclient.close();
//            }catch (java.io.IOException ex ){
//
//                System.out.println(ex.getMessage());
//            }

        }
        return phoneNumber;
    }

    /**
     * Show response from the API.AI server,
     * If parameters are enough and user said "Yes", try to get data from webservice.
     * @param response
     */
    @Override
    public void onResult(final AIResponse response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                ParseResult PR = new ParseResult(response);

                String query = PR.get_ResolvedQuery();
                queryTextView.setText(query);

                if(PR.reply_yes()==true) {
                    if(dataasked.isParameter_Enough()==true)
                    {
                        if(dataasked.IsAccessable(account_access)){
                        String speech = PR.get_reply();
                        //resultTextView.setText(speech+"\n"+dataasked.get_info());
                        //    resultTextView.setText(speech+"\n"+dataasked.get_info());
                            RetrieveFeedTask httpTask = new RetrieveFeedTask();
                            httpTask.execute();
                        //TTS.speak(speech);
                        }
                        else{
                            String speech = "Sorry, you are not permitted to access these information.";
                            resultTextView.setText(speech);
                            TTS.speak(speech);
                        }
                        //clear parameters
                        //dataasked.clear_params();
                    }
                }
                else
                {
                    if(PR.reply_sq()==true) {
                        /*
                        final Result result = response.getResult();
                        final HashMap<String, JsonElement> params = result.getParameters();
                        if (params != null && !params.isEmpty()) {
                            Log.i(TAG, "Parameters: ");
                            for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                                Log.i(TAG, String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
                            }
                        }
                        */

                        Log.i(TAG, "get_param_q_type: " + PR.get_param_q_type());
                        Log.i(TAG, "get_param_Surgery: " + PR.get_param_Surgery());
                        dataasked.assign_params(PR.get_param_q_type(), PR.get_param_Surgery());
                    }
                        String speech = PR.get_reply();
                        //resultTextView.setText(gson.toJson(response));
                        resultTextView.setText(speech);
                        TTS.speak(speech);

                    //RetrieveFeedTask httpTask = new RetrieveFeedTask();
                    //httpTask.execute();


                }

                /*
                Log.i(TAG, "Received success response");

                System.out.println("where is system.out.print");
                // this is example how to get different parts of result object
                final Status status = response.getStatus();
                Log.i(TAG, "Status code: " + status.getCode());
                Log.i(TAG, "Status type: " + status.getErrorType());

                final Result result = response.getResult();
                Log.i(TAG, "Resolved query: " + result.getResolvedQuery());

                Log.i(TAG, "Action: " + result.getAction());
                final String speech = result.getFulfillment().getSpeech();;
                Log.i(TAG, "Speech: " + speech);

                final Metadata metadata = result.getMetadata();
                if (metadata != null) {
                    Log.i(TAG, "Intent id: " + metadata.getIntentId());
                    Log.i(TAG, "Intent name: " + metadata.getIntentName());
                }
                */
            }

        });
    }

    @Override
    public void onError(final AIError error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onError");
                resultTextView.setText(error.toString());
            }
        });
    }

    @Override
    public void onCancelled() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onCancelled");
                resultTextView.setText("");
            }
        });
    }

    class RetrieveFeedTask extends AsyncTask<Void,Integer,String> {

        private Exception exception;

        @Override
        protected String doInBackground(Void... voids) {
            String data=null;
            try {
                 data = dataasked.getHttpClientReply();
                //String data = dataasked.get_info_html("");
                //return data;
            } catch (Exception e) {
                this.exception = e;
            }
            return data;
        }

        @Override
        protected void onPostExecute(String str){
            //super.onPostExecute(str);
            TextView resultTV_insync = (TextView) findViewById(R.id.resultTextView);
            Log.d(TAG,str);
            if(str!=null){
                resultTV_insync.setText(str);
                TTS.speak(str);
            }
        }

    }
}
