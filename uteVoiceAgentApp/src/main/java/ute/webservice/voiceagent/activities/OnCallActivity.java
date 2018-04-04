package ute.webservice.voiceagent.activities;

/**
 * Created by u0450254 on 3/29/2018.
 */


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
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
import ute.webservice.voiceagent.oncall.ClientTestEmulator;
import ute.webservice.voiceagent.oncall.OnCallListAdapter;
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

/**
 * Before starting this activity, two items should be added to its intent's bundle:
 *
 * 1. A HashMap<String, ArrayList<String>> under the label phoneNumMap. This should be a mapping
 *  of Names to a list of phone numbers.
 *
 * 2. A String under the label "query", which is the desired text to display at the top of the activity
 */
public class OnCallActivity extends BaseActivity implements AIButton.AIButtonListener, RetrievalListener {

    private static String TAG = OpenBedsActivity.class.getName();

    private ListAdapter listAdapter;
    private ExpandableListView expListView;
    private ArrayList<String> names;
    private HashMap<String, ArrayList<String>> phoneNumbers;

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
        setContentView(R.layout.activity_oncall);

        extractBundle();
        initializeSharedData();
        initializeToolbar();
        initializeButtons();
        initializeListView();
        initializeTextViews();
    }

    private void extractBundle(){
        Bundle bundle = getIntent().getExtras();

        // set the texts to the query and retrieved answer
        if (bundle != null){
            query = bundle.getString("query");
            HashMap<String, ArrayList<String>> phoneNumMap = (HashMap<String, ArrayList<String>>)bundle.get("phoneNumMap");
            names = new ArrayList<>();
            names.addAll(phoneNumMap.keySet());
            phoneNumbers = phoneNumMap;
        }
    }

    private void initializeListView() {
        expListView = (ExpandableListView)findViewById(R.id.on_call_ListView);
        OnCallListAdapter adapter = new OnCallListAdapter(this, names, phoneNumbers);
        adapter.setWidth(getResources().getDimensionPixelSize(R.dimen.surgery_list_width)-200);
        expListView.setAdapter(adapter);
    }

    private void initializeSharedData() {
        sessiondata = new SharedData(getApplicationContext());
        accountID = sessiondata.getKeyAccount();
        account_access = sessiondata.getKeyAccess();
        dataAsked = new DataAsked();
    }

    private void initializeTextViews() {
        this.queryTextView = (TextView)findViewById(R.id.on_call_querytextView);
        queryTextView.setText(query);
    }

    private void initializeButtons() {
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
                // stop any speech that is being played currently
                TTS.stop();
            }
        });
    }

    private void initializeToolbar() {
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

        TextView userIDText = (TextView) findViewById(R.id.userText);
        userIDText.setText(accountID);
    }

    /**
     * Tries to make a set call to the webservice to get on call information and print the result
     */
    private void testCall(){
        try {

            // Open a socket to the server
            Socket socket = new Socket("155.100.69.40", 9720);

            // Wait for the server to accept connection before reading the xml file

            String group = ParseResult.extractOCMID("CARDIOLOGY - FETAL [10000636]");
//            String group = "10000636";
            String toRead = ParseResult.getCurrentAssignmentsCall(group);
            BufferedReader reader = new BufferedReader(new StringReader(toRead));
            String line;
            StringBuilder  stringBuilder = new StringBuilder();
            while((line = reader.readLine() ) != null) {
                stringBuilder.append(line);
            }

            // Send xml data to server

            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(stringBuilder.toString());
            //writer.close();
            // Wait for server response
            ClientTestEmulator.getSingleClientEmulator().readServerResponse(socket);
            writer.close();

        } catch (IOException | NumberFormatException ex) {
            System.out.println("Error: " + ex);
        }
        catch (Exception e){
            e.printStackTrace();
        }
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

    @Override
    public void onResult(final AIResponse response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                PR = new ParseResult(response);

                query = PR.get_ResolvedQuery();

                dataAsked.setIncomplete(PR.get_ActionIncomplete());
                dataAsked.setCurrentReply(PR.get_reply());
                dataAsked.setCensusUnit(PR.getCensusUnit());
                dataAsked.setCurrentSurgeryCategory(PR.get_param_Surgery());
                dataAsked.setCurrentAction(PR.get_Action());
                Log.d("OUTPUTRESPONSE", PR.get_reply());

                // Retrieve the information and display the results
                RetrieveTask httpTask = new RetrieveTask(dataAsked,
                        CertificateManager.getSSlContext(OnCallActivity.this)); // the task to retrieve the information
                httpTask.addListener(OnCallActivity.this);
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
        if (dataAsked.isIncomplete()){
            if (dataAsked.getCurrentAction().equals(Constants.GET_CENSUS)){
                // TODO: Send to the activity that will prompt for a unit name
                Intent intent = new Intent(this, OpenBedsActivity.class);
                intent.putExtra("query", PR.get_ResolvedQuery());
                intent.putExtra("result", result);
                startActivity(intent);
            }
            else if (dataAsked.getCurrentAction().equals(Constants.GET_SURGERY_COST)){
                Intent intent = new Intent(this, ProceduresListActivity.class);
                startActivity(intent);
            }
            else if (dataAsked.getCurrentAction().equals(Constants.GET_ONCALL)){
                Intent intent = new Intent(this, ProceduresListActivity.class);
                startActivity(intent);
            }
        }
        else {
            // open a ResultsActivity with the query and the corresponding result
            Intent intent = new Intent(this, ResultsActivity.class);
            intent.putExtra("query", PR.get_ResolvedQuery());
            intent.putExtra("result", result);
            startActivity(intent);
        }
    }
}
