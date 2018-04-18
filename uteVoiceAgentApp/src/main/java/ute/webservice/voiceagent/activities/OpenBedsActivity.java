package ute.webservice.voiceagent.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/**
 * Created by u0450254 on 3/13/2018.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.widget.ExpandableListView;
import android.widget.TextView;

import ai.api.android.AIConfiguration;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.ui.AIButton;
import ute.webservice.voiceagent.R;
import ute.webservice.voiceagent.oncall.util.OnCallRetrievalListener;
import ute.webservice.voiceagent.oncall.util.OnCallRetrieveTask;
import ute.webservice.voiceagent.util.Controller;
import ute.webservice.voiceagent.util.TTS;
import ute.webservice.voiceagent.openbeds.ListAdapter;
import ute.webservice.voiceagent.util.CertificateManager;
import ute.webservice.voiceagent.util.Config;
import ute.webservice.voiceagent.util.Constants;
import ute.webservice.voiceagent.util.DataAsked;
import ute.webservice.voiceagent.util.LogoutTask;
import ute.webservice.voiceagent.util.ParseResult;
import ute.webservice.voiceagent.util.RetrievalListener;
import ute.webservice.voiceagent.util.RetrieveTask;
import ute.webservice.voiceagent.util.SharedData;

public class OpenBedsActivity extends BaseActivity implements AIButton.AIButtonListener {

    private static String TAG = OpenBedsActivity.class.getName();

    ListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> unitGroups;
    HashMap<String, List<String>> units;
    
    private Button cancelButton;
    private AIButton aiButton;
    private TextView queryTextView;
    private android.support.v7.widget.Toolbar setting_toolbar;

    private DataAsked dataAsked;
    private ParseResult PR;
    private String query;

    SharedData sessiondata;
    private String accountID;
    private int account_access;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_openbeds);


        sessiondata = new SharedData(getApplicationContext());
        accountID = sessiondata.getKeyAccount();
        account_access = sessiondata.getKeyAccess();

        initializeToolbar();
        initializeButtons();
        initializeExpandableList();

        dataAsked = new DataAsked();

    }

    /**
     * Initializes the components of the toolbar
     */
    private void initializeToolbar(){
        TextView userIDText = (TextView) findViewById(R.id.userText);
        userIDText.setText(accountID);

        setting_toolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(setting_toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setting_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Initializes the button components
     */
    private void initializeButtons(){
        aiButton = (AIButton) findViewById(R.id.micButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        queryTextView = (TextView) findViewById(R.id.querytextView);

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
                Controller.getController().onCancelPressed();
            }
        });
    }

    /**
     * Initializes the list and sets it up to retrieve the appropriate data when clicked
     */
    private void initializeExpandableList(){
        initializeUnitLists();

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.resultListView);

        // preparing list data
        initializeUnitLists();

        expListView.setBackgroundResource(R.drawable.menushape);

        listAdapter = new ListAdapter(this, unitGroups, units);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        final TextView statusView = this.queryTextView;

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
                String group = unitGroups.get(groupPosition);
                String unit = units.get(group).get(childPosition);
                Controller.getController().displayOpenBedCount(view.getContext(), unit);
                return true;
            }
        });
    }

    /*
     * Preparing the list data
     */
    private void initializeUnitLists() {
        unitGroups = new ArrayList<String>();
        units = new HashMap<String, List<String>>();

        // Adding child data
        unitGroups.add("U Neuro Institute");
        unitGroups.add("University Hospitals");
        unitGroups.add("Huntsman Cancer Institute");

        // Adding child data

        List<String> UNI = new ArrayList<String>();
        UNI.add("2A");
        UNI.add("2B");
        UNI.add("2 EAST");
        UNI.add("2 NORTH");
        UNI.add("2 SOUTH");
        UNI.add("3 NORTH");
        UNI.add("3 SOUTH");
        UNI.add("4 NORTH");
        UNI.add("4 SOUTH");

        List<String> UH = new ArrayList<String>();
        UH.add("5W");
        UH.add("AIMA");
        UH.add("AIMB");
        UH.add("BRN");
        UH.add("CVICU");
        UH.add("CVMU");
        UH.add("ICN");
        UH.add("IMR");
        UH.add("LND");
        UH.add("MICU");
        UH.add("MNBC");
        UH.add("NAC");
        UH.add("NCCU");
        UH.add("NICU");
        UH.add("NNCCN");
        UH.add("NSY");
        UH.add("OBGY");
        UH.add("OTSS");
        UH.add("SICU");
        UH.add("SSTU");
        UH.add("WP5");

        List<String> HC = new ArrayList<String>();
        HC.add("HCBMT ");
        HC.add("HCH4");
        HC.add("HCH5");
        HC.add("HCICU");
      

        units.put(unitGroups.get(0), UNI);
        units.put(unitGroups.get(1), UH);
        units.put(unitGroups.get(2), HC);
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
