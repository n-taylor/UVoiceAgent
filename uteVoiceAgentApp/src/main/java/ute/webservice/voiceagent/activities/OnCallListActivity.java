package ute.webservice.voiceagent.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import ai.api.android.AIConfiguration;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.ui.AIButton;
import ute.webservice.voiceagent.R;
import ute.webservice.voiceagent.oncall.OnCallController;
import ute.webservice.voiceagent.util.Config;
import ute.webservice.voiceagent.util.Controller;
import ute.webservice.voiceagent.util.DataAsked;
import ute.webservice.voiceagent.util.ParseResult;
import ute.webservice.voiceagent.util.SharedData;

public class OnCallListActivity extends BaseActivity implements AIButton.AIButtonListener{

    private static String TAG = OnCallListActivity.class.getName();

    private AIButton aiButton;
    private TextView queryTextView;
    private ListView listView;
    private EditText searchBar;

    private String query;

    private ParseResult PR;
    private android.support.v7.widget.Toolbar setting_toolbar;

    private OnCallController controller;

    private String accountID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_call_list);

        controller = new OnCallController();
        initializeToolbar();
        initializeSharedData();
        initializeTextViews();
        initializeButtons();
        extractBundle();
        initializeListView();
    }

    private void extractBundle(){
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("query")){
            queryTextView.setText(bundle.getString("query"));
        }
    }

    /**
     * Initializes the list view
     */
    private void initializeListView(){
        listView = (ListView)findViewById(R.id.search_list_view);
        controller.populateListView(this, listView, searchBar.getText().toString());
    }

    /**
     * Sets up the text views in this activity.
     */
    private void initializeTextViews(){
        queryTextView = (TextView)findViewById(R.id.query_text);
        searchBar = (EditText)findViewById(R.id.searchOnCall);

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

        // set up the search button
        Button searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                controller.populateListView(OnCallListActivity.this, getListView(), getSearchText());
            }
        });

        // set up the keyboard listener
//        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
//                if (i == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN){
//                    controller.populateListView(OnCallListActivity.this, getListView(), getSearchText());
//                }
//                return true;
//            }
//        });

        searchBar.setOnKeyListener(new TextView.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN){
                    String toSearch = getSearchText();
                    if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL && toSearch.length() > 0)
                        toSearch = toSearch.substring(0, toSearch.length()-1);
                    else
                        toSearch += keyEvent.getDisplayLabel();
                    controller.populateListView(OnCallListActivity.this, getListView(), toSearch);
                }
                return false;
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

    public ListView getListView(){
        return listView;
    }

    public String getSearchText(){
        return searchBar.getText().toString();
    }
}
