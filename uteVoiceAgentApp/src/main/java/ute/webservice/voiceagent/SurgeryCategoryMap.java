package ute.webservice.voiceagent;

import java.util.ArrayList;
import java.util.Map;

/**
 * Defines the ArrayList of categories, subcategories and specific surgery types.
 * Created by Nathan Taylor on 3/21/2018.
 */

public class SurgeryCategoryMap {

    private ArrayList<String> categories;
    private Map<String, ArrayList<String>> subcategories;
    private Map<String, ArrayList<String>> extremities;

    public SurgeryCategoryMap(ArrayList<String> categories, Map<String, ArrayList<String>> subcategories,
                              Map<String, ArrayList<String>> surgeries){
        this.categories  = categories;
        this.subcategories = subcategories;
        this.extremities = surgeries;
    }

    public ArrayList<String> getCategories(){return categories;}

    public Map<String, ArrayList<String>> getSubcategories(){return subcategories;}

    public Map<String, ArrayList<String>> getSurgeries(){return extremities;}
}
