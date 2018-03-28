package ute.webservice.voiceagent.procedures.util;

import java.util.ArrayList;

/**
 * An interface required by any Activity that will use ProcedureCategoryRetrieveTask.
 * Created by Nathan Taylor on 3/21/2018.
 */
public interface ProcedureCategoryRetrievalListener {

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
