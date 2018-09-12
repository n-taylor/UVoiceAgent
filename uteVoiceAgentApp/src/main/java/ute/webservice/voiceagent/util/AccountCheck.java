package ute.webservice.voiceagent.util;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.client.methods.HttpPostHC4;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import ute.webservice.voiceagent.activities.BaseActivity;

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
    private static final int timeout = 10000; // 10 seconds

    private static final String TAG = "AccountCheck";

    private String accountID;
    public AccountCheck(){
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


    private void initializeHttpClient(BaseActivity activity){

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();

        cookieStore = new BasicCookieStore();
        httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setSslcontext(CertificateManager.getSSlContext(activity, "server.crt"))
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
                Object myObject = new JSONObject(json).get("authenticated");
             int statusCode  = response3.getStatusLine().getStatusCode();

                    if(statusCode == 200 && myObject!=null && myObject.toString().equals("true")){

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
