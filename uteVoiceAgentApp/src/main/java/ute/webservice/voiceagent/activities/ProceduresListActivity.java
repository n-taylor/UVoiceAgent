package ute.webservice.voiceagent.activities;

import android.os.Bundle;
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

import ai.api.android.AIConfiguration;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.ui.AIButton;
import ute.webservice.voiceagent.oncall.util.OnCallRetrievalListener;
import ute.webservice.voiceagent.procedures.ProceduresParentListAdapter;
import ute.webservice.voiceagent.util.Config;
import ute.webservice.voiceagent.util.Controller;
import ute.webservice.voiceagent.util.DataAsked;
import ute.webservice.voiceagent.util.ParseResult;
import ute.webservice.voiceagent.R;
import ute.webservice.voiceagent.util.RetrievalListener;
import ute.webservice.voiceagent.util.SharedData;

public class ProceduresListActivity extends BaseActivity implements AIButton.AIButtonListener, RetrievalListener, OnCallRetrievalListener {

    private static String TAG = ProceduresListActivity.class.getName();

    private AIButton aiButton;
    private Button cancelButton;
    private TextView queryTextView;
    private TextView userIDText;

    ProceduresParentListAdapter listAdapter;
    ExpandableListView listView;
    List<String> categoryHeaders;
    HashMap<String, List<String>> categoryChildren;

    private String query;

    private ParseResult PR;
    private DataAsked dataAsked;
    private android.support.v7.widget.Toolbar setting_toolbar;

    private SharedData sessiondata;
    private String accountID;
    private int account_access;

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

        userIDText = (TextView) findViewById(R.id.userText);
        userIDText.setText(accountID);
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
                Controller.getController().onCancelPressed();
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

        Controller.processDialogFlowResponse(this, response);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//
//                PR = new ParseResult(response);
//
//                query = PR.get_ResolvedQuery();
//
//                dataAsked.setIncomplete(PR.get_ActionIncomplete());
//                dataAsked.setCurrentReply(PR.get_reply());
//                dataAsked.setCensusUnit(PR.getCensusUnit());
//                dataAsked.setCurrentSurgeryCategory(PR.get_param_Surgery());
//                dataAsked.setCurrentAction(PR.get_Action());
//                Log.d("OUTPUTRESPONSE", PR.get_reply());
//
//                if (PR.get_Action().equalsIgnoreCase(Constants.GET_ONCALL)){
//                    OnCallRetrieveTask task = new OnCallRetrieveTask();
//                    String OCMID = ParseResult.extractOCMID(PR.get_reply());
//                    task.addListener(ProceduresListActivity.this);
//                    task.execute(OCMID);
//                }
//                else {
//                    // Retrieve the information and display the results
//                    RetrieveTask httpTask = new RetrieveTask(dataAsked,
//                            CertificateManager.getSSlContext(ProceduresListActivity.this)); // the task to retrieve the information
//                    httpTask.addListener(ProceduresListActivity.this);
//                    httpTask.execute();
//                }
//            }
//
//        });
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

        super.onRetrieval(result, dataAsked, this, PR.get_ResolvedQuery());

//        if (dataAsked.isIncomplete()){
//            if (dataAsked.getCurrentAction().equals(Constants.GET_CENSUS)){
//                // TODO: Send to the activity that will prompt for a unit name
//                Intent intent = new Intent(this, OpenBedsActivity.class);
//                intent.putExtra("query", PR.get_ResolvedQuery());
//                intent.putExtra("result", result);
//                startActivity(intent);
//            }
//            else if (dataAsked.getCurrentAction().equals(Constants.GET_SURGERY_COST)){
//                // TODO: Send to the activity that will prompt for a surgery category
//            }
//        }
//        else {
//            // open a ResultsActivity with the query and the corresponding result
//            Intent intent = new Intent(this, ResultsActivity.class);
//            intent.putExtra("query", query);
//            intent.putExtra("result", result);
//            startActivity(intent);
//        }
    }

    @Override
    public void onOnCallRetrieval(HashMap<String, ArrayList<String>> numbers) {
        super.onCallRetrieval(numbers, this, PR.get_ResolvedQuery());
    }
}