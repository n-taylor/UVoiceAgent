package ute.webservice.voiceagent;

/**
 * The interface that allows a subscriber to receive the results of a surgery cost retrieval.
 * Created by Nathan Taylor on 3/22/2018.
 */

public interface SurgeryCostRetrievalListener {
    void onCostRetrieval(int cost, String description);
}
