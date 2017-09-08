package ute.webservice.voiceagent;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ProgressBar;

/**
 * Login screen, after click login button, it will verify authentication.
 */
public class MainActivity extends BaseActivity {

    //Progress bar
    //private ProgressDialog progress;
    private boolean connectionInComplete = true;

    private EditText idEditText;
    private EditText passwordEditText;
    SharedData sessiondata;

    public static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessiondata = new SharedData(getApplicationContext());
        idEditText = (EditText) findViewById(R.id.inputAccount);
        passwordEditText = (EditText) findViewById(R.id.inputPassword);
        TTS.init(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();

        checkAudioRecordPermission();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        final int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(AISettingsActivity.class);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Check input ID and password, if both are correct, jump to {@link AIButtonActivity}
     * @param view
     */
    public void buttonSampleClick(final View view) {
        final String idString = String.valueOf(idEditText.getText());
        final String pwString = String.valueOf(passwordEditText.getText());

        //AccountCheck acnt= new AccountCheck();
        if (TextUtils.isEmpty(idString) || TextUtils.isEmpty(pwString)) {
            LoginAlertDialog alertd= new LoginAlertDialog();
            alertd.showAlertDialog(MainActivity.this,"Login fail","Please enter your username and password.",null);
            clearEditText();
            return ;
        }

        AuthenticationTask httpTask = new AuthenticationTask();
        try{
            httpTask.execute(idString,pwString);
        }
        catch (IllegalStateException e){
            e.printStackTrace();
        }

        /*
        if(acnt.isAccountCorrect(idString, pwString)){
            sessiondata.createLoginSession(idString,acnt.getAccessLevel());
            //apiConnect();
            //while(connectionInComplete);
            startActivity(AIButtonActivity.class);
        }
        //else{
            LoginAlertDialog alertd= new LoginAlertDialog();
            alertd.showAlertDialog(MainActivity.this,"Login fail","Account does not exist or password is incorrect.",null);
            clearEditText();
        //}
        //*/
        return ;
    }

    /**
     * Create progress dialog and until get respond from webservice server.
     */
    /*
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
                        //sendRequest();
                        sleep(200);
                        jumpTime += 5;
                        progress.setProgress(jumpTime);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                startActivity(AIButtonActivity.class);
            }
        };
        t.start();
    }
    */

    /**
     * Enter another activity.
     * @param cls
     */
    private void startActivity(Class<?> cls) {
        final Intent intent = new Intent(this, cls);
        startActivity(intent);
    }

    private void clearEditText() {
        idEditText.setText("");
        passwordEditText.setText("");
    }

    class AuthenticationTask extends AsyncTask<String,Integer,Boolean> {

        private Exception exception;
        private AccountCheck acnt;
        private String idString;
        private ProgressDialog progress;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(MainActivity.this);
            progress.setMessage("Connecting...");
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setIndeterminate(false);
            //progress.setProgress(0);
            progress.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            acnt= new AccountCheck();
            idString = strings[0];
            //pwString = strings[0][1];
            boolean authentication=false;
            //authentication = acnt.isAccountCorrect(strings);

            try {
                authentication = acnt.isAuthenticated(strings);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            for (int i=0; i<2; i++){
                    publishProgress(i);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            return authentication;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progress.setProgress(values[0]*20);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progress.dismiss();
            if(aBoolean){
                //MainActivity.this.sessiondata.createLoginSession(acnt.getAccountID(),acnt.getAccessLevel());
                MainActivity.this.sessiondata.createLoginSession(acnt.getAccountID(),2);
                startActivity(AIButtonActivity.class);
            }
            else{
                LoginAlertDialog alertd= new LoginAlertDialog();
                alertd.showAlertDialog(MainActivity.this,"Login fail","Account does not exist or password is incorrect.",null);
                clearEditText();
            }
        }
    }
}
