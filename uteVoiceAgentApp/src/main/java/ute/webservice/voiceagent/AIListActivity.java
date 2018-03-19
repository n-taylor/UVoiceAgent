package ute.webservice.voiceagent;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

/**
 * Created by u0450254 on 3/13/2018.
 */

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.TextView;
import android.widget.Toast;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import ai.api.android.AIConfiguration;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.ui.AIButton;
import ai.api.ui.SoundLevelCircleDrawable;

public class AIListActivity extends BaseActivity implements AIButton.AIButtonListener, RetrievalListener {

    private static String TAG = AIListActivity.class.getName();

    ListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    
    private Button cancelButton;
    private AIButton aiButton;
    private TextView queryTextView;

    private DataAsked dataAsked;
    private ParseResult PR;

    SharedData sessiondata;
    private String accountID;
    private int account_access;

    //CA variables
    private CertificateFactory cf = null;
    private Certificate ca;
    private SSLContext sslContext = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aibutton_sample_accordion);

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.resultListView);

        // preparing list data
        dummyListData();

        expListView.setBackgroundResource(R.drawable.menushape);

        listAdapter = new ListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        aiButton = (AIButton) findViewById(R.id.micButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        queryTextView = (TextView) findViewById(R.id.querytextView);

        //Open shared data
        sessiondata = new SharedData(getApplicationContext());
        accountID = sessiondata.getKeyAccount();
        account_access = sessiondata.getKeyAccess();

        //Set up action bar by toolbar
        Toolbar settintTB= (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(settintTB);

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

        this.loadCA();

    }

    private void loadCA(){
        System.out.println("working:"+System.getProperty("user.dir"));
        // Load CAs from an InputStream
        // (could be from a resource or ByteArrayInputStream or ...)
        //CertificateFactory cf = null;
        try {
            cf = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        // From https://www.washington.edu/itconnect/security/ca/load-der.crt
        InputStream caInput = null;
        try {
            caInput = new BufferedInputStream(this.getBaseContext().getAssets().open("ca.cer"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
        } catch (CertificateException e) {
            e.printStackTrace();
        } finally {
            try {
                caInput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance(keyStoreType);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        try {
            keyStore.load(null, null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        try {
            keyStore.setCertificateEntry("ca", ca);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = null;
        try {
            tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            tmf.init(keyStore);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        // Create an SSLContext that uses our TrustManager

        try {
            sslContext = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            sslContext.init(null, tmf.getTrustManagers(), null);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    /*
     * Preparing the list data
     */
    private void dummyListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add("North");
        listDataHeader.add("South");
        listDataHeader.add("East");
        listDataHeader.add("West");

        // Adding child data

        List<String> north = new ArrayList<String>();
        north.add("one ");
        north.add("Two");
        north.add("Three");

        List<String> south = new ArrayList<String>();
        south.add("one ");
        south.add("Two");
        south.add("Three");

        List<String> east = new ArrayList<String>();
        east.add("one ");
        east.add("Two");
        east.add("Three");

        List<String> west = new ArrayList<String>();
        west.add("one ");
        west.add("Two");
        west.add("Three");

        listDataChild.put(listDataHeader.get(0), north);
        listDataChild.put(listDataHeader.get(1), south);
        listDataChild.put(listDataHeader.get(2), east);
        listDataChild.put(listDataHeader.get(3), west);
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

                // Retrieve the information and display the results
                RetrieveTask httpTask = new RetrieveTask(dataAsked, sslContext); // the task to retrieve the information
                httpTask.addListener(AIListActivity.this);
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
            intent.putExtra("query", PR.get_ResolvedQuery());
            intent.putExtra("result", result);
            startActivity(intent);
        }
    }
}
