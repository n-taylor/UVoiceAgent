package ute.webservice.voiceagent;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;


import com.google.gson.Gson;

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
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import ai.api.AIServiceException;
import ai.api.RequestExtras;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.android.GsonFactory;
import ai.api.model.AIContext;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.ui.AIButton;

/**
 * Show mic button and interact with api.ai.
 */

public class AIButtonActivity extends BaseActivity implements AIButton.AIButtonListener, RetrievalListener {

    public static final String TAG = AIButtonActivity.class.getName();
    private final String openingMessage = "I can give you the cost of a procedure or I can give you the census of a hospital room.";

    private AIButton aiButton;
    private Button cancelButton;
    private TextView resultTextView;
    private TextView queryTextView;

    private Gson gson = GsonFactory.getGson();
    private DataAsked dataasked;
    private ParseResult PR;

    SharedData sessiondata;
    private String accountID;
    private int account_access;
    private boolean cancel = false; // use to cancel the current data retrieval

    //Progress bar
    private ProgressDialog progress;

    //
    private AIDataService aiDataService;

    //CA variables
    private CertificateFactory cf = null;
    private Certificate ca;
    private SSLContext sslContext = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aibutton_sample);

        //TextView
        resultTextView = (TextView) findViewById(R.id.resultTextView);
        resultTextView.setGravity(Gravity.RIGHT);
        queryTextView = (TextView) findViewById(R.id.querytextView);
        aiButton = (AIButton) findViewById(R.id.micButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);

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

        //Save asked query
        dataasked = new DataAsked();

        //Welcome message
        resultTextView.setText(Html.fromHtml("<b>Welcome, "+accountID+"! <br/>" + openingMessage + "</b>"));
        //TTS.setVoice(new Voice("Voice", Voice.QUALITY_VERY_HIGH, Voice.LATENCY_NORMAL, false, ));
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

    private void apiConnect(){
        progress = new ProgressDialog(this);
        progress.setMessage("Connecting...");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(true);
        progress.setProgress(0);
        progress.show();

        final int totalProgressTime = 100;
        final Thread t = new Thread() {
            @Override
            public void run() {
                int jumpTime = 0;

                while(jumpTime < totalProgressTime) {
                    try {
                        sendRequest();
                        sleep(200);
                        jumpTime += 5;
                        progress.setProgress(jumpTime);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();
    }

    /**
     * Try to send dummy text query to the server
     */
    private void sendRequest() {

        final String queryString = "Hello";

        final AsyncTask<String, Void, AIResponse> task = new AsyncTask<String, Void, AIResponse>() {

            private AIError aiError;

            @Override
            protected AIResponse doInBackground(final String... params) {
                final AIRequest request = new AIRequest();
                String query = params[0];
                //String event = params[1];

                if (!TextUtils.isEmpty(query))
                    request.setQuery(query);
                //if (!TextUtils.isEmpty(event))
                //    request.setEvent(new AIEvent(event));
                final String contextString = params[1];
                RequestExtras requestExtras = null;
                if (!TextUtils.isEmpty(contextString)) {
                    final List<AIContext> contexts = Collections.singletonList(new AIContext(contextString));
                    requestExtras = new RequestExtras(contexts, null);
                }

                try {
                    return aiDataService.request(request, requestExtras);
                } catch (final AIServiceException e) {
                    aiError = new AIError(e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final AIResponse response) {
                if (response != null) {
                    onResult(response);
                } else {
                    onError(aiError);
                }
            }
        };

        task.execute(queryString);
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
                queryTextView.setText(query);

//                RetrieveTask httpTask = new RetrieveTask();
//                httpTask.execute();

                // Retrieve the information and display the results
                RetrieveTask httpTask = new RetrieveTask(dataasked, sslContext); // the task to retrieve the information
                httpTask.addListener(AIButtonActivity.this);
                httpTask.execute();


                dataasked.setIncomplete(PR.get_ActionIncomplete());
                dataasked.setCurrentReply(PR.get_reply());
                dataasked.setCensusUnit(PR.getCensusUnit());
                dataasked.setCurrentSurgeryCategory(PR.get_param_Surgery());
                dataasked.setCurrentAction(PR.get_Action());
                Log.d("OUTPUTRESPONSE", PR.get_reply());
            }

        });
    }

    /**
     *
     * @param result the result of what what retrieved from the server
     */
    public void onRetrieval(String result){
        TextView resultTV_insync = (TextView) findViewById(R.id.resultTextView); // text view to display the results in
        if(result != null){
            resultTV_insync.setText(result); // display the results
            TTS.speak(dataasked.getVoiceMessageFormat(result)); // play the audio
        }
    }

    @Override
    public void onError(final AIError error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onError");
                resultTextView.setText(error.toString());
            }
        });
    }

    @Override
    public void onCancelled() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onCancelled");
                resultTextView.setText("");
            }
        });
    }

    /**
     * When the back button is pressed, display the opening message and hide the last query
     */
    @Override
    public void onBackPressed(){
        displayOpeningMessage();
    }

    /**
     * Displays the opening message in the resultTextView and clears the queryTextView
     */
    private void displayOpeningMessage(){
        this.resultTextView.setText(Html.fromHtml("<b>" + openingMessage + "</b>"));
        this.queryTextView.setText("");
    }

    // Deprecated way to retrieve information from the server. Use RetrieveTask instead.

//    /**
//     * Create AsyncTask thread to send query to serve and display response.
//     */
//    class RetrieveFeedTask extends AsyncTask<Void,Integer,String> {
//
//        private Exception exception;
//
//        @Override
//        protected String doInBackground(Void... voids) {
//            String data=null;
//            try {
//                data = dataasked.getHttpClientReply(sslContext);
//            } catch (Exception e) {
//                this.exception = e;
//            }
//            return data;
//        }
//
//        @Override
//        protected void onPostExecute(String str){
//            TextView resultTV_insync = (TextView) findViewById(R.id.resultTextView);
//            Log.d(TAG,str);
//            if(str!=null){
//                resultTV_insync.setText(str);
//                TTS.speak(dataasked.getVoiceMessageFormat(str));
//            }
//        }
//
//    }


    // Deprecated method of logging out. Access the class LogoutTask in voiceagent to log out

//    /**
//     * Build one thread to log out.
//     */
//    class LogoutTask extends AsyncTask<Void,Void,Boolean> {
//
//        private Exception exception;
//        private AccountCheck acnt;
//
//        @Override
//        protected Boolean doInBackground(Void... voids) {
//            acnt= new AccountCheck();
//            boolean authentication=false;
//
//            try {
//                authentication = acnt.logout();
//            }
//            catch (Exception e){
//                e.printStackTrace();
//            }
//            /*
//            for (int i=0; i<2; i++){
//                //publishProgress(i);
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//            */
//            return authentication;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean aBoolean) {
//            super.onPostExecute(aBoolean);
//            //progress.dismiss();
//            if(aBoolean){
//                sessiondata.logoutUser();
//                final Intent intent = new Intent(AIButtonActivity.this, LoginActivity.class);
//                startActivity(intent);
//                //startActivity(LoginActivity.class);
//            }
//            else{
//                LoginAlertDialog alertd= new LoginAlertDialog();
//                alertd.showAlertDialog(AIButtonActivity.this,"Log out fail","time out",null);
//                //clearEditText();
//            }
//        }
//    }
}
