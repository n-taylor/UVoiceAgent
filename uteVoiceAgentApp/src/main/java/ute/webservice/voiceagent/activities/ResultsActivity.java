package ute.webservice.voiceagent.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

import ai.api.android.AIConfiguration;
import ai.api.android.GsonFactory;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.ui.AIButton;
import ute.webservice.voiceagent.R;
import ute.webservice.voiceagent.oncall.util.OnCallRetrievalListener;
import ute.webservice.voiceagent.oncall.util.OnCallRetrieveTask;
import ute.webservice.voiceagent.util.Constants;
import ute.webservice.voiceagent.util.Controller;
import ute.webservice.voiceagent.util.TTS;
import ute.webservice.voiceagent.util.CertificateManager;
import ute.webservice.voiceagent.util.Config;
import ute.webservice.voiceagent.util.DataAsked;
import ute.webservice.voiceagent.util.LogoutTask;
import ute.webservice.voiceagent.util.ParseResult;
import ute.webservice.voiceagent.util.RetrievalListener;
import ute.webservice.voiceagent.util.RetrieveTask;
import ute.webservice.voiceagent.util.SharedData;

public class ResultsActivity extends BaseActivity implements AIButton.AIButtonListener {

    private String TAG = ResultsActivity.class.getName();

    private AIButton aiButton;
    private Button cancelButton;
    private TextView queryTextView;
    private TextView resultsTextView;

    private Gson gson = GsonFactory.getGson();
    private DataAsked dataAsked;
    private ParseResult PR;

    SharedData sessiondata;
    private String accountID;
    private int account_access;

    //Progress bar
    private ProgressDialog progress;

    private String query;
    private String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        aiButton = (AIButton) findViewById(R.id.micButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        queryTextView = (TextView) findViewById(R.id.query_text);
        resultsTextView = (TextView) findViewById(R.id.result_textView);
        resultsTextView.setMovementMethod(new ScrollingMovementMethod());

        extractBundle();

        //Open shared data
        sessiondata = new SharedData(getApplicationContext());
        accountID = sessiondata.getKeyAccount();
        account_access = sessiondata.getKeyAccess();

        initializeToolbar();

        initializeButtons();


        //Save asked query
        dataAsked = new DataAsked();


    }

    /**
     * Extracts objects from the bundle to determine what the query and results text views display
     * and what is spoken.
     */
    private void extractBundle(){
        Bundle bundle = getIntent().getExtras();

        boolean speak = true;

        // set the texts to the query and retrieved answer
        if (bundle != null){
            query = bundle.getString("query");
            result = bundle.getString("result");
            if (bundle.containsKey("speak")){
                speak = bundle.getBoolean("speak");
            }
        }

        if (query != null)
            queryTextView.setText(query);
        if (result != null) {
            resultsTextView.setText(result);
            if (speak)
                TTS.speak(result);
        }
    }

    /**
     * Sets up the toolbar
     */
    private void initializeToolbar(){
        TextView userIDText = (TextView) findViewById(R.id.userText);
        userIDText.setText(accountID);

        //Set up action bar by toolbar
        Toolbar settintTB= (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(settintTB);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        settintTB.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TTS.stop();
                finish();
            }
        });
    }

    /**
     * Initializes the microphone and cancel buttons
     */
    private void initializeButtons(){
        final AIConfiguration config = new AIConfiguration(Config.ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        config.setRecognizerStartSound(getResources().openRawResourceFd(R.raw.test_start));
        config.setRecognizerStopSound(getResources().openRawResourceFd(R.raw.test_stop));
        config.setRecognizerCancelSound(getResources().openRawResourceFd(R.raw.test_cancel));

        // set up the microphone/AI button
        aiButton.initialize(config);
        aiButton.setResultsListener(this);

        // set up the cancel button
        cancelButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                // stop any speech that is being played currently
                TTS.stop();
            }
        });
    }

    /**
     * Show response from the API.AI server,
     * If parameters are enough and user said "Yes", try to get data from webservice.
     * @param response
     */
    @Override
    public void onResult(final AIResponse response) {
        Controller.processDialogFlowResponse(this, response, queryTextView);
    }

    @Override
    public void onError(final AIError error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onError");
                resultsTextView.setText(error.toString());
            }
        });
    }

    @Override
    public void onCancelled() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onCancelled");
                resultsTextView.setText("");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_aibutton_sample, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;

            case R.id.action_logout:
                // Create an LogoutTask and execute it to logout
                LogoutTask httpTask = new LogoutTask(this);
                httpTask.execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed(){
        TTS.stop();
        super.onBackPressed();
    }
}
