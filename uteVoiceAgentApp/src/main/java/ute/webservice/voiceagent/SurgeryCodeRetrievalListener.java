package ute.webservice.voiceagent;

import java.util.HashMap;

/**
 * The interface that allows access to the results of a SurgeryCodeRetrieveTask.
 * Created by Nathan Taylor on 3/22/2018.
 */

public interface SurgeryCodeRetrievalListener {
    //void onCodeRetrieval(HashMap<String, String> surgeries);

    void onCodeRetrieval(String jsonString);
}
