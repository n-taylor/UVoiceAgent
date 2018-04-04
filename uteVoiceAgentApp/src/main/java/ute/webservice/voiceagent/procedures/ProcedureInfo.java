package ute.webservice.voiceagent.procedures;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ute.webservice.voiceagent.util.ParseResult;
import ute.webservice.voiceagent.procedures.util.ProcedureCategoryRetrievalListener;
import ute.webservice.voiceagent.procedures.util.ProcedureCategoryRetrieveTask;
import ute.webservice.voiceagent.procedures.util.ProcedureJsonRetrievalListener;
import ute.webservice.voiceagent.procedures.util.ProcedureJsonRetrieveTask;

/**
 * This class holds the information needed to populate the procedures expandable list.
 * It is probably best to get information from this class in an asynchronous thread.
 * Created by Nathan Taylor on 3/23/2018.
 */

public class ProcedureInfo implements ProcedureCategoryRetrievalListener, ProcedureJsonRetrievalListener {

    private static boolean isRetrieving = false; // Determines whether the a call to the server is still in process.
    public final static String MISC_CATEGORY_TITLE = "OTHER";

    private static ProcedureNode procedureTreeRoot;

    private static int procedureCount = 0;

    private ParseResult PR;
    private ArrayList<ProcedureInfoListener> listeners;
    private ArrayList<String> categoryNames;

    public ProcedureInfo(){
        PR = new ParseResult();
        listeners = new ArrayList<>();
    }

    /**
     * Determines whether the procedure information needs to be fetched from the server.
     * @return True if the procedure information should be fetched.
     */
    public static boolean needsData(){
        return procedureTreeRoot == null;
    }

    /**
     * Returns the given description without the code on the end.
     * @param description The description to truncate.
     * @return The truncated string. If there is no code to remove, the given description is returned.
     */
    public static String removeCode(String description){
        Pattern pattern = Pattern.compile("(\\s-\\s[0-9]+)");
        Matcher matcher = pattern.matcher(description);
        String toShow = description;
        if (matcher.find()) {
            String end = matcher.group();
            toShow = toShow.replace(end, "");
        }
        return toShow;
    }

    /**
     * Retrieves the code associated with the first found instance of the given description.
     * @param description The description of the procedure to find.
     * @return The code of the given procedure, or null if the given description is not a registered procedure.
     */
    public static String getCode(String description){
        return procedureTreeRoot.findCode(description);
    }

    /**
     * Determines whether a given description is associated with an extremity or a procedure.
     * @return True if the given node is an extremity. False if it does not exist or is not an extremity.
     */
    public static boolean isExtremity(String category, String subcategory, String description){
        if(nodeExists(category, subcategory, description)){
            ProcedureNode node = procedureTreeRoot.goTo(category).goTo((subcategory)).goTo(description);
            if (!node.isProcedure())
                return true;
        }
        return false;
    }

    /**
     * Determines whether a given description exists under a given subcategory and category.
     * @param category
     * @param subcategory
     * @param description
     * @return True if it exists
     */
    public static boolean nodeExists(String category, String subcategory, String description){
        ProcedureNode node = procedureTreeRoot;
        if (node.containsChild(category)) {
            node = node.goTo(category);
            if (node.containsChild(subcategory)) {
                node = node.goTo(subcategory);
                if (node.containsChild(description))
                    return true;
            }
        }
        return false;
    }

    /**
     * If the data is not yet available, returns null. Otherwise, returns a list of the top-level category names.
     */
    public static ArrayList<String> getCategoryNames(){
        if (procedureTreeRoot == null)
            return null;
        ArrayList<String> categories = new ArrayList<>();
        for (ProcedureNode node : procedureTreeRoot.getChildren())
            categories.add(node.getDescription());
        return categories;
    }

    /**
     * If the data is not yet available or the given category does not exist, returns null.
     * Otherwise, returns a list of the second-level descriptions.
     */
    public static ArrayList<String> getSubCategoryHeaders(String category){
        if (procedureTreeRoot == null)
            return null;
        if (!procedureTreeRoot.containsChild(category))
            return null;
        ProcedureNode node = procedureTreeRoot.goTo(category);
        ArrayList<String> subcategories = new ArrayList<>();
        for (ProcedureNode child : node.getChildren()){
            String name = child.getDescription();
            if (name != null)
                subcategories.add(name);
        }
        return subcategories;
    }

    /**
     * If the data is not yet available or the given category or subcategory does not exist, returns null.
     * Otherwise, returns a list of third-level headers.
     */
    public static ArrayList<String> getExtremityHeaders(String category, String subcategory){
        if (procedureTreeRoot == null)
            return null;
        if (!procedureTreeRoot.containsChild(category))
            return null;
        ProcedureNode node = procedureTreeRoot.goTo(category);
        if (!node.containsChild(subcategory))
            return null;
        node = node.goTo(subcategory);
        ArrayList<String> thirdlevelHeaders = new ArrayList<>();
        for (ProcedureNode child : node.getChildren()){
            String name = child.getDescription();
            if (name != null)
                thirdlevelHeaders.add(name);
        }
        return thirdlevelHeaders;
    }

