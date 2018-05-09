package ute.webservice.voiceagent.util;

import android.util.Xml;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

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
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handle response from API.AI, and save parameters temporarily.
 * Created by u1076070 on 5/10/2017.
 */

public class ParseResult {

    private static String currAssignOpeningTags = "<?xml version=\"1.0\" encoding=\"utf-8\"?> \n" +
            "<procedureCall name=\"GetGroupsCurrAssignXml\" xmlns=\"http://xml.amcomsoft.com/api/request\">   \n" +
            "<parameter name=\"ocmid\" null=\"false\">";

    private static String currAssignClosingTags = "</parameter>   \n" +
            "<parameter name=\"tz\" null=\"true\"></parameter> \n" +
            "</procedureCall> \n";

    private static String phoneNumberOpeningTags = "<?xml version=\"1.0\" encoding=\"utf-8\"?> \n" +
            "<procedureCall name=\"GetPhoneNumber\" xmlns=\"http://xml.amcomsoft.com/api/request\">\n " +
            "<parameter name=\"mid\" null=\"false\">";

    private static String phoneNumberClosingTags = "</parameter> \n" +
            "<parameter name=\"phone_number_type\" null=\"true\"></parameter> \n" + "</procedureCall>";

    //intent names, which matched to the names on API.AI.
    //TODO: load intent name from file/Constants, since this table may grow tremendously.
    private static final String intent_yes = "ReplyYes";
    private static final String intent_unknown = "Default Fallbck Intent";
    private static final String intent_sq = "surgery question";

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
        gBuilder.registerTypeAdapter(RoomStatus.class, new RoomStatusDeserializer());
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
     * Determines if the current action is complete
     * @return
     */
    public boolean actionIsComplete(){
        return !result.isActionIncomplete();
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

    /**
     * When a call for "GetGroupsCurrAssignXml" is made to spok and the xml is received, extracts
     * the Messaging ID (MID) and name of each on-call personnel.
     * @param in The input stream to pull the xml from
     * @return A mapping whose key is the on-call MID and value is the name retrieved.
     */
    public HashMap<String, String> parseCurrentAssignments(InputStream in) throws XmlPullParserException, IOException{
        try{
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            parser.nextTag();
            return readAssignments(parser);
        }
        finally {
            in.close();
        }
    }

    /**
     * If the current tag is not an opening tag named "success," throws an XmlPullParserException.
     * Otherwise, finds each assignment and adds its MID and name to a HashMap, which it will return.
     *
     * @param parser The XmlPullParser loaded with the input stream containing the XML to parse
     * @return A HashMap mapping the MID to the name of each assignment.
     */
    private HashMap<String, String> readAssignments(XmlPullParser parser) throws XmlPullParserException, IOException{
        HashMap<String, String> assignments = new HashMap<>();

        parser.require(XmlPullParser.START_TAG, null, "success");
        // move to <getGroupsCurrentAssignments>
        while (parser.getName() != null && !parser.getName().equals("getGroupsCurrentAssignments")){

            // If the parser has reached the end tag "</success>", then return null
            if (parser.getName().equals("success") && parser.getEventType() == parser.END_TAG)
                return null;

            // Keep moving to find getGroupsCurrentAssignments
            parser.next();
            if (parser.getEventType() == parser.TEXT)
                parser.next();
        }
        while (parser.next() != XmlPullParser.END_TAG){
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Look for assignment tags
            if (name.equals("assignment")){
                assignments = addAssignment(parser, assignments);
            }
            else{
                skip(parser); // skip this tag since it isn't an assignment.
            }
        }
        return assignments;
    }

    /**
     * Provided the parser is within an <assignment> tag, extracts the MID and name, adds them
     * to the given HashMap and returns the same HashMap.
     * If the parser is not currently at an <assignment> tag, throws an XmlPullParserException
     * @param assignments The HashMap to add to.
     * @return The given HashMap with the added assignment information.
     */
    private HashMap<String, String> addAssignment(XmlPullParser parser, HashMap<String, String> assignments) throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG, null, "assignment");
        String mid = null;
        String name = null;
        while (parser.next() != XmlPullParser.END_TAG){
            if (parser.getEventType() != XmlPullParser.START_TAG){
                continue;
            }
            String tagName = parser.getName();
            if (tagName.equals("mid")){
                mid = readMID(parser);
            } else if (tagName.equalsIgnoreCase("name")){
                name = readName(parser);
            }
            else {
                skip(parser);
            }
        }
        assignments.put(mid, name);
        return assignments;
    }

    /**
     * Reads the text inside an "mid" tag and advances the parser to the </mid> end tag.
     * @param parser
     * @return The text inside the "mid" tag
     */
    private String readMID(XmlPullParser parser) throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG, null, "mid");
        String mid = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "mid");
        return mid;
    }

    /**
     * Reads the text inside an "name" tag and advances the parser to the </name> end tag.
     * @param parser
     * @return The text inside the "name" tag
     */
    private String readName(XmlPullParser parser) throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG, null, "name");
        String name = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "name");
        return name;
    }

    /**
     *
     * @param parser
     * @return
     */
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    /**
     * Creates the XML needed to send a request to get information about the current assignment to the
     * group specified.
     * @param OCMID The On-Call Messaging ID of the group about which to get information
     * @return The completed string containing the XML request to be sent to the socket.
     */
    public static String getCurrentAssignmentsCall(String OCMID){
        return currAssignOpeningTags + OCMID + currAssignClosingTags;
    }

    /**
     * Given the name of a groupName, extracts and returns the OCMID.
     * The groupName string must be in this format: BODY DONATION [10000516]
     * @return The OCMID as a string
     */
    public static String extractOCMID(String groupName){
        String ocmid = "";
        Pattern pattern = Pattern.compile("[A-Za-z\\s\\-]+\\[(\\d+)\\]");
        Matcher matcher = pattern.matcher(groupName);
        if (matcher.find()) {
            ocmid = matcher.group(1);
        }
        return ocmid;
    }

    public static String getPhoneNumberCall(String MID){
        return phoneNumberOpeningTags + MID + phoneNumberClosingTags;
    }

    /**
     * After a call is written to the socket to get Phone numbers, this parses the response into
     * a list of strings that contain each phone number associated with the MID used, prepended
     * by the number type.
     * @param in The input stream to read from.
     * @return The list of phone numbers.
     */
    public ArrayList<String> parsePhoneNumbers(InputStream in) throws XmlPullParserException, IOException{
        try{
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            parser.nextTag();
            return readNumbers(parser);
        }
        finally {
            in.close();
        }
    }


    private ArrayList<String> readNumbers(XmlPullParser parser) throws XmlPullParserException, IOException{
        String numbersText = null;
        parser.require(XmlPullParser.START_TAG, null, "success");
        while (parser.next() != XmlPullParser.END_TAG){
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            String attrType = parser.getAttributeValue(null, "name");
            // Look for assignment tags
            if (name.equals("parameter")){
                if (attrType.equalsIgnoreCase("phone_number")){
                    numbersText = readText(parser);
                }
                else{
                    if (parser.next() != XmlPullParser.END_TAG)
                        parser.next();
                }
            }
            else{
                skip(parser); // skip this tag since it isn't an assignment.
            }
        }

        return extractNumbers(numbersText);
    }

    /**
     * Given a string, looks for phone numbers and their types and places them into an ArrayList<String>.
     * Formats the number into this format: 888-888-8888 : TYPE
     *
     * @param text
     * @return
     */
    private ArrayList<String> extractNumbers(String text){
        ArrayList<String> numbers = new ArrayList<>();
        Pattern pattern = Pattern.compile("(\\[[0-9]+\\]\\[[A-Z\\s\\-.]+\\])");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()){
            for (int i = 1; i <= matcher.groupCount(); i++){
                String toShow = formatNumber(matcher.group(i));
                if (!numbers.contains(toShow) && !isZero(matcher.group(i)))
                    numbers.add(formatNumber(toShow));
            }
        }
        if (numbers.size() < 1){
            numbers.add("No phone numbers available");
        }
        return numbers;
    }

    /**
     * Given a phone number in the following format, determines if the number is all zeros:
     * [12345] [TYPE]
     * @param phoneNumber
     * @return
     */
    private boolean isZero(String phoneNumber) {
        Pattern pattern = Pattern.compile("\\[([0-9]+)\\]\\[[A-Z\\s\\-.]+\\]");
        Matcher matcher = pattern.matcher(phoneNumber);
        while (matcher.find()){
            String number = matcher.group(1);
            long num = Long.parseLong(number);
            if (num == 0)
                return true;
        }
        return false;
    }

    private String formatNumber(String number){
        String formatted = number;
        String phoneNum = "";
        String type = "";
        Pattern fullPattern = Pattern.compile("\\[(\\d+)\\]\\[(\\D+)\\]");
        Matcher matcher = fullPattern.matcher(number);
        if (matcher.find()){
            phoneNum = matcher.group(1);
            type = matcher.group(2);
        }

        if (phoneNum != null && !phoneNum.isEmpty() && type != null && !type.isEmpty()){
            if (phoneNum.length() == 10){
                phoneNum = phoneNum.substring(0,3) + "-" + phoneNum.substring(3,6) + "-" + phoneNum.substring(6);
            }
            else if (phoneNum.length() == 7){
                phoneNum = phoneNum.substring(0,3) + "-" + phoneNum.substring(3);
            }

            formatted = phoneNum + " : " + type;
        }

        return formatted;
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