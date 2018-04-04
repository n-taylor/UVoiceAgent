package ute.webservice.voiceagent.oncall.util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Nathan Taylor on 4/4/2018.
 */

public interface OnCallRetrievalListener {
    void onOnCallRetrieval(HashMap<String, ArrayList<String>> numbers);
}
