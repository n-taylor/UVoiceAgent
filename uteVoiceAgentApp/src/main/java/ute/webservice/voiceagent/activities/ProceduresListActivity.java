package ute.webservice.voiceagent.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import ai.api.android.AIConfiguration;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.ui.AIButton;
import ute.webservice.voiceagent.procedures.ProceduresParentListAdapter;
import ute.webservice.voiceagent.util.Config;
import ute.webservice.voiceagent.util.Controller;
import ute.webservice.voiceagent.util.DataAsked;
import ute.webservice.voiceagent.util.ParseResult;
import ute.webservice.voiceagent.R;
import ute.webservice.voiceagent.util.SharedData;

public class ProceduresListActivity extends BaseActivity implements AIButton.AIButtonListener {

    private static String TAG = ProceduresListActivity.class.getName();

    private AIButton aiButton;
    private TextView queryTextView;

    ProceduresParentListAdapter listAdapter;
    ExpandableListView listView;
    List<String> categoryHeaders;
    HashMap<String, List<String>> categoryChildren;

    private String query;

    private ParseResult PR;
    private android.support.v7.widget.Toolbar setting_toolbar;

    private String accountID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_procedures_list);


        initializeToolbar();
        initializeButtons();
        initializeSharedData();
        initializeTextViews();
        initializeListView();
    }

    /**
     * Creates and executes a ProcedureCategoryRetrieveTask to get all the surgery categories and subcategories.
     */
    private void initializeListView(){
        listView = (ExpandableListView)findViewById(R.id.surgeryListView);
        Controller.getController().initializeProceduresExpandableList(this, listView);
    }

    /**
     * Sets up the text views in this activity.
     */
    private void initializeTextViews(){
        queryTextView = (TextView)findViewById(R.id.querytextView);

        TextView userIDText = (TextView) findViewById(R.id.userText);
        userIDText.setText(accountID);
    }

    /**
     * Sets up the sessiondata, dataAsked and account data variables.
     */
    private void initializeSharedData(){
        SharedData sessiondata = new SharedData(getApplicationContext());
        accountID = sessiondata.getKeyAccount();
        int account_access = sessiondata.getKeyAccess();
        DataAsked dataAsked = new DataAsked();
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
        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Controller.getController().onCancelPressed();
            }
        });
        Button helpButton = (Button) findViewById(R.id.helpButton);
        helpButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                RelativeLayout hiddenView = (RelativeLayout) findViewById(R.id.helpView);
                hiddenView.bringToFront();
            }
        });
        Button returnButton = (Button) findViewById(R.id.returnButton);
        returnButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                RelativeLayout hiddenView = (RelativeLayout) findViewById(R.id.mainView);
                RelativeLayout bottomView = (RelativeLayout) findViewById(R.id.bottom_container);
                hiddenView.bringToFront();
                bottomView.bringToFront();
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
                Controller.getController().onLogoutPressed(this);
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