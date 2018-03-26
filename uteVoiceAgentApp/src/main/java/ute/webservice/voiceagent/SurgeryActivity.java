package ute.webservice.voiceagent;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.api.android.AIConfiguration;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.ui.AIButton;

public class SurgeryActivity extends BaseActivity implements AIButton.AIButtonListener, RetrievalListener {

    private static String TAG = SurgeryActivity.class.getName();

    private AIButton aiButton;
    private Button cancelButton;
    private TextView queryTextView;

    SurgeryParentListAdapter listAdapter;
    ExpandableListView listView;
    List<String> categoryHeaders;
    HashMap<String, List<String>> categoryChildren;

    private String query;

    private ParseResult PR;
    private DataAsked dataasked;

    private SharedData sessiondata;
    private String accountID;
    private int account_access;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surgery);
        initializeToolbar();
        initializeButtons();
        initializeTextViews();
        initializeSharedData();
        initializeListView();
    }

    /**
     * Creates and executes a SurgeryCategoryRetrieveTask to get all the surgery categories and subcategories.
     */
    private void initializeListView(){
        listView = (ExpandableListView)findViewById(R.id.surgeryListView);
        if (listView != null){
            SurgeryParentListAdapter adapter = new SurgeryParentListAdapter(this, ProcedureInfo.getCategoryNames());
            adapter.setWidth(R.dimen.surgery_list_width);
            listView.setAdapter(adapter);
        }
    }

    /**
     * Sets up the text views in this activity.
     */
    private void initializeTextViews(){
        queryTextView = (TextView)findViewById(R.id.querytextView);
    }

    /**
     * Sets up the sessiondata, dataasked and account data variables.
     */
    private void initializeSharedData(){
        sessiondata = new SharedData(getApplicationContext());
        accountID = sessiondata.getKeyAccount();
        account_access = sessiondata.getKeyAccess();
        dataasked = new DataAsked();
    }

    /**
     * Sets up the toolbar elements for this activity
     */
    private void initializeToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(toolbar);
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                PR = new ParseResult(response);

                query = PR.get_ResolvedQuery();

                dataasked.setIncomplete(PR.get_ActionIncomplete());
                dataasked.setCurrentReply(PR.get_reply());
                dataasked.setCensusUnit(PR.getCensusUnit());
                dataasked.setCurrentSurgeryCategory(PR.get_param_Surgery());
                dataasked.setCurrentAction(PR.get_Action());
                Log.d("OUTPUTRESPONSE", PR.get_reply());

                // Retrieve the information and display the results
                RetrieveTask httpTask = new RetrieveTask(dataasked,
                        CertificateManager.getSSlContext(SurgeryActivity.this)); // the task to retrieve the information
                httpTask.addListener(SurgeryActivity.this);
                httpTask.execute();
            }

        });
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

    /**
     * If the query needs to be more specific (i.e. a surgery type or unit name), open the
     * appropriate Activity. Else open the activity to display the results.
     * @param result the result of what what retrieved from the server
     */
    @Override
    public void onRetrieval(String result) {
        if (dataasked.isIncomplete()){
            if (dataasked.getCurrentAction().equals(Constants.GET_CENSUS)){
                // TODO: Send to the activity that will prompt for a unit name
                Intent intent = new Intent(this, AIListActivity.class);
                intent.putExtra("query", PR.get_ResolvedQuery());
                intent.putExtra("result", result);
                startActivity(intent);
            }
            else if (dataasked.getCurrentAction().equals(Constants.GET_SURGERY_COST)){
                // TODO: Send to the activity that will prompt for a surgery category
            }
        }
        else {
            // open a ResultsActivity with the query and the corresponding result
            Intent intent = new Intent(this, ResultsActivity.class);
            intent.putExtra("query", query);
            intent.putExtra("result", result);
            startActivity(intent);
        }
    }
}