package ute.webservice.voiceagent.dao;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Nathan Taylor on 4/11/2018.
 */

public interface OpenBedsDAO {
    int getOpenBedCount(String unit);
    HashMap<String, Integer> getAllOpenBedCounts();
    ArrayList<String> getAllUnitNames();
    HashMap<String, ArrayList<String>> getUnitCategories();
}
