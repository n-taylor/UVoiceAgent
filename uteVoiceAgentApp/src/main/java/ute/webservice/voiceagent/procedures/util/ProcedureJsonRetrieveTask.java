package ute.webservice.voiceagent.procedures.util;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.entity.StringEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import ute.webservice.voiceagent.util.AccountCheck;
import ute.webservice.voiceagent.util.Constants;
import ute.webservice.voiceagent.util.ParseResult;


/**
 * This task sends a GET request to the server for the code and description of a surgery.
 * To get the information of a surgery, pass String params {CATEGORY}, {SUBCATEGORY}, {EXTREMITY}.
 *
 * To get the results, the caller must implement ProcedureCategoryRetrievalListener.
 *
 * Here is an example of how to use this class:
 *
 * public class Example implements ProcedureJsonRetrievalListener {
 *
 *     public getCodes(){
 *         ProcedureJsonRetrieveTask task = new ProcedureJsonRetrieveTask();
 *         task.addListener(this);
 *         task.execute();
 *     }
 *
 *     public void onCodeRetrieval(String jsonString){
 *          ParseResult PR = new ParseResult();
 *          PR.parseProcedures(jsonString);
 *     }
 * }
 * Created by Nathan Taylor on 3/22/2018.
 */

public class ProcedureJsonRetrieveTask extends AsyncTask<String, Void, String> {

    private ArrayList<ProcedureJsonRetrievalListener> listeners;
    private ParseResult PR;

    public ProcedureJsonRetrieveTask(){
        PR = new ParseResult();
        listeners = new ArrayList<>();
    }

    /**
     * Gets the surgery types and their codes.
     * The parameters must be of length three, containing the category, subCategory and extremity in that order.
     * @return The response from the server as a Json string
     */
    @Override
    protected String doInBackground(String... strings) {
        if (strings.length < 1 || strings.length > 3)
            return null;
        return getJsonSurgeries(strings);
    }

    /**
     * Returns that which was retrieved from the server.
     * @param result
     */
    @Override
    protected void onPostExecute(String result){
//        HashMap<String, String> codes = PR.parseSurgeryCodes(result);
//        for (ProcedureJsonRetrievalListener listener : listeners){
//            listener.onCodeRetrieval(codes);
//        }
        for (ProcedureJsonRetrievalListener listener : listeners){
            listener.onCodeRetrieval(result);
        }
    }

    /**
     * Sends a GET request to the webservice for the category, subCategory and extremity specified.
     * @param strings The 1. Category, 2. Subcategory and 3. Extremity of the procedures to query
     * @return
     */
    private String getJsonSurgeries(String... strings){
        try{
            String responseString = "";
            String category = (strings.length > 0) ? strings[0] : "";
            String subCategory = (strings.length > 1) ? strings[1] : "";
            String extremity = (strings.length > 2) ? strings[2] : "";
            String queryString = Constants.CLINWEB_SURGERY_CODES_QUERY + ((!category.isEmpty()) ? "/" + category : "");
            queryString += ((!subCategory.isEmpty()) ? "/" + subCategory : "");
            queryString += ((!extremity.isEmpty()) ? "/" + extremity : "");
            queryString = queryString.replace(" ", "%20");

            HttpGetHC4 getRequest = new HttpGetHC4(queryString);
            CloseableHttpResponse response = AccountCheck.httpclient.execute(getRequest);
            HttpEntity entity = response.getEntity();
            if (entity != null){
                BufferedReader rdSrch = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));

                String lineSrch;
                while ((lineSrch = rdSrch.readLine()) != null) {
                    responseString += lineSrch;
                }

                if (responseString.equals(Constants.ACCESS_DENIED)) {
                    responseString = "You are not allowed to access.";
                }
                rdSrch.close();
            }
            response.close();
            return responseString;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Subscribes the given listener to receive the results of the GET call.
     * @param listener
     */
    public void addListener(ProcedureJsonRetrievalListener listener){
        listeners.add(listener);
    }
}

