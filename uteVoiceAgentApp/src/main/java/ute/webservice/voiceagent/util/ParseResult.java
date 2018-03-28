package ute.webservice.voiceagent.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;
import ai.api.model.Status;
import ute.webservice.voiceagent.openbeds.RoomStatus;
import ute.webservice.voiceagent.procedures.SurgeryInfo;
import ute.webservice.voiceagent.procedures.ProcedureCategoryMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
    public ParseResult(AIResponse received_response) {
        this.response = received_response;
        this.result = this.response.getResult();
        this.status = this.response.getStatus();
        this.metadata = this.result.getMetadata();
        this.params = this.result.getParameters();

        GsonBuilder gBuilder = new GsonBuilder();
        gBuilder.registerTypeAdapter(RoomStatus.class, new RoomStatusDeserializer());
        gBuilder.registerTypeAdapter(SurgeryInfo.class, new SurgeryInfoDeserializer());
        gBuilder.registerTypeAdapter(ProcedureCategoryMap.class, new ProcedureCategoryDeserializer());
        gson = gBuilder.create();

    }

    /**
     * For use when a call to DialogFlow has not been made. Builds a Gson object to deserialize
     * Surgery Categories.
     */
    public ParseResult(){
        GsonBuilder gBuilder = new GsonBuilder();
        gBuilder.registerTypeAdapter(ProcedureCategoryMap.class, new ProcedureCategoryDeserializer());
        gBuilder.registerTypeAdapter(new TypeToken<HashMap<String, String>>(){}.getType(), new ProcedureCodeDeserializer());
        gBuilder.registerTypeAdapter(new TypeToken<Integer>(){}.getType(), new ProcedureCostDeserializer());
        gBuilder.registerTypeAdapter(new TypeToken<ArrayList<String[]>>(){}.getType(), new ProceduresDeserializer());
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

    /**
     * Parses a Json string containing categories into a ProcedureCategoryMap
     * @param jsonCategories
     * @return
     */
    public ProcedureCategoryMap parseCategories(String jsonCategories){
        ProcedureCategoryMap map = null;
        Type returnType = new TypeToken<ProcedureCategoryMap>(){}.getType();
        try{
            map = gson.fromJson(jsonCategories, returnType);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return map;
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

    public HashMap<String, String> parseSurgeryCodes(String jsonCodes){
        HashMap<String, String> codes = null;
        Type returnType = new TypeToken<HashMap<String, String>>(){}.getType();
        try{
            codes = gson.fromJson(jsonCodes, returnType);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return codes;
    }

    public Integer parseSurgeryCost(String jsonCost){
        Integer cost = 0;
        Type returnType = new TypeToken<Integer>(){}.getType();
        try{
            cost = gson.fromJson(jsonCost, returnType);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return cost;
    }

    public ArrayList<String[]> parseAllProcedures(String jsonProcedures){
        ArrayList<String[]> list = null;
        Type type = new TypeToken<ArrayList<String[]>>(){}.getType();
        try{
            list = gson.fromJson(jsonProcedures, type);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return list;
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
        System.out.println("getting here");
        byte data[] = currRoom.getUnit().getBytes();

        File newFile = new File("results.txt");
        try {
            FileOutputStream out = new FileOutputStream(newFile, true);
            out.write(data);
            out.close();

        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

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

class ProcedureCategoryDeserializer implements JsonDeserializer<ProcedureCategoryMap>{

    /**
     * Gson invokes this call-back method during deserialization when it encounters a field of the
     * specified type.
     * <p>In the implementation of this call-back method, you should consider invoking
     * {@link JsonDeserializationContext#deserialize(JsonElement, Type)} method to create objects
     * for any non-trivial field of the returned object. However, you should never invoke it on the
     * the same type passing {@code json} since that will cause an infinite loop (Gson will call your
     * call-back method again).
     *
     * @param json    The Json data being deserialized
     * @param typeOfT The type of the Object to deserialize to
     * @param context
     * @return a deserialized object of the specified type typeOfT which is a subclass of {@code T}
     * @throws JsonParseException if json is not in the expected format of {@code typeofT}
     */
    @Override
    public ProcedureCategoryMap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        ArrayList<String> categories = new ArrayList<>();
        HashMap<String, ArrayList<String>> subCategories = new HashMap<>();
        HashMap<String, ArrayList<String>> extremities = new HashMap<>();

        JsonObject jsonObject = json.getAsJsonObject();

        // Get each category name
        JsonArray allCategories = jsonObject.get("categories").getAsJsonArray();
        for (JsonElement json_category : allCategories) {

            JsonObject category = json_category.getAsJsonObject();
            String description = category.get("description").getAsString();
            categories.add(description);

//            // Get each subcategory name
//            ArrayList<String> subCategoryNames = new ArrayList<>();
//            JsonArray json_subCategories = category.get("subCategories").getAsJsonArray();
//            for (JsonElement json_subCategory : json_subCategories) {
//
//                JsonObject subCategory = json_subCategory.getAsJsonObject();
//                String subDescription = subCategory.get("description").getAsString();
//                subCategoryNames.add(subDescription);
//
//                // Get each extremity group name
//                ArrayList<String> extremityNames = new ArrayList<>();
//                JsonArray json_extremities = subCategory.get("extremities").getAsJsonArray();
//                for (JsonElement json_extremity : json_extremities) {
//                    JsonObject extremity = json_extremity.getAsJsonObject();
//                    String extremityName = extremity.get("description").getAsString();
//                    extremityNames.add(extremityName);
//                }
//                extremities.put(subDescription, extremityNames);
//            }
//            subCategories.put(description, subCategoryNames);
        }

//        return new ProcedureCategoryMap(categories, subCategories, extremities);
        return new ProcedureCategoryMap(categories);
    }
}

class ProcedureCodeDeserializer implements JsonDeserializer<HashMap<String, String>> {

    /**
     * Gson invokes this call-back method during deserialization when it encounters a field of the
     * specified type.
     * <p>In the implementation of this call-back method, you should consider invoking
     * {@link JsonDeserializationContext#deserialize(JsonElement, Type)} method to create objects
     * for any non-trivial field of the returned object. However, you should never invoke it on the
     * the same type passing {@code json} since that will cause an infinite loop (Gson will call your
     * call-back method again).
     *
     * @param json    The Json data being deserialized
     * @param typeOfT The type of the Object to deserialize to
     * @param context
     * @return a deserialized object of the specified type typeOfT which is a subclass of {@code T}
     * @throws JsonParseException if json is not in the expected format of {@code typeofT}
     */
    @Override
    public HashMap<String, String> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        HashMap<String, String> codes = new HashMap<>();

        JsonArray json_surgeries = json.getAsJsonArray();
        for (JsonElement json_surgery : json_surgeries){
            JsonObject surgery = json_surgery.getAsJsonObject();
            String code = surgery.get("code").getAsString();
            codes.put(code, surgery.get("description").getAsString());
        }
        return codes;
    }
}

class ProcedureCostDeserializer implements JsonDeserializer<Integer>{

    /**
     * Gson invokes this call-back method during deserialization when it encounters a field of the
     * specified type.
     * <p>In the implementation of this call-back method, you should consider invoking
     * {@link JsonDeserializationContext#deserialize(JsonElement, Type)} method to create objects
     * for any non-trivial field of the returned object. However, you should never invoke it on the
     * the same type passing {@code json} since that will cause an infinite loop (Gson will call your
     * call-back method again).
     *
     * @param json    The Json data being deserialized
     * @param typeOfT The type of the Object to deserialize to
     * @param context
     * @return a deserialized object of the specified type typeOfT which is a subclass of {@code T}
     * @throws JsonParseException if json is not in the expected format of {@code typeofT}
     */
    @Override
    public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Integer cost;
        JsonObject jsonCost = json.getAsJsonObject();
        cost = jsonCost.get("patientCharges").getAsInt();
        return cost;
    }
}

class ProceduresDeserializer implements JsonDeserializer<ArrayList<String[]>>{

    /**
     * Gson invokes this call-back method during deserialization when it encounters a field of the
     * specified type.
     * <p>In the implementation of this call-back method, you should consider invoking
     * {@link JsonDeserializationContext#deserialize(JsonElement, Type)} method to create objects
     * for any non-trivial field of the returned object. However, you should never invoke it on the
     * the same type passing {@code json} since that will cause an infinite loop (Gson will call your
     * call-back method again).
     *
     * @param json    The Json data being deserialized
     * @param typeOfT The type of the Object to deserialize to
     * @param context
     * @return a deserialized object of the specified type typeOfT which is a subclass of {@code T}
     * @throws JsonParseException if json is not in the expected format of {@code typeofT}
     */
    @Override
    public ArrayList<String[]> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        ArrayList<String[]> list = new ArrayList<>();

        JsonArray procedures = json.getAsJsonArray();
        for (JsonElement json_procedure : procedures){
            JsonObject procedure = json_procedure.getAsJsonObject();
            String[] properties = new String[5];
            properties[0] = (procedure.has("code") ? procedure.get("code").getAsString() : "");
            properties[1] = (procedure.has("serviceLineCategory") ? procedure.get("serviceLineCategory").getAsString(): "");
            properties[2] = (procedure.has("serviceLineSubCategory") && !procedure.get("serviceLineSubCategory").isJsonNull() ? procedure.get("serviceLineSubCategory").getAsString(): "");
            properties[3] = (procedure.has("extremity") && !procedure.get("extremity").isJsonNull()? procedure.get("extremity").getAsString(): "");
            properties[4] = (procedure.has("description") ? procedure.get("description").getAsString(): "");

            list.add(properties);
        }

        return list;
    }
}