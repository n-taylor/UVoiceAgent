package ute.webservice.voiceagent;

import android.os.AsyncTask;

import java.util.ArrayList;

/**
 * This task, when executed, retrieves all the categories, subcategories and specific surgery types.
 * To get the results, the caller must implement SurgeryCategoryRetrievalListener.
 *
 * Here is an example of how to use this class:
 *
 * public class Example implements SurgeryCategoryRetrievalListener {
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

    private ArrayList<SurgeryCategoryRetrievalListener> listeners;

    @Override
    protected String doInBackground(Void... voids) {
        return null;
    }

    @Override
    protected void onPostExecute(String result){
        
    }

    /**
     * Subscribes the given listener to receive the results of a webservice call.
     * @param listener The listener to subscribe.
     */
    public void addListener(SurgeryCategoryRetrievalListener listener){
        listeners.add(listener);
    }
}
