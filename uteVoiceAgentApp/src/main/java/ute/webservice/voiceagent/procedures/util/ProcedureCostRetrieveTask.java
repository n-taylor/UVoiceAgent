package ute.webservice.voiceagent.procedures.util;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGetHC4;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import ute.webservice.voiceagent.util.AccountCheck;
import ute.webservice.voiceagent.util.Constants;
import ute.webservice.voiceagent.util.ParseResult;

/**
 * Gets the cost of a given surgery, identified by its code.
 * When calling this class' execute method, only the code and description, in that order, should be
 * passed as String parameters.
 * Created by Nathan Taylor on 3/22/2018.
 */

public class ProcedureCostRetrieveTask extends AsyncTask<String, Void, String> {

    private ArrayList<ProcedureCostRetrievalListener> listeners;
    private ParseResult PR;
    private String description;

    public ProcedureCostRetrieveTask(){
        listeners = new ArrayList<>();
        PR = new ParseResult();
        description = "";
    }

    /**
     * Gets the response from the server.
     * @param strings Only the procedure code and description, in that order, should be passed as parameters.
     * @return If there is more or less than one parameter, return null. Otherwise, return the Json String response.
     */
    @Override
    protected String doInBackground(String... strings) {
        if (strings.length != 2)
            return null;
        else {
            this.description = strings[1];
            return getResponse(strings[0]);
        }
    }

    /**
     * Parses the Json string and notifies all listeners. Sends an integer through onCostRetrieval(int cost)
     * @param result The Json string to parse.
     */
    @Override
    protected void onPostExecute(String result){
        int cost = 0;
        if (result.equals(Constants.ACCESS_DENIED))
            cost = Constants.ACCESS_DENIED_INT;
        else
            cost = PR.parseSurgeryCost(result);
        for (ProcedureCostRetrievalListener listener : listeners){
            listener.onCostRetrieval(cost, description);
        }
    }

    /**
     * Gets the response from the server.
     * @param code The procedure code.
     * @return The response as a Json string.
     */
    private String getResponse(String code){
        try{
            String responseString = "";
            String queryString = Constants.CLINWEB_SURGERY_COST_BY_CODE_QUERY + "/" + code;

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
                    return Constants.ACCESS_DENIED;
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
     * Subscribes a ProcedureCostRetrievalListener to receive the results of the retrieval.
     * @param listener The subscriber.
     */
    public void addListener(ProcedureCostRetrievalListener listener){
        listeners.add(listener);
    }
}

