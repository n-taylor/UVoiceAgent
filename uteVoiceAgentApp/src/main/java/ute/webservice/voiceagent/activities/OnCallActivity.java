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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.widget.ExpandableListView;
import android.widget.TextView;

import ai.api.android.AIConfiguration;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.ui.AIButton;
import ute.webservice.voiceagent.R;
import ute.webservice.voiceagent.oncall.ClientTestEmulator;
import ute.webservice.voiceagent.oncall.OnCallListAdapter;
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

/**
 * Before starting this activity, two items should be added to its intent's bundle:
 *
 * 1. A HashMap<String, ArrayList<String>> under the label phoneNumMap. This should be a mapping
 *  of Names to a list of phone numbers.
 *
 * 2. A String under the label "query", which is the desired text to display at the top of the activity
 */
public class OnCallActivity extends BaseActivity implements AIButton.AIButtonListener {

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
        speak();
    }

    private void speak(){
        int size = names.size();
        String toSpeak = String.format(String.valueOf(size) + " result%s found.", (size != 1) ? "s" : "");
        if (names.size() == 1){
            toSpeak += " " + formatName(names.get(0));
        }
        TTS.speak(toSpeak);
    }

    /**
     * Given a name in the format "Last, First" or "Last, First Middle", reformats it into "First Middle Last"
     * If the string is not in that format, returns the string given.
     * @param name The name to format
     * @return The formatted name
     */
    private String formatName(String name){
        String ordered = name;
        Pattern pattern = Pattern.compile("(\\D+), [\\D\\s]+");
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()){
            String last = matcher.group(1);
            ordered = name.replace(last + ", ", "");
            ordered += " " + last;
        }
        return ordered;
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
        if (names.size() == 1){
            expListView.expandGroup(0);
        }
    }

    private void initializeSharedData() {
        sessiondata = new SharedData(getApplicationContext());
        accountID = sessiondata.getKeyAccount();
        account_access = sessiondata.getKeyAccess();
        dataAsked = new DataAsked();
    }

    private void initializeTextViews() {
        this.queryTextView = (TextView)findViewById(R.id.on_call_querytextView);
        query = query.substring(0,1).toUpperCase() + query.substring(1).toLowerCase();
        query = query.trim();

        // see if there are 5 or more results
        if (names.size() >= 5){
            query += ": " + names.size() + " results found";
        }
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
                TTS.stop();
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

    @Override
    public void onBackPressed(){
        TTS.stop();
        super.onBackPressed();
    }
}