    /**
     * If the data is not yet available or the given category, subcategory or extremity does not exist, returns null.
     * Otherwise, returns a list of procedure descriptions.
     */
    public static ArrayList<String> getExtremityProcedureDescriptions(String category, String subcategory, String extremity){
        if (procedureTreeRoot == null)
            return null;
        if (!procedureTreeRoot.containsChild(category))
            return null;
        ProcedureNode node = procedureTreeRoot.goTo(category);
        if (!node.containsChild(subcategory))
            return null;
        node = node.goTo(subcategory);
        if (!node.containsChild(extremity))
            return null;
        node = node.goTo(extremity);

        ArrayList<String> procedureDescriptions = new ArrayList<>();
        for (ProcedureNode child : node.getChildren()){
            String name = child.getDescription();
            if (name != null)
                procedureDescriptions.add(name);
        }
        return procedureDescriptions;
    }

    /**
     * Sends a request to the server to get all the procedure categoryNames and codes.
     * isRetrieving is set to true until all the information is retrieved and available.
     */
    public static ProcedureInfo fetchData(){
        // Only get the data if the data is not already present
        if (procedureTreeRoot != null)
            return null;
        ProcedureInfo PI = new ProcedureInfo();
        PI.retrieve();
        return PI;
    }

    private void retrieve(){
        isRetrieving = true;
        ProcedureCategoryRetrieveTask task = new ProcedureCategoryRetrieveTask();
        task.addListener(this);
        task.execute();
    }

    /**
     * Initializes the data structures, adds the category names (and an OTHER category) from the server and gets all the procedures
     * and their codes from the server.
     * @param categoryNames Only the names of the top-level category.
     */
    public void onCategoryRetrieval(ArrayList<String> categoryNames){
        procedureTreeRoot = new ProcedureNode("ROOT");
        this.categoryNames = categoryNames;

        fetchNextCategory();
    }

    private int currentCategory = 0;
    private void fetchNextCategory(){
        if (categoryNames == null)
            return;
        if (currentCategory < categoryNames.size()) {
            ProcedureJsonRetrieveTask task = new ProcedureJsonRetrieveTask();
            task.addListener(this);
            task.execute(categoryNames.get(currentCategory++));
        }
        else {

            // Signal that the retrieving process has ended
            isRetrieving = false;
            //procedureTreeRoot.printTree();
            notifyListeners();
        }
    }

    /**
     * Given the list of procedures as a Json string, parses them into an ArrayList of String arrays containing:
     * [0] -> code
     * [1] -> category
     * [2] -> subcategory
     * [3] -> extremity
     * [4] -> description
     *
     * Adds the information received to the appropriate static data structure.
     * Signals that it has finished retrieving information from the server.
     * @param jsonResponse
     */
    public void onCodeRetrieval (String jsonResponse){
        ArrayList<String[]> procedures;
        procedures = PR.parseAllProcedures(jsonResponse);
        procedureCount += procedures.size();
        if (procedures != null){
            for (String[] procedure : procedures){
                ProcedureNode currentNode = procedureTreeRoot;
                // Make sure the procedure returned has at least a description and category
                if (!procedure[4].isEmpty() && !procedure[1].isEmpty()) {
                    // if the given category is not present in the tree, add it
                    currentNode.ensureChildCategory(procedure[1]);
                    // navigate to the category
                    currentNode = currentNode.goTo(procedure[1]);

                    // if the procedure does not belong to a subcategory, add it to the "other" category
                    String currentSubCategory = "";
                    if (procedure[2].isEmpty()) {
                        currentNode.ensureChildCategory(MISC_CATEGORY_TITLE);
                        currentNode = currentNode.goTo(MISC_CATEGORY_TITLE);
                    }
                    else {
                        currentNode.ensureChildCategory(procedure[2]);
                        currentNode = currentNode.goTo(procedure[2]);
                    }
                    // now the procedure has a subcategory
                    // if the procedure does not belong to an extremity, add it under the given subcategory
                    if (procedure[3].isEmpty()){
                        currentNode.addChild(procedure[0], procedure[4]);
                    }
                    else{
                        // add it under the given extremity
                        currentNode.ensureChildCategory(procedure[3]);
                        currentNode = currentNode.goTo(procedure[3]);
                        currentNode.addChild(procedure[0], procedure[4]);
                    }
                }
            }
        }
        fetchNextCategory();
    }

    private void notifyListeners(){
        for (ProcedureInfoListener listener : listeners){
            listener.onInfoRetrieval();
        }
    }

    public void addListener(ProcedureInfoListener listener){
        listeners.add(listener);
    }

}

