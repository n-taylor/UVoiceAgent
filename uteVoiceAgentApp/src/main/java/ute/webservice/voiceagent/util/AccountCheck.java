package ute.webservice.voiceagent.util;

import android.util.Log;

import org.apache.http.HttpClientConnection;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.client.methods.HttpPostHC4;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ute.webservice.voiceagent.activities.BaseActivity;
import ute.webservice.voiceagent.util.Constants;

//import static ai.api.android.AIDataService.TAG;

/**
 * Connect to server and verify login authentication.
 * Created by u1076070 on 6/29/2017.
 */

public class AccountCheck {
    //TODO: Connect to account database
    //SharedData sessiondata = new SharedData(getApplicationContext());
    public static CloseableHttpClient httpclient = null;
    public static BasicCookieStore cookieStore;
    private static final int timeout = 1000;

    private static final String TAG = "AccountCheck";
    public static final String PEER_CERTIFICATES = "PEER_CERTIFICATES";

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


    private void initializeHttpClient(BaseActivity activity){

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();

        cookieStore = new BasicCookieStore();
        httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setSslcontext(CertificateManager.getSSlContext(activity, "emulator.crt"))
                .setDefaultRequestConfig(config)
                .build();
    }

    /**
     * Connect to account database, and then check if account exist and password is correct.
     * @param param
     * @return
     */
    public boolean isAuthenticated(String[] param, BaseActivity activity) throws Exception {
        //SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslContext);
        //System.out.println("set SSL ");

        initializeHttpClient(activity);
        String responseString="";
        boolean loginSucceed = false;
        try {

            HttpPostHC4 httpPost = new HttpPostHC4(Constants.AUTHENTIC_LINK);
            //Prepare Parameters
            String  JSON_STRING = "{";
            JSON_STRING+= Constants.USER +":\""+param[0]+"\",";
            JSON_STRING+= Constants.PASSWORD +":\""+param[1]+"\"}";
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
             int statusCode  = response3.getStatusLine().getStatusCode();

                    if(statusCode == 200){

                        loginSucceed = true;
                        setAccountID(param[0]);
                        List<Cookie> cookies = cookieStore.getCookies();
                        if (cookies.isEmpty()) {
                            Log.d(TAG,"None");
                        } else {
                            for (int i = 0; i < cookies.size(); i++) {
                                Log.d(TAG,"- " + cookies.get(i).toString());
                            }
                        }
                        //save cookies
                        //sessiondata.saveCookies(cookies.get(0).toString());
                        //Header[] mCookies = response3.getHeaders("cookie");

                    }

            } catch(Exception e){
                e.printStackTrace();
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return loginSucceed;
    }

    public boolean logout() {
        BasicCookieStore cookieStore = new BasicCookieStore();
        //CloseableHttpClient httpclient = HttpClients.custom()
        /*
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build();
        */
        boolean logoutSucceed = false;
        try {
            HttpGetHC4 httpGet = new HttpGetHC4(Constants.AUTHENTIC_LINK_LOGOUT);

            try {
                CloseableHttpResponse response3 = httpclient.execute(httpGet);
                HttpEntity entity = response3.getEntity();
                String json = EntityUtils.toString(entity, "UTF-8");
                //JSONObject myObject = new JSONObject(json);
                //String s = myObject.get("authenticated").toString();

                if(json.equals("logout")){
                    logoutSucceed = true;
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

        return logoutSucceed;
    }

}
