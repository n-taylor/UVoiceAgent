package ai.api.sample;

import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import ai.api.android.GsonFactory;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;
import ai.api.model.Status;

import java.util.HashMap;
/**
 * Created by u1076070 on 5/10/2017.
 */

public class ParseResult {

    static final String intent_yes = "ReplyYes";
    static final String intent_unknown = "Default Fallbck Intent";
    static final String intent_sq = "surgery question";

    static final String param_surgery = "Surgery";
    static final String param_question_type = "Question-type";
    class response {

    }

    private String speech;
    private Gson gson = GsonFactory.getGson();
    private AIResponse response = null;

    private Result result = null;
    private Status status = null;
    private Metadata metadata = null;
    private HashMap<String, JsonElement> params;

    ParseResult(AIResponse received_response) {
        this.response = received_response;
        this.result = this.response.getResult();
        this.status = this.response.getStatus();
        this.metadata = this.result.getMetadata();
        this.params = this.result.getParameters();
    }

    public String get_reply() {
        /* display cost of surgery */
        return result.getFulfillment().getSpeech();
    }

    public String get_ResolvedQuery() {
        return result.getResolvedQuery();
    }

    public String get_Action(){
        return result.getAction();
    }

    public String get_IntentName(){
        return this.metadata.getIntentName();
    }

    /*
    Check replied intents
     */
    public boolean reply_unknown(){
        if(this.get_IntentName().equals(intent_unknown))
            return true;
        return false;
    }

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
            return param_json.substring(1,param_json.length()-1);
        }
        return "";
        /*
        if (params != null && !params.isEmpty()) {
            Log.i(TAG, "Parameters: ");
            for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                Log.i(TAG, String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
            }
        }*/
    }

    public String get_param_q_type(){
        if (params != null && params.containsKey(param_question_type))
        {
            String param_json = params.get(param_question_type).toString();
            return param_json.substring(1,param_json.length()-1);
            //return params.get(param_question_type).toString();
        }
        return "";
    }
    /*
    private void parse_response(){
        /* get string item from response */
        //Result result = response.getResult();
        //this.speech = result.getFulfillment().getSpeech();
    //}

}
