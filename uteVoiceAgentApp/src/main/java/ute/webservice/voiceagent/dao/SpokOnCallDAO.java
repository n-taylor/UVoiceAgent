package ute.webservice.voiceagent.dao;

import android.content.Context;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.client.methods.HttpPostHC4;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ute.webservice.voiceagent.oncall.util.SpokParser;
import ute.webservice.voiceagent.openbeds.RoomStatus;
import ute.webservice.voiceagent.util.AccountCheck;
import ute.webservice.voiceagent.util.CertificateManager;
import ute.webservice.voiceagent.util.Constants;
import ute.webservice.voiceagent.util.ParseResult;

/**
 * Created by Nathan Taylor on 4/11/2018.
 */

public class  SpokOnCallDAO implements OnCallDAO {

    private static final String NO_NUMBERS = "No phone numbers found";

    private ParseResult PR;

    public SpokOnCallDAO(){
        PR = new ParseResult();
    }

    private final int timeout = 5000;
    private static String IPAddress = "155.100.69.40";


    /**
     * Given an OCMID, maps all of the on-call assignments' names to their available phone numbers.
     *
     * @param OCMID
     * @return
     */
    @Override
    public HashMap<String, ArrayList<String>> getPhoneNumbers(Context context, String OCMID) {
        if (OCMID.equals("000")){
            HashMap<String, ArrayList<String>> testNumsMap = new HashMap<>();
            ArrayList<String> testNums = new ArrayList<>();
            testNums.add("801-420-8471 : Test Number");
            testNumsMap.put("Nathan", testNums);
            return testNumsMap;
        }
        else if (OCMID.equals("001")){
            HashMap<String, ArrayList<String>> testNumsMap = new HashMap<>();
            ArrayList<String> testNums = new ArrayList<>();
            testNums.add("801-560-0488 : Test Number");
            testNumsMap.put("Aaron", testNums);
            return testNumsMap;
        }
        HashMap<String, String> mids = getMIDs(OCMID); // maps MID -> Name

        if (mids == null)
        {
            return null;
        }
        HashMap<String, ArrayList<String>> numbers = getNumbers(mids);
        return numbers;
    }

    /**
     * Given a mapping of MIDs to Names, makes a call to retrieve the phone numbers associated
     * with the MID.
     * @param mids MID -> Name
     * @return A mapping of Names to phone numbers
     */
    private HashMap<String, ArrayList<String>> getNumbers(HashMap<String, String> mids) {
        try {
            HashMap<String, ArrayList<String>> numbers = new HashMap<>();

            for (String mid : mids.keySet()) {

                HttpPostHC4 postRequest = new HttpPostHC4("https://10.0.2.2:8042/onCall/getNumbers");

                String  JSON_STRING = "{";
                JSON_STRING+= Constants.CODE +":\""+mid+"\"}";
                StringEntity params= new StringEntity(JSON_STRING);

                postRequest.setEntity(params);
                postRequest.setHeader("Accept", "application/json");
                postRequest.setHeader("Content-Type", "application/json;charset=UTF-8");



                CloseableHttpResponse response3 = AccountCheck.httpclient.execute(postRequest);
                HttpEntity entity = response3.getEntity();

                String json = EntityUtils.toString(entity, "UTF-8");
                ArrayList<String> phoneNumbers = new ArrayList<>();

                if (response3.getStatusLine().getStatusCode() == 200) {
                    // If the retrieval was a success
                    JSONObject myObject = new JSONObject(json);
                    String responseString = "";
                    String myObjectString = myObject.getString("numbers");

                    String arrays[] = myObjectString.split("\\|");

                    for (String number : arrays) {
                        number = number.substring(1);
                        String topics[] = number.split("\\]\\[");

                        if (topics[0].length() == 10) {
                            StringBuilder sb = new StringBuilder(topics[0]);
                            sb.insert(6, "-");
                            sb.insert(3, "-");
                            topics[0] = sb.toString();
                        } else if (topics[0].length() == 7) {

                            StringBuilder sb = new StringBuilder(topics[0]);
                            sb.insert(3, "-");
                            topics[0] = sb.toString();
                        }

                        if (topics[1].equals("SEE NOTE BELOW")) {
                            break;
                        }


                        String phoneNumber = topics[1] + ": " + topics[0];


                        if (!phoneNumbers.contains(phoneNumber)) {

                            phoneNumbers.add(phoneNumber);
                        }
                    }
                }


                phoneNumbers = appendPagers(phoneNumbers, mid);
                if (phoneNumbers.size() == 0){
                    phoneNumbers.add(NO_NUMBERS);
                }
                numbers.put(mids.get(mid), phoneNumbers);
            }

            return numbers;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Given a list of phone numbers, uses the given socket to make a call to the Spok webservice to retrieve any pager IDs
     * associated with the given mid.
     * @param numbers
     * @return an empty ArrayList<String> if an error occurs
     */
    private ArrayList<String> appendPagers(ArrayList<String> numbers, String mid){
            try {
                    HttpPostHC4 postRequest = new HttpPostHC4("https://10.0.2.2:8042/onCall/getPagers");

                    String  JSON_STRING = "{";
                    JSON_STRING+= Constants.CODE +":\""+mid+"\"}";
                    StringEntity params= new StringEntity(JSON_STRING);

                    postRequest.setEntity(params);
                    postRequest.setHeader("Accept", "application/json");
                    postRequest.setHeader("Content-Type", "application/json;charset=UTF-8");

                    CloseableHttpResponse response3 = AccountCheck.httpclient.execute(postRequest);
                    HttpEntity entity = response3.getEntity();

                    String json = EntityUtils.toString(entity, "UTF-8");
                    JSONObject myObject = new JSONObject(json);
                    String responseString = "";

                    String myObjectString = myObject.getString("numbers");

                    myObjectString = "PAGER: "+ myObjectString.replaceAll("[^\\d.]", "");

                    numbers.add(myObjectString);

            } catch (Exception e) {
                return numbers;
            }

        return numbers;
    }

    /**
     * Given an OCMID, returns a mapping of MID to Name for each on-call employee associated with
     * the OCMID.
     */
    private HashMap<String, String> getMIDs(String OCMID){
        //SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslContext);
        BasicCookieStore cookieStore = new BasicCookieStore();
        HashMap<String,String> toSend = new HashMap<>();
        try {
            HttpPostHC4 postRequest = new HttpPostHC4("https://10.0.2.2:8042/onCall/getMID");

            String  JSON_STRING = "{";
            JSON_STRING+= Constants.CODE +":\""+OCMID+"\"}";
            StringEntity params= new StringEntity(JSON_STRING);


            postRequest.setEntity(params);
            postRequest.setHeader("Accept", "application/json");
            postRequest.setHeader("Content-Type", "application/json;charset=UTF-8");



            CloseableHttpResponse response3 = AccountCheck.httpclient.execute(postRequest);
            HttpEntity entity = response3.getEntity();

            String json = EntityUtils.toString(entity, "UTF-8");
            JSONObject myObject = new JSONObject(json);
            String responseString = "";

            Iterator<?> keys = myObject.keys();



            if (entity != null) {
                while( keys.hasNext() ) {
                    String key = (String)keys.next();
                    if ( myObject.get(key) instanceof JSONObject ) {
                        JSONObject thisKey = (JSONObject) myObject.get(key);
                        toSend.put(thisKey.getString("mid"),thisKey.getString("name"));
                    }
                }
                }


        } catch (Exception e) {
            System.out.println(e);
        }

        return toSend;
    }
}
