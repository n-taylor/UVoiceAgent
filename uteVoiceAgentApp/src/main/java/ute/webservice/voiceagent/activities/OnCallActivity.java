package ute.webservice.voiceagent.activities;

/**
 * Created by u0450254 on 3/29/2018.
 */


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

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


public class OnCallActivity extends BaseActivity implements AIButton.AIButtonListener, RetrievalListener {


    private static String TAG = OpenBedsActivity.class.getName();

    ListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

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


        sessiondata = new SharedData(getApplicationContext());
        accountID = sessiondata.getKeyAccount();
        account_access = sessiondata.getKeyAccess();

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

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.resultListView);

        // preparing list data

        expListView.setBackgroundResource(R.drawable.menushape);

        dummyListData();

        listAdapter = new ListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);



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
                // stop any speech that is being played currently
                TTS.stop();
            }
        });
        dataAsked = new DataAsked();

    }

    private void dummyListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add("1");
        listDataHeader.add("2");
        listDataHeader.add("3");

        // Adding child data

        List<String> one = new ArrayList<String>();
        one.add("A");
        one.add("B");
        one.add("C");

        List<String> two = new ArrayList<String>();
        two.add("A");
        two.add("B");
        two.add("C");

        List<String> three = new ArrayList<String>();
        three.add("A");
        three.add("B");
        three.add("C");




        listDataChild.put(listDataHeader.get(0), one);
        listDataChild.put(listDataHeader.get(1), two);
        listDataChild.put(listDataHeader.get(2), three);
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
            }
            else if (dataAsked.getCurrentAction().equals(Constants.GET_SURGERY_COST)){
                // TODO: Send to the activity that will prompt for a surgery category
            }
        }
        else {
            // open a ResultsActivity with the query and the corresponding result
            Intent intent = new Intent(this, ResultsActivity.class);


            //if result is from button, extract
            if (result.contains("[{"))
            {
                int aindex = result.indexOf("available");

                aindex += 10;

                int bindex = result.indexOf(",", aindex);

                String sAnswer = result.substring(aindex+1,bindex);

                int answer = Integer.parseInt(sAnswer);

                String beds = "beds";

                if (answer == 1)
                {
                    beds = "bed";
                }

                int sindex = result.indexOf("has");
                result = result.substring(0,sindex)+"has "+sAnswer+" "+beds+" available";
            }
            intent.putExtra("query", query);
            intent.putExtra("result", result);
            startActivity(intent);
        }
    }

    //launch census request via button
    public void launchCensus(String room)
    {

        String roomx = room.replaceAll("\\s", "");

        query = roomx;

        dataAsked.setCensusUnit(roomx);
        dataAsked.setCurrentAction("getCensus");
        dataAsked.setCurrentReply(room + " has this many beds remaning:");
        dataAsked.setIncomplete(false);
        dataAsked.setCurrentSurgeryCategory("");
        // Log.d("OUTPUTRESPONSE", PR.get_reply());

        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(OnCallActivity.this)); // the task to retrieve the information
        httpTask.addListener(OnCallActivity.this);
        httpTask.execute();
    }
}
