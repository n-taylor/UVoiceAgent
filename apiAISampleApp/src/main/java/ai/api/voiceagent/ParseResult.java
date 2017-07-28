package ai.api.voiceagent;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import ai.api.android.GsonFactory;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;
import ai.api.model.Status;

import java.util.HashMap;
/**
 * Handle response from API.AI, and save parameters temporarily.
 * Created by u1076070 on 5/10/2017.
 */

public class ParseResult {

    //intent names, which matched to the names on API.AI.
    //TODO: load intent name from file/Constants, since this table may grow tremendously.
    static final String intent_yes = "ReplyYes";
    static final String intent_unknown = "Default Fallbck Intent";
    static final String intent_sq = "surgery question";

    //parameters
    static final String param_surgery = "surgery";
    static final String param_question_type = "questionType";

    private String speech;
    private Gson gson = GsonFactory.getGson();
    private AIResponse response = null;

    private Result result = null;
    private Status status = null;
    private Metadata metadata = null;
    private HashMap<String, JsonElement> params;

    /**
     * Initialize class by received response.
     * @param received_response
     */
    ParseResult(AIResponse received_response) {
        this.response = received_response;
        this.result = this.response.getResult();
        this.status = this.response.getStatus();
        this.metadata = this.result.getMetadata();
        this.params = this.result.getParameters();
    }

    /**
     * Get replied sentences from API.AI.
     * @return replied sentences
     */
    public String get_reply() {
        /* display cost of surgery */
        return result.getFulfillment().getSpeech();
    }

    /**
     * Get user query, API.AI use google speech recognition system.
     * @return User query
     */
    public String get_ResolvedQuery() {
        return result.getResolvedQuery();
    }

    /**
     * Get action name, which is defined on API.AI server.
     * @return action name
     */
    public String get_Action(){
        return result.getAction();
    }

    /**
     * Get status of action.
     * @return if all parameters are saved, return true, else false.
     */
    public boolean get_ActionComplete(){
        return result.isActionIncomplete();
    }

    /**
     * Get intent name, which is defined on API.AI agent.
     * @return  intent name
     */
    public String get_IntentName(){
        return this.metadata.getIntentName();
    }

    /**
     * If the input query can not be recognized.
     * @return
     */
    public boolean reply_unknown(){
        if(this.get_IntentName().equals(intent_unknown))
            return true;
        return false;
    }

    /**
     * If last query is 'Yes' or not.
     * @return true if user said yes, else false.
     */
    public boolean reply_yes(){
        if(this.get_IntentName().equals(intent_yes))
            return true;
        return false;
    }

    public boolean reply_sq(){
        if(this.get_IntentName().equals(intent_sq))
            return true;
        return false;
    }
    /*

     */
    public String get_param_Surgery(){
        if (params != null && params.containsKey(param_surgery))
        {
            String param_json = params.get(param_surgery).toString();
            //return param_json.substring(1,param_json.length()-1);
            return param_json;
        }
        return "";
    }

    public String get_param_q_type(){
        if (params != null && params.containsKey(param_question_type))
        {
            String param_json = params.get(param_question_type).toString();
           // return param_json.substring(1,param_json.length()-1);
            return param_json;
        }
        return "";
    }

}
