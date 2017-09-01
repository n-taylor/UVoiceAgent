package ute.webservice.voiceagent;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPostHC4;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import static ai.api.android.AIDataService.TAG;

/**
 * Connect to server and verify login authentication.
 * Created by u1076070 on 6/29/2017.
 */

public class AccountCheck {
    //TODO: Connect to account database

    private HashMap<String,String> account_map = new HashMap<String, String>();
    private HashMap<String,Integer> admin_map = new HashMap<String, Integer>();

    private String accountID;
    private Constants const_value;
    public AccountCheck(){

        account_map.put("account02","abcd");
        account_map.put("account01","1234");
        account_map.put("admin","admin");

        admin_map.put("admin", Constants.ACCESS_LEVEL_ADMIN);
        admin_map.put("account01", Constants.ACCESS_LEVEL_HIGH);
        admin_map.put("account02", Constants.ACCESS_LEVEL_LOW);
    }

    /**
     * Return current user ID, it may be null.
     * @return user ID
     */
    public String getAccountID() {
        return accountID;
    }

    /**
     * Save current user ID
     * @param inputID
     */
    private void setAccountID(String inputID){
        accountID=inputID;
    }

    /**
     * Return authorization level
     * @return user's authorization
     */
    public int getAccessLevel(){
        if(accountID!=null && !accountID.isEmpty())
            return admin_map.get(accountID);
        return -1;
    }

    /**
     * Connect to account database, and then check if account exist and password is correct.
     * @param ID Input account ID
     * @param password Input password
     * @return return true if all input information is right; else false.
     */
    public boolean isAccountCorrect(String ID, String password){
        if(!account_map.containsKey(ID))
            return false;
        if(!account_map.get(ID).equals(password))
            return false;

        setAccountID(ID);
        return true;
    }

    /**
     * Connect to account database, and then check if account exist and password is correct.
     * @param param
     * @return
     */
    public boolean isAccountCorrect(String[] param){
        if(param.length!=2 || !account_map.containsKey(param[0]))
            return false;
        if(!account_map.get(param[0]).equals(param[1]))
            return false;

        setAccountID(param[0]);
        return true;
    }

    /**
     * Connect to account database, and then check if account exist and password is correct.
     * @param param
     * @return
     */
    public boolean isAuthenticated(String[] param) throws Exception {
        //SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslContext);
        BasicCookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build();
        //System.out.println("set SSL ");
        String responseString="";
        boolean loginSucceed = false;
        try {
            HttpPostHC4 httpPost = new HttpPostHC4(const_value.AUTHENTIC_LINK);
            //Prepare Parameters
            String  JSON_STRING = "{";
            JSON_STRING+=const_value.USER+":\""+param[0]+"\",";
            JSON_STRING+=const_value.PASSWORD+":\""+param[1]+"\"}";
            StringEntity params= new StringEntity(JSON_STRING);
            Log.d(TAG,JSON_STRING);

            httpPost.setEntity(params);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
            try {
                CloseableHttpResponse response3 = httpclient.execute(httpPost);
                HttpEntity entity = response3.getEntity();
                String json = EntityUtils.toString(entity, "UTF-8");
                JSONObject myObject = new JSONObject(json);
                String s = myObject.get("authenticated").toString();

                    if(s.equals("true")){
                    //if(myObject.getBoolean("authenticated")){
                        loginSucceed = true;
                        setAccountID(param[0]);
                    }

            } catch(Exception e){
                e.printStackTrace();
            }
        } finally {
            try {
                httpclient.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }

        return loginSucceed;
    }

}
