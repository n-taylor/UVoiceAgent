package ute.webservice.voiceagent.util;

import android.content.Intent;
import android.os.AsyncTask;

import ute.webservice.voiceagent.activities.BaseActivity;
import ute.webservice.voiceagent.activities.LoginActivity;
import ute.webservice.voiceagent.login.LoginAlertDialog;

/**
 * Build one thread to log out.
 * Created by Nathan Taylor 3/14/2018
 */
public class LogoutTask extends AsyncTask<Void,Void,Boolean> {

    private Exception exception;
    private BaseActivity activity;

    public LogoutTask(BaseActivity activity){
        this.activity = activity;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        AccountCheck acnt = new AccountCheck();
        boolean authentication=false;

        try {
            authentication = acnt.logout();
        }
        catch (Exception e){
            e.printStackTrace();
        }
            /*
            for (int i=0; i<2; i++){
                //publishProgress(i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            */
        return authentication;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        //progress.dismiss();
        if(aBoolean){
            //sessiondata.logoutUser();
            final Intent intent = new Intent(activity, LoginActivity.class);
            activity.startActivity(intent);
            //startActivity(LoginActivity.class);
        }
        else{
            LoginAlertDialog alertd= new LoginAlertDialog();
            alertd.showAlertDialog(activity,"Log out fail","time out",null);
            //clearEditText();
        }
    }
}