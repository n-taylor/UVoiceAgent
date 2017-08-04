package ute.webservice.voiceagent;

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


import com.google.gson.Gson;

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

public class AIButtonActivity extends BaseActivity implements AIButton.AIButtonListener {

    public static final String TAG = AIButtonActivity.class.getName();

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

    /**
     * Try to send dummy text query to the server
     */
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

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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

                if(PR.reply_yes()) {
                    if(dataasked.isParameter_Enough())
                    {
                        if(dataasked.IsAccessable(account_access)){
                        String speech = PR.get_reply();
                            RetrieveFeedTask httpTask = new RetrieveFeedTask();
                            httpTask.execute();
                        }
                        else{
                            String speech = "Sorry, you are not permitted to access these information.";
                            resultTextView.setText(speech);
                            TTS.speak(speech);
                        }
                    }
                }
                else
                {
                    if(PR.reply_sq()) {

                        Log.i(TAG, "get_param_q_type: " + PR.get_param_q_type());
                        Log.i(TAG, "get_param_Surgery: " + PR.get_param_Surgery());
                        dataasked.assign_params(PR.get_param_q_type(), PR.get_param_Surgery());
                    }
                        String speech = PR.get_reply();
                        resultTextView.setText(speech);
                        TTS.speak(speech);

                }
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
            } catch (Exception e) {
                this.exception = e;
            }
            return data;
        }

        @Override
        protected void onPostExecute(String str){
            TextView resultTV_insync = (TextView) findViewById(R.id.resultTextView);
            Log.d(TAG,str);
            if(str!=null){
                resultTV_insync.setText(str);
                TTS.speak(str);
            }
        }

    }
}
