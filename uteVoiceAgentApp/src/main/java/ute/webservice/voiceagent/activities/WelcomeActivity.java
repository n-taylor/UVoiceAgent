package ute.webservice.voiceagent.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
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
import ute.webservice.voiceagent.util.TTS;
import ute.webservice.voiceagent.procedures.ProcedureInfo;
import ute.webservice.voiceagent.procedures.ProcedureInfoListener;
import ute.webservice.voiceagent.util.CertificateManager;
import ute.webservice.voiceagent.util.Config;
import ute.webservice.voiceagent.util.Constants;
import ute.webservice.voiceagent.util.DataAsked;
import ute.webservice.voiceagent.util.LogoutTask;
import ute.webservice.voiceagent.util.ParseResult;
import ute.webservice.voiceagent.util.RetrievalListener;
import ute.webservice.voiceagent.util.RetrieveTask;
import ute.webservice.voiceagent.util.SharedData;

public class WelcomeActivity extends BaseActivity implements AIButton.AIButtonListener, RetrievalListener, ProcedureInfoListener, OnCallRetrievalListener {

    private String TAG = WelcomeActivity.class.getName();

    private AIButton aiButton;
    private Button cancelButton;
    private Button bedButton;
    private Button surgeryButton;
    private Button equipButton;
    private Button oncallButton;

    private TextView welcomeTextView;
    private ActionBar actionBar;

    private Gson gson = GsonFactory.getGson();
    private DataAsked dataAsked;
    private ParseResult PR;

    SharedData sessiondata;
    private String accountID;
    private int account_access;

    //Progress bar
    private ProgressDialog progress;

    //CA variables
//    private CertificateFactory cf = null;
//    private Certificate ca;
//    private SSLContext sslContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        initializeButtons();




        //Open shared data
        sessiondata = new SharedData(getApplicationContext());
        accountID = sessiondata.getKeyAccount();
        account_access = sessiondata.getKeyAccess();

        TextView userIDText = (TextView) findViewById(R.id.userText);
        userIDText.setText(accountID);

                //Set up action bar by toolbar
        Toolbar settintTB= (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(settintTB);

        dataAsked = new DataAsked();

        fetchProcedureInfo();
    }

    private void fetchProcedureInfo(){
        if (ProcedureInfo.needsData()) {
            ProcedureInfo PI = ProcedureInfo.fetchData();
            if (PI != null)
                PI.addListener(this);
        }
    }

    private void initializeButtons(){
        aiButton = (AIButton) findViewById(R.id.micButton);
        welcomeTextView = (TextView) findViewById(R.id.welcome_message);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        bedButton = (Button) findViewById(R.id.bed_finder_button);
        surgeryButton = (Button) findViewById(R.id.cost_button);
        equipButton = (Button) findViewById(R.id.equipment_button);
        oncallButton = (Button) findViewById(R.id.on_call_button);

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

        bedButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), OpenBedsActivity.class);
                // intent.putExtra("query", PR.get_ResolvedQuery());
                startActivity(intent);

            }
        });

        surgeryButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ProceduresListActivity.class);
                startActivity(intent);
            }
        });

        oncallButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ResultsActivity.class);
                intent.putExtra("query", oncallButton.getText().toString().toUpperCase());
                String toShow = "For which message group are you searching? For example, say \"Attending Burn\" " +
                        "or \"Dental\"";
                intent.putExtra("result", toShow);
                intent.putExtra("speak", false);
                startActivity(intent);
            }
        });

        if (ProcedureInfo.needsData()) {
            // disable everything
            cancelButton.setEnabled(false);
            aiButton.setEnabled(false);
            bedButton.setEnabled(false);
            surgeryButton.setEnabled(false);
            equipButton.setEnabled(false);
            oncallButton.setEnabled(false);
            welcomeTextView.setText("Loading...");
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

                String query = PR.get_ResolvedQuery();

                dataAsked.setIncomplete(PR.get_ActionIncomplete());
                dataAsked.setCurrentReply(PR.get_reply());
                dataAsked.setCensusUnit(PR.getCensusUnit());
                dataAsked.setCurrentSurgeryCategory(PR.get_param_Surgery());
                dataAsked.setCurrentAction(PR.get_Action());
                Log.d("OUTPUTRESPONSE", PR.get_reply());
                if (PR.get_Action().equalsIgnoreCase(Constants.GET_ONCALL)){
                    OnCallRetrieveTask task = new OnCallRetrieveTask();
                    String OCMID = ParseResult.extractOCMID(PR.get_reply());
                    task.addListener(WelcomeActivity.this);
                    task.execute(OCMID);
                }
                else {
                    // Retrieve the information and display the results
                    RetrieveTask httpTask = new RetrieveTask(dataAsked,
                            CertificateManager.getSSlContext(WelcomeActivity.this)); // the task to retrieve the information
                    httpTask.addListener(WelcomeActivity.this);
                    httpTask.execute();
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
                welcomeTextView.setText("Please try again");
            }
        });
    }

    @Override
    public void onCancelled() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onCancelled");
                welcomeTextView.setText("");
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
//                Intent intent = new Intent(this, ProceduresListActivity.class);
//                startActivity(intent);
//            }
//            else if (dataAsked.getCurrentAction().equals(Constants.GET_ONCALL)){
//                Intent intent = new Intent(this, ProceduresListActivity.class);
//                startActivity(intent);
//            }
//        }
//        else {
//            if (dataAsked.getCurrentAction().equals(Constants.GET_CENSUS)
//                    || dataAsked.getCurrentAction().equalsIgnoreCase(Constants.GET_SURGERY_COST)) {
//                // open a ResultsActivity with the query and the corresponding result
//                Intent intent = new Intent(this, ResultsActivity.class);
//                intent.putExtra("query", PR.get_ResolvedQuery());
//                intent.putExtra("result", result);
//                startActivity(intent);
//            }
//        }
    }

    @Override
    public void onBackPressed() {
        // do nothing
    }

    /**
     * Enable the buttons and change the welcome text to its proper message.
     */
    public void onInfoRetrieval(){
        cancelButton.setEnabled(true);
        aiButton.setEnabled(true);
        bedButton.setEnabled(true);
        surgeryButton.setEnabled(true);
        equipButton.setEnabled(true);
        oncallButton.setEnabled(true);
        welcomeTextView.setText(R.string.welcome_message);
        
    }

    @Override
    public void onOnCallRetrieval(HashMap<String, ArrayList<String>> numbers) {

        super.onCallRetrieval(numbers, this, PR.get_ResolvedQuery());

//        for (String name : numbers.keySet()){
//            for (String number : numbers.get(name)){
//                System.out.println(name + " -> " + number);
//            }
//        }
//
//        Intent intent = new Intent(this, OnCallActivity.class);
//        intent.putExtra("query", PR.get_ResolvedQuery());
//        intent.putExtra("phoneNumMap", numbers);
//        startActivity(intent);
    }
}
