package ute.webservice.voiceagent.procedures.util;

/**
 * The interface that allows a subscriber to receive the results of a surgery cost retrieval.
 * Created by Nathan Taylor on 3/22/2018.
 */

public interface ProcedureCostRetrievalListener {
    void onCostRetrieval(int cost, String description);
}
