package ute.webservice.voiceagent;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import ai.api.android.GsonFactory;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;
import ai.api.model.Status;

import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
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
    static final String param_surgery = "SurgeryCategory";
    static final String param_question_type = "questionType";

    private static Gson gson;
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

        GsonBuilder gBuilder = new GsonBuilder();
        gBuilder.registerTypeAdapter(RoomStatus.class, new RoomStatusDeserializer());
        gBuilder.registerTypeAdapter(SurgeryInfo.class, new SurgeryInfoDeserializer());
        gson = gBuilder.create();

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
        if (result.getAction() != null) {
            return result.getAction();
        }
        else {
            return "";
        }
    }

    /**
     * Get status of action.
     * @return if all parameters are saved, return true, else false.
     */
    public boolean get_ActionIncomplete(){
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
     * @return True if speech input cant be recognized.
     */
    public boolean reply_unknown(){
        return this.get_IntentName().equals(intent_unknown);
    }

    /**
     * If last query is 'Yes' or not.
     * @return true if user said yes, else false.
     */
    public boolean reply_yes(){
        return this.get_IntentName().equals(intent_yes);
    }

    /**
     * Return true if this is a surgery question.
     * @return true if current intent is surgery question.
     */
    public boolean reply_sq(){
        return this.get_IntentName().equals(intent_sq);
    }

    /**
     * Return saved surgery name.
     * @return saved surgery name.
     */
    public String get_param_Surgery(){
        if (params != null && params.containsKey(param_surgery))
        {
            String param_json = params.get(param_surgery).getAsString();
            return param_json;
        }
        return "";
    }

    /**
     * Return question type.
     * @return saved question type
     */
    public String get_param_q_type(){
        if (params != null && params.containsKey(param_question_type))
        {
            String param_json = params.get(param_question_type).toString();
            return param_json;
        }
        return "";
    }

    /**
     * Return saved census unit param.
     * @return census unit.
     */
    public String getCensusUnit(){
        if (params != null && params.containsKey("censusUnit"))
        {
            String param_json = params.get("censusUnit").getAsString();
            return param_json;
        }
        return "";
    }

    /**
     * Return saved compelete param.
     * @return complete.
     */
    public String getComplete(){
        if (params != null && params.containsKey("complete"))
        {
            String param_json = params.get("complete").getAsString();
            return param_json;
        }
        return "";
    }

    /**
     *
     * @return all rooms in an ArrayList
     */
    public static ArrayList<RoomStatus> parseRooms(String jsonRooms) {
        ArrayList<RoomStatus> rooms = new ArrayList<RoomStatus>();
        Type arrayType = new TypeToken<ArrayList<RoomStatus>>(){}.getType();


        rooms = gson.fromJson(jsonRooms, arrayType);
        return rooms;
    }

    public static SurgeryInfo parseSurgery(String jsonSurgery) {
        SurgeryInfo si = null;
        try {
             si = gson.fromJson(jsonSurgery, SurgeryInfo.class);
        } catch (Exception e ) {
            System.out.print("D");
        }

        return si;
    }




}

class RoomStatusDeserializer implements JsonDeserializer<RoomStatus> {
    @Override
    public RoomStatus deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {


        JsonObject jobj = json.getAsJsonObject();

        RoomStatus currRoom = new RoomStatus(
                jobj.get("unit").getAsString(),
                jobj.get("available").getAsInt()
        );

        return currRoom;
    }
}

class SurgeryInfoDeserializer implements JsonDeserializer<SurgeryInfo>{


    @Override
    public SurgeryInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject jobj = json.getAsJsonObject();

        SurgeryInfo currSurgery = new SurgeryInfo(
                jobj.get("description").getAsString().toLowerCase(),
                jobj.get("totalAvgCharges").getAsString()
        );

        return currSurgery;
    }
}