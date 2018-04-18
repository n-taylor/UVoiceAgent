package ute.webservice.voiceagent.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import ai.api.android.AIConfiguration;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.ui.AIButton;
import ute.webservice.voiceagent.oncall.util.OnCallRetrievalListener;
import ute.webservice.voiceagent.oncall.util.OnCallRetrieveTask;
import ute.webservice.voiceagent.procedures.ProceduresSelectListAdapter;
import ute.webservice.voiceagent.util.CertificateManager;
import ute.webservice.voiceagent.util.Config;
import ute.webservice.voiceagent.util.Constants;
import ute.webservice.voiceagent.util.Controller;
import ute.webservice.voiceagent.util.DataAsked;
import ute.webservice.voiceagent.util.LogoutTask;
import ute.webservice.voiceagent.util.ParseResult;
import ute.webservice.voiceagent.R;
import ute.webservice.voiceagent.util.RetrievalListener;
import ute.webservice.voiceagent.util.RetrieveTask;
import ute.webservice.voiceagent.util.SharedData;

public class ProceduresSelectActivity extends BaseActivity implements AIButton.AIButtonListener {

    private static String TAG = ProceduresListActivity.class.getName();

    private AIButton aiButton;
    private Button cancelButton;
    private TextView queryTextView;
    private ListView listView;
    private ArrayList<String> procedures;

    private String query;

    private ParseResult PR;
    private DataAsked dataAsked;

    private SharedData sessiondata;
    private String accountID;
    private int account_access;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_procedures_select);

        sessiondata = new SharedData(getApplicationContext());
        accountID = sessiondata.getKeyAccount();
        account_access = sessiondata.getKeyAccess();

        TextView userIDText = (TextView) findViewById(R.id.userText);
        userIDText.setText(accountID);


        initializeToolbar();
        initializeButtons();
        initializeTextViews();
        initializeSharedData();
        initializeBundle();
        initializeListView();




    }

    /**
     * Initializes the list view and populates it with the descriptions in the Map codes.
     */
    private void initializeListView(){
        listView = (ListView)findViewById(R.id.surgery_codes_list_view);
        ProceduresSelectListAdapter adapter = new ProceduresSelectListAdapter(this, procedures);
        adapter.setBackColor(ContextCompat.getColor(this, R.color.color_slategrey));
        adapter.setTextColor(Color.WHITE);
        listView.setAdapter(adapter);
    }

    /**
     * Gets the extras from the bundle.
     * Sets the query text view to the value "message" in the bundle if it is not null.
     * Sets the codes map equal to the map in the bundle.
     */
    private void initializeBundle(){
        Bundle bundle = getIntent().getExtras();
        String message = (String)bundle.get("message");
        if (message != null)
            queryTextView.setText(message);
        procedures = (ArrayList<String>)bundle.get("procedures");
    }

    /**
     * Sets up the text views in this activity.
     */
    private void initializeTextViews(){
        queryTextView = (TextView)findViewById(R.id.querytextView);
    }

    /**
     * Sets up the sessiondata, dataAsked and account data variables.
     */
    private void initializeSharedData(){
        sessiondata = new SharedData(getApplicationContext());
        accountID = sessiondata.getKeyAccount();
        account_access = sessiondata.getKeyAccess();
        dataAsked = new DataAsked();
    }

    /**
     * Sets up the toolbar elements for this activity
     */
    private void initializeToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Sets up the AI button and cancel button for this activity.
     */
    private void initializeButtons(){
        // configure the AI Button
        aiButton = (AIButton)findViewById(R.id.micButton);
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
        cancelButton = (Button)findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // TODO: Add action for cancel button here.
            }
        });
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
                queryTextView.setText("Please try again");
            }
        });
    }

    @Override
    public void onCancelled() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onCancelled");
                queryTextView.setText("");
            }
        });
    }
}
