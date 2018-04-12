package ute.webservice.voiceagent.dao;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Nathan Taylor on 4/11/2018.
 */

public interface OnCallDAO {
    /**
     * Given an OCMID, maps all of the on-call assignments' names to their available phone numbers.
     * @param OCMID
     * @return
     */
    HashMap<String, ArrayList<String>> getPhoneNumbers(String OCMID);
}
