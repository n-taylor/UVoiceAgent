package ute.webservice.voiceagent;

import java.util.List;
import java.util.Map;

/**
 * An interface required by any Activity that will use SurgeryCategoryRetrieveTask.
 * Created by Nathan Taylor on 3/21/2018.
 */
public interface SurgeryCategoryRetrievalListener {

    /**
     * Provides all the categories, subcategories and specific surgery types listed on the server.
     * @param categories A list of all the main categories of surgery
     * @param subCategories A map from each main category of surgery to its subcategory
     * @param surgeryTypes A map from each subcategory to the specific surgery type.
     */
    void onCategoryRetrieval(List<String> categories, Map<String, List<String>> subCategories,
                             Map<String, List<String>> surgeryTypes);
}
