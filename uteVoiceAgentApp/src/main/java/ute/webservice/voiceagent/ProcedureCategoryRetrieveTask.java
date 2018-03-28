package ute.webservice.voiceagent;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.entity.StringEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * <p>This task, when executed, retrieves all the categories, subcategories and specific surgery types.
 * To get the results, the caller must implement ProcedureCategoryRetrievalListener.</p>
 *
 * <p>Here is an example of how to use this class:</p>
 *
 * public class Example implements ProcedureCategoryRetrievalListener {<br/>
 *
 *     public getCategories(){
 *         ProcedureCategoryRetrieveTask task = new ProcedureCategoryRetrieveTask();
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
public class ProcedureCategoryRetrieveTask extends AsyncTask<Void, Void, String> {

    /**
     * The list of listeners to notify when a result is obtained and processed.
     */
    private ArrayList<ProcedureCategoryRetrievalListener> listeners;

    private ParseResult PR;

    public ProcedureCategoryRetrieveTask(){
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
        ProcedureCategoryMap map = null;
        if (result != null && !result.isEmpty()){
            map = PR.parseCategories(result);
        }

        for (ProcedureCategoryRetrievalListener listener : listeners){
            //listener.onCategoryRetrieval(map.getCategories(), map.getSubcategories(), map.getSurgeries());
            listener.onCategoryRetrieval(map.getCategories());
        }
    }

    /**
     * Subscribes the given listener to receive the results of a webservice call.
     * @param listener The listener to subscribe.
     */
    public void addListener(ProcedureCategoryRetrievalListener listener){
        listeners.add(listener);
    }

}

/**
 * An interface required by any Activity that will use ProcedureCategoryRetrieveTask.
 * Created by Nathan Taylor on 3/21/2018.
 */
interface ProcedureCategoryRetrievalListener {

//    /**
//     * Provides all the categories, subcategories and specific surgery types listed on the server.
//     * @param categories A ArrayList of all the main categories of surgery
//     * @param subCategories A map from each main category of surgery to its subcategory
//     * @param surgeryTypes A map from each subcategory to the specific surgery type.
//     */
//    void onCategoryRetrieval(ArrayList<String> categories, Map<String, ArrayList<String>> subCategories,
//                             Map<String, ArrayList<String>> surgeryTypes);

    /**
     * Provides all the categories of procedures listed by the webservice.
     * @param categories All surgery category procedures.
     */
    void onCategoryRetrieval(ArrayList<String> categories);
}