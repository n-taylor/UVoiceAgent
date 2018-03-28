package ute.webservice.voiceagent.util;

/**
 * The interface for anything that uses RetrieveTask
 * Created by Nathan Taylor on 3/14/2018.
 */
public interface RetrievalListener {

    /**
     * Gets called when the server retrieves the information requested using RetrieveTask.
     * @param result the result of what what retrieved from the server
     */
    void onRetrieval(String result);
}
