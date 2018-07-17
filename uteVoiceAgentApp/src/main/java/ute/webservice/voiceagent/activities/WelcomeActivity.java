package ute.webservice.voiceagent.activities;

import android.app.ProgressDialog;
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

import ai.api.android.AIConfiguration;
import ai.api.android.GsonFactory;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.ui.AIButton;
import ute.webservice.voiceagent.R;
import ute.webservice.voiceagent.util.Controller;
import ute.webservice.voiceagent.util.Config;
import ute.webservice.voiceagent.util.DataAsked;
import ute.webservice.voiceagent.util.ParseResult;
import ute.webservice.voiceagent.util.SharedData;

/**
 * The activity that displays the possible actions the app can take.
 *
 * To display a message at the top of the activity, insert a string into the bundle under the key "message".
 */
public class WelcomeActivity extends BaseActivity implements AIButton.AIButtonListener {

    private String TAG = WelcomeActivity.class.getName();

    private Controller controller;

    private AIButton aiButton;
    private Button cancelButton;
    private Button bedButton;
    private Button surgeryButton;
    private Button equipButton;
    private Button oncallButton;

    private TextView welcomeTextView;
    private ActionBar actionBar;

    private Gson gson = GsonFactory.getGson();
    private ParseResult PR;

    SharedData sessiondata;

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

        controller = Controller.getController();
        initializeButtons();


        //Open shared data
        sessiondata = new SharedData(getApplicationContext());
        String accountID = sessiondata.getKeyAccount();
        int account_access = sessiondata.getKeyAccess();

        TextView userIDText = (TextView) findViewById(R.id.userText);
        userIDText.setText(accountID);

                //Set up action bar by toolbar
        Toolbar settintTB= (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(settintTB);

        DataAsked dataAsked = new DataAsked();

        controller.onActivityCreated(this);

        extractBundle();
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
                controller.onCancelPressed();
            }
        });

        bedButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                controller.onBedButtonPressed(WelcomeActivity.this);
            }
        });

        surgeryButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                controller.onProcedureButtonPressed(view.getContext());
            }
        });

        oncallButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                controller.onOnCallButtonPressed(view.getContext());
            }
        });

        equipButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                controller.onEquipmentFinderButtonPressed(WelcomeActivity.this, view.getContext());
            }
        });

    }

    public void enableComponents(boolean enable){
        cancelButton.setEnabled(enable);
        aiButton.setEnabled(enable);
        bedButton.setEnabled(enable);
        surgeryButton.setEnabled(enable);
        equipButton.setEnabled(enable);
        oncallButton.setEnabled(enable);
    }

    /**
     * Checks if the welcome message has been set in the bundle
     */
    private void extractBundle(){
        try {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                String message = (String) bundle.get("message");
                if (message != null)
                    setWelcomeText(message);
            }
        }
        catch (ClassCastException e){
            setWelcomeText("There was a problem parsing the welcome message");
        }
    }

    public void setWelcomeText(String text){
        welcomeTextView.setText(text);
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
                controller.onLogoutPressed(this);
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

        Controller.processDialogFlowResponse(this, response, welcomeTextView);
    }

    @Override
    public void onError(final AIError error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onError");
                welcomeTextView.setText(R.string.dialog_flow_error);
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

    @Override
    public void onBackPressed() {
        // do nothing
    }

    /**
     * Enable the buttons and change the welcome text to its proper message.
     */
    public void onProcedureInfoRetrieval(){
        cancelButton.setEnabled(true);
        aiButton.setEnabled(true);
        bedButton.setEnabled(true);
        surgeryButton.setEnabled(true);
        equipButton.setEnabled(true);
        oncallButton.setEnabled(true);
        welcomeTextView.setText(R.string.welcome_message);
        
    }
}
