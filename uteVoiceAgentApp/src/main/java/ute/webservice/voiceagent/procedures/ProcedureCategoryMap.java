package ute.webservice.voiceagent.procedures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines the ArrayList of categories, subcategories and specific surgery types.
 * Created by Nathan Taylor on 3/21/2018.
 */

public class ProcedureCategoryMap {

    private ArrayList<String> categories;
    private Map<String, ArrayList<String>> subcategories;
    private Map<String, ArrayList<String>> extremities;

    public ProcedureCategoryMap(ArrayList<String> categories, Map<String, ArrayList<String>> subcategories,
                                Map<String, ArrayList<String>> surgeries){
        this.categories  = categories;
        this.subcategories = subcategories;
        this.extremities = surgeries;
    }

    public ProcedureCategoryMap(ArrayList<String> categories){
        this.categories = categories;
    }

    public ArrayList<String> getCategories(){return categories;}

    public Map<String, ArrayList<String>> getSubcategories(){return subcategories;}

    public Map<String, ArrayList<String>> getSurgeries(){return extremities;}
}
