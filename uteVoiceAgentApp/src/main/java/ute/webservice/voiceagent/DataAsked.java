package ute.webservice.voiceagent;

import android.content.Context;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPostHC4;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.net.ssl.SSLContext;


/**
 * A Hashmap to save queries and answers which are already asked.
 * Created by u1076070 on 6/14/2017.
 */

public class DataAsked {

    private static final String TAG = "DataAsked";
    private static final String test_url ="https://drcapptest.ad.utah.edu:7443/pricing-transparency-api/pricing/query";
    private static final String test_url_query = "http://drcapptest.ad.utah.edu:7003/pricing-transparency-api/pricing/query";
    private static final String test_url_retrieve = "https://clinweb.med.utah.edu/orscheduling-api/user/retrieve";
    private Constants const_value;

    //TODO: load data from files
    private HashMap<String,HashMap<String,String>> Map_Sugery = new HashMap<String,HashMap<String,String>>();
    private HashMap<String,String> Hernia = new HashMap<String,String>();
    private HashMap<String,String> Bypass = new HashMap<String,String>();

    private String Question_type="";
    private String Surgery_type="";

    private HashMap<Integer,HashSet<String>> Admin_group = new HashMap<Integer,HashSet<String>>();

    // Create a KeyStore containing our trusted CAs
    private String keyStoreType = KeyStore.getDefaultType();
    private KeyStore keyStore;

    /**
     * Initialize surgery database and administration with HashMap
     */
    DataAsked()
    {
        const_value=new Constants();
        //Test: initialize Hashmap by program
        //TODO initialize HashMap by loading file
        Hernia.put("price","6000");
        Hernia.put("success rate","98%");
        Hernia.put("recovery time","18");

        Bypass.put("price","70000");
        Bypass.put("success rate","90%");
        Bypass.put("recovery time","30");

        Map_Sugery.put(const_value.SURGERY_HERNIA,Hernia);
        Map_Sugery.put(const_value.SURGERY_BYPASS,Bypass);

        //Initialize administration group
        Admin_group.put(Constants.ACCESS_LEVEL_ADMIN,new HashSet<>(Arrays.asList(const_value.SURGERY_HERNIA,const_value.SURGERY_BYPASS)));
        Admin_group.put(Constants.ACCESS_LEVEL_HIGH,new HashSet<>(Arrays.asList(const_value.SURGERY_BYPASS)));
        Admin_group.put(Constants.ACCESS_LEVEL_LOW,new HashSet<>(Arrays.asList(const_value.SURGERY_HERNIA)));

        //this.loadCA();
    }

    private void loadCA(){
        System.out.println("working:"+System.getProperty("user.dir"));
        // Load CAs from an InputStream
        // (could be from a resource or ByteArrayInputStream or ...)
        CertificateFactory cf = null;
        try {
            cf = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        // From https://www.washington.edu/itconnect/security/ca/load-der.crt
        InputStream caInput = null;

        try {
            caInput = new BufferedInputStream(new FileInputStream("ca.cer"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Certificate ca;
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
    }

    /**
     * Return true if there are enough parameters.
     * @return true if enought parameters are temporarily saved.
     */
    public boolean isParameter_Enough(){
        return !Question_type.equals("") && !Surgery_type.equals("");
    }

    /**
     * clear saved parameters.
     */
    public void clear_params(){
        this.Question_type = "";
        this.Surgery_type = "";
    }

    /**
     * Save parameters from JSON of api.ai
     * @param question_t question about specific surgery
     * @param surgery_t surgery name
     */
    public void assign_params(String question_t, String surgery_t){
        this.Question_type = question_t;
        this.Surgery_type = surgery_t;
    }

    /**
     * Check if the current user can access data related to his/her question
     * @param access_level user's authorization.
     * @return if accessable, return true.
     */
    public boolean IsAccessable(int access_level){
        return Admin_group.containsKey(access_level) && Admin_group.get(access_level).contains(this.Surgery_type);
    }

    /**
     * Reply to user's query, these are data for test.
     * @return complete respond to user's question
     */
    public String get_info(){
        HashMap<String,String> info= Map_Sugery.get(Surgery_type);
        //info.get(Question_type);
        if(Question_type.equals("price")) {
            return new String("The average "+Question_type+" of "+Surgery_type+" is "+info.get(Question_type)+"$.");
        }
        else if(Question_type.equals("recovery time")){
            return new String("The average "+Question_type+" of "+Surgery_type+" is "+info.get(Question_type)+" days.");
        }
        else{
            return new String("The average "+Question_type+" of "+Surgery_type+" is "+info.get(Question_type)+".");
        }
    }

    /**
     * Get data from webservice by HttpURLConnection.
     * @param params
     * @return data from webservice server.
     * @throws IOException
     */
    public String get_info_html (String... params)throws IOException {

        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String drcapp=test_url_query;
        try {
            URL url = new URL(drcapp);
            connection = (HttpURLConnection) url.openConnection();
            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line+"\n");
                Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)
            }

            return buffer.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Get data from webservice by CloseableHttpClient.
     * @return Respond to user's query.
     * @throws IOException
     */
    public String getHttpClientReply(SSLContext sslContext) throws IOException {
        SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslContext);
                //SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        BasicCookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                //.setSSLSocketFactory(factory)
                .build();
        //HttpParams httpParams = httpclient.getParams();
        //HttpConnectionParams.setConnectionTimeout(httpParams,6000);
        //HttpConnectionParams.setSoTimeout(httpParams,6000);
        System.out.println("set SSL ");
        String responseString="";
        try {
            HttpPostHC4 httpPost = new HttpPostHC4(test_url_retrieve);
            //Prepare Parameters
            String  JSON_STRING = "{";
            JSON_STRING+=const_value.QUESTION_TYPE+":"+this.Question_type+",";
            JSON_STRING+=const_value.SURGERY_TYPE+":"+this.Surgery_type+"}";
            StringEntity params= new StringEntity(JSON_STRING);
            Log.d(TAG,JSON_STRING);

            httpPost.setEntity(params);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
            try {
                CloseableHttpResponse response3 = httpclient.execute(httpPost);
                HttpEntity entity = response3.getEntity();
                if (entity != null) {
                    BufferedReader rdSrch = new BufferedReader(
                            new InputStreamReader(response3.getEntity().getContent()));

                    String  lineSrch;
                    while ((lineSrch = rdSrch.readLine()) != null) {
                        Log.d(TAG, lineSrch);
                        responseString+=lineSrch;
                    }
                    if(this.Question_type.equals("\"price\""))
                        responseString = "The average cost of "+this.Surgery_type.substring(1,this.Surgery_type.length()-1)+
                            " is $"+responseString+".";
                    if(this.Question_type.equals("\"risk\""))
                        responseString = "The average risk of "+this.Surgery_type.substring(1,this.Surgery_type.length()-1)+
                                " is "+responseString+"%.";
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
        this.clear_params();
        return responseString;
    }

}
