package ute.webservice.voiceagent.procedures.util;

/**
 * The interface that allows access to the results of a ProcedureJsonRetrieveTask.
 * Created by Nathan Taylor on 3/22/2018.
 */

public interface ProcedureJsonRetrievalListener {
    //void onCodeRetrieval(HashMap<String, String> surgeries);

    void onCodeRetrieval(String jsonString);
}
