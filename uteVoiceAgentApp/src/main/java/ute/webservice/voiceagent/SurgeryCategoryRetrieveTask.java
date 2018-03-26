package ute.webservice.voiceagent;

import android.accounts.Account;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPostHC4;
import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.entity.StringEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * <p>This task, when executed, retrieves all the categories, subcategories and specific surgery types.
 * To get the results, the caller must implement SurgeryCategoryRetrievalListener.</p>
 *
 * <p>Here is an example of how to use this class:</p>
 *
 * public class Example implements SurgeryCategoryRetrievalListener {<br/>
 *
 *     public getCategories(){
 *         SurgeryCategoryRetrieveTask task = new SurgeryCategoryRetrieveTask();
 *         task.addListener(this);
 *         task.execute();
 *     }
 *
 *     public void onCategoryRetrieval(List<String> categories, Map<String, List<String>> subCategories,
 *          Map<String, List<String>> surgeryTypes){
 *          for (String category : categories){
 *              System.out.println(category + "\n");
 *          }
 *     }
 * }
 *
 * Created by Nathan Taylor on 3/21/2018.
 */
public class SurgeryCategoryRetrieveTask extends AsyncTask<Void, Void, String> {

    /**
     * The list of listeners to notify when a result is obtained and processed.
     */
    private ArrayList<SurgeryCategoryRetrievalListener> listeners;

    private ParseResult PR;

    public SurgeryCategoryRetrieveTask(){
        listeners = new ArrayList<>();
        PR = new ParseResult();
    }

    /**
     * Sends a GET request to the server to get a JSON response containing all the categories,
     * subcategories and specific surgery types.
     * @return The JSON string containing the response.
     */
    @Override
    protected String doInBackground(Void... voids) {
        try{
            String responseString = "";
            HttpGetHC4 getRequest = new HttpGetHC4(Constants.CLINWEB_SURGERY_CATEGORIES_QUERY);
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

    @Override
    protected void onPostExecute(String result){
        SurgeryCategoryMap map = null;
        if (result != null && !result.isEmpty()){
            map = PR.parseCategories(result);
        }

        for (SurgeryCategoryRetrievalListener listener : listeners){
            //listener.onCategoryRetrieval(map.getCategories(), map.getSubcategories(), map.getSurgeries());
            listener.onCategoryRetrieval(map.getCategories());
        }
    }

    /**
     * Subscribes the given listener to receive the results of a webservice call.
     * @param listener The listener to subscribe.
     */
    public void addListener(SurgeryCategoryRetrievalListener listener){
        listeners.add(listener);
    }

}

