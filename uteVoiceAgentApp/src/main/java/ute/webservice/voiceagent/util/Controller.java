package ute.webservice.voiceagent.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.NetworkInterface;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ai.api.model.AIResponse;
import ute.webservice.voiceagent.R;
import ute.webservice.voiceagent.activities.BaseActivity;
import ute.webservice.voiceagent.activities.EquipmentFindActivity;
import ute.webservice.voiceagent.activities.OnCallActivity;
import ute.webservice.voiceagent.activities.OnCallListActivity;
import ute.webservice.voiceagent.activities.OpenBedsActivity;
import ute.webservice.voiceagent.activities.ProceduresListActivity;
import ute.webservice.voiceagent.activities.ResultsActivity;
import ute.webservice.voiceagent.activities.WelcomeActivity;
import ute.webservice.voiceagent.dao.CiscoDAOFactory;
import ute.webservice.voiceagent.dao.DAOFactory;
import ute.webservice.voiceagent.dao.EDWDAOFactory;
import ute.webservice.voiceagent.dao.LocationDAO;
import ute.webservice.voiceagent.dao.OnCallDAO;
import ute.webservice.voiceagent.dao.OpenBedsDAO;
import ute.webservice.voiceagent.dao.ProceduresDAO;
import ute.webservice.voiceagent.dao.SpokDAOFactory;
import ute.webservice.voiceagent.location.ClientLocation;
import ute.webservice.voiceagent.location.LocationController;
import ute.webservice.voiceagent.procedures.ProcedureInfoListener;
import ute.webservice.voiceagent.procedures.ProceduresParentListAdapter;
import ute.webservice.voiceagent.procedures.ProceduresSecondLevelAdapter;
import ute.webservice.voiceagent.procedures.util.ProcedureCostRetrievalListener;
import ute.webservice.voiceagent.procedures.util.ProcedureCostRetrieveTask;

/**
 * This class separates the model from the view. It provides a static method for each
 * function the activities need to perform that involves the handling of data.
 *
 * Created by Nathan Taylor on 4/11/2018.
 */

public class Controller implements ProcedureInfoListener, ProcedureCostRetrievalListener {

    private WelcomeActivity welcomeActivity;
    private TextView statusText; // A text view to use to show current status while a process is ongoing
    private Context lastContext; // To store the last context passed to the controller

    private static OpenBedsDAO openBedsDAO;
    private static ProceduresDAO proceduresDAO;
    private static OnCallDAO onCallDAO;
    private static LocationDAO locationDAO;

    private static final int zerosThreshold = 5;
    private static final String WELCOME_MESSAGE = "What do you want to know?";
    public static final String NOT_A_CURRENT_ASSIGNMENT = "The area requested has no current assignments";
    public static final String PARTIAL_QUERY_MESSAGE = "What do you want to know about ";
    public static final String ON_CALL_LIST_MESSAGE = "For which area are you looking?";
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static Controller controller;

    public static Controller getController(){
        if (controller == null)
            controller = new Controller();
        return controller;
    }

    public static void processDialogFlowResponse(Context context, AIResponse response, TextView textView){

        // Parse the result
        ParseResult parseResult = new ParseResult(response);

        // Store the result information
        boolean complete = parseResult.actionIsComplete();
        String action = parseResult.get_Action();
        String unit = parseResult.getCensusUnit();
        String query = parseResult.get_ResolvedQuery();
        String reply = parseResult.get_reply();

        // If there is more information needed to display a result, take the user to the appropriate activity
        if(!complete || (action.equals(Constants.GET_CENSUS) && (unit == null || unit.isEmpty()))){
            if (action.equals(Constants.GET_ONCALL))
                openNewActivity(context, OnCallActivity.class);
            else if (action.equals(Constants.GET_SURGERY_COST))
                openNewActivity(context, ProceduresListActivity.class);
            else
                openNewActivity(context, OpenBedsActivity.class);
        }
        // Otherwise, retrieve the necessary information and display it to the user
        else {
            if (action.equals(Constants.GET_CENSUS)){
                if (unit.equals("All")){
                    displayAllOpenBeds(context, query);
                }
                else
                    displayOpenBeds(context, unit, query);
            }
            else if (action.equals(Constants.GET_SURGERY_COST)){
                getController().displayProcedureCost(context, reply);
            }
            else if (action.equals(Constants.GET_ONCALL)){
                String OCMID = ParseResult.extractOCMID(reply);
                displayPhoneNumbers(context, OCMID, query);
            }
            else if (action.equals(Constants.ACTION_UNKNOWN)){
                String message = "Sorry, '" + query + "' is not a known command";
                if (context.getClass() != WelcomeActivity.class) {
                    Intent intent = getController().welcomeActivity.getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("message", message);
                    context.startActivity(intent);
                }
                else {
                    getController().welcomeActivity.setWelcomeText(message);
                }
            }
            else if (action.equals(Constants.PARTIAL_ACTION)){
                String toDisplay = PARTIAL_QUERY_MESSAGE + query + "?";
                // If the current activity is the welcome activity, just update the welcome message
                if (getController().welcomeActivity != null && context.getClass() != WelcomeActivity.class) {
                    Intent intent = getController().welcomeActivity.getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("message", toDisplay);
                    context.startActivity(intent);
                }
                else
                    getController().welcomeActivity.setWelcomeText(toDisplay);
            }
        }
    }

    private static void displayOpenBeds(final Context context, final String unit, final String query){
        final String trimmedUnit = unit.replace(" ", "");


        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {

            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute(){
                super.onPreExecute();

                // Create the progress dialog
                progressDialog = new ProgressDialog(context);
                progressDialog.setTitle("Loading");
                progressDialog.setIndeterminate(false);
                progressDialog.show();
            }

            @Override
            protected Integer doInBackground(Void... voids) {
                return Controller.getOpenBedsDAO().getOpenBedCount(trimmedUnit);
            }

            @Override
            protected void onPostExecute(Integer count){
                String toDisplay = unit;
                if (unit.equals("5W") || unit.equals("5 W")){
                    toDisplay = "5 West";
                }

                progressDialog.dismiss(); // Close the progress dialog

                Controller.getController().displayOpenBedCount(context, toDisplay, query, count);
            }
        };
        task.execute();
    }

    /**
     * Given an OCMID, displays the on-call professional and their associated phone numbers.
     * This method is done asynchronously.
     *
     * @param context The context used to start a new activity
     * @param OCMID The OCMID of the on-call area
     * @param query The user's query (e.g. "Who is on call in the burn unit?")
     */
    public static void displayPhoneNumbers(final Context context, final String OCMID, final String query){
        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, HashMap<String, ArrayList<String>>> task = new AsyncTask<Void, Void, HashMap<String, ArrayList<String>>>() {
            @Override
            protected HashMap<String, ArrayList<String>> doInBackground(Void... voids) {
                // Get all the phone numbers
                return getOnCallDAO().getPhoneNumbers(context, OCMID);
            }

            @Override
            protected void onPostExecute(HashMap<String, ArrayList<String>> numbers){
                if (numbers != null) {
                    Intent intent = new Intent(context, OnCallActivity.class);
                    intent.putExtra("query", query);
                    intent.putExtra("phoneNumMap", numbers);
                    context.startActivity(intent);
                }
                else {
                    Intent intent = new Intent(context, ResultsActivity.class);
                    intent.putExtra("query", query);
                    intent.putExtra("result", Controller.NOT_A_CURRENT_ASSIGNMENT);
                    context.startActivity(intent);
                }
            }
        };
        task.execute();
    }


    public static void displayAllOpenBeds(final Context context, final String query){
        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, HashMap<String, Integer>> task = new AsyncTask<Void, Void, HashMap<String, Integer>>() {

            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute(){
                progressDialog = new ProgressDialog(context);
                progressDialog.setTitle("Loading");
                progressDialog.setIndeterminate(false);
                progressDialog.show();
            }

            @Override
            protected HashMap<String, Integer> doInBackground(Void... voids) {
                return getOpenBedsDAO().getAllOpenBedCounts();
            }

            @Override
            protected void onPostExecute(HashMap<String, Integer> openBeds){
                String toShow = "";
                ArrayList<String> keys = new ArrayList<String>(openBeds.keySet());
                Collections.sort(keys);
                for (String unit : keys){
                    int beds = openBeds.get(unit);
                    toShow += unit;
                    toShow += String.format(Locale.US, " has %1$d bed%2$s available\n", beds, (beds == 1)?"":"s");
                }
                progressDialog.dismiss();
                // Open a results activity with the information
                Intent intent = new Intent(context, ResultsActivity.class);
                intent.putExtra("query", query);
                intent.putExtra("result", toShow);
                context.startActivity(intent);
            }
        };
        task.execute();
    }

    public static OpenBedsDAO getOpenBedsDAO(){
        if (openBedsDAO == null){
            DAOFactory daoFactory = DAOFactory.getDAOFactory(DAOFactory.EDW);
            EDWDAOFactory edwFactory = null;
            if (daoFactory instanceof EDWDAOFactory)
                edwFactory = (EDWDAOFactory) daoFactory;
            else
                return null;
            openBedsDAO = edwFactory.getOpenBedsDAO();
        }
        return openBedsDAO;
    }

    public static OnCallDAO getOnCallDAO(){
        if (onCallDAO == null){
            SpokDAOFactory daoFactory = (SpokDAOFactory) DAOFactory.getDAOFactory(DAOFactory.SPOK);
            onCallDAO = daoFactory.getOnCallDAO();
        }
        return onCallDAO;
    }

    /**
     * Opens a new activity. If the activity to open is the same as "from", do nothing.
     * @param from The context the new activity is being opened from
     * @param to The activity to open
     */
    private static void openNewActivity(Context from, Class to){
        if (from.getClass().equals(to))
            return;

        Intent intent = new Intent(from, to);
        if (to.equals(OnCallListActivity.class)){
            intent = new Intent(from, OnCallListActivity.class);
            intent.putExtra("query", ON_CALL_LIST_MESSAGE);
            from.startActivity(intent);
        }
        else
         {
             from.startActivity(intent);
         }
    }

    public static ProceduresDAO getProceduresDAO(){
        if (proceduresDAO == null){
            EDWDAOFactory daoFactory = (EDWDAOFactory)DAOFactory.getDAOFactory(DAOFactory.EDW);
            proceduresDAO = daoFactory.getProceduresDAO();
        }
        return proceduresDAO;
    }

    /**
     * If necessary, creates a singleton instance of the LocationDAO and returns it.
     */
    public static LocationDAO getLocationDAO(){
        if (locationDAO == null){
            CiscoDAOFactory daoFactory = (CiscoDAOFactory)DAOFactory.getDAOFactory(DAOFactory.CISCO);
            locationDAO = daoFactory.getLocationDAO();
        }
        return locationDAO;
    }

    protected Controller(){

    }

    /**
     * To be called when a cancel button is pressed.
     */
    public void onCancelPressed(){
        TTS.stop();
    }

    /**
     * To be called when an Open Beds button is pressed.
     */
    public void onBedButtonPressed(Context context){
        openNewActivity(context, OpenBedsActivity.class);
    }

    /**
     * To be called when a Procedure button is pressed.
     * @param context
     */
    public void onProcedureButtonPressed(Context context){
        openNewActivity(context, ProceduresListActivity.class);
    }

    public void onOnCallButtonPressed(Context context){
        openNewActivity(context, OnCallListActivity.class);
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    /**
     * Call when the equipment finder button gets pressed.
     */
    public void onEquipmentFinderButtonPressed(Activity activity, Context context) {
//        verifyStoragePermissions(activity);

//        displayClientLocation("f8:34:41:bf:ab:ee", context); // Hardcoded mac address for testing with an emulator
        displayClientLocation(getMacAddr().toLowerCase(Locale.US), context); // This line for use without an emulator
        LocationController.getInstance().findTagLocation("00:12:b8:0d:0a:2b", context);
    }

    /**
     * Retrieves the location of the client with the given ID, and displays it in the Equipment find activity
     * with a map of the floor plan.
     *
     * @param id The mac address of the client
     * @param context used to start a new activity
     */
    public static void displayClientLocation(String id, final Context context){
        @SuppressLint("StaticFieldLeak")
        AsyncTask<String, Void, ClientLocation> task = new AsyncTask<String, Void, ClientLocation>() {
            @Override
            protected ClientLocation doInBackground(String... strings) {
                ClientLocation location = null;
                try {
                    // Try finding the client at the hospital. If it's not there, check research park
                    location = getLocationDAO().getClientLocation(strings[0], context, LocationDAO.EBC);
                    if (location == null) {
                        location = getLocationDAO().getClientLocation(strings[0], context, LocationDAO.PARK);
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                return location;
            }

            @Override
            protected void onPostExecute(ClientLocation location){
                if (location != null) {
                    float x = location.getMapCoordinate().getX();
                    float y = location.getMapCoordinate().getY();
                    String message = "Coordinates: (" + x + ", " + y + ")";
                    System.out.println(message);

                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    getLocationDAO().getFloorPlanImage(context, location.getImageName());
                    LocationController.getInstance().setClientLocation(location);
                }
                else {
                    Toast.makeText(context, getMacAddr(), Toast.LENGTH_SHORT).show();
                }
            }
        };
        task.execute(id);

        //openNewActivity(context, EquipmentFindActivity.class);
    }

    /**
     * Displays an image in the EquipmentFindActivity
     * @param context used to open a new activity
     * @param bitmap the image to display
     */
    public static void displayFloorMap(Context context, Bitmap bitmap){
        Intent intent = new Intent(context, EquipmentFindActivity.class);
        intent.putExtra(EquipmentFindActivity.BITMAP_KEY, bitmap);
        context.getApplicationContext().startActivity(intent);
    }

    /**
     *
     * @return the mac address of the current device. An emulator returns 02:00:00:00:00:00.
     */
    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
//                return res1.toString();
              return "f8:34:41:bf:ab:ee";
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }

    /**
     * To be called when the logout button is pressed
     */
    public void onLogoutPressed(BaseActivity activity){
        // Create an LogoutTask and execute it to logout
        //LogoutTask httpTask = new LogoutTask(activity);
        logout();
    }

    /**
     * To be called when an activity is created.
     * If the context calling it is a WelcomeActivity, loads the procedure category data.
     */
    public void onActivityCreated(BaseActivity activity){
        if (activity instanceof WelcomeActivity){
            if (getProceduresDAO().needsData()){
                welcomeActivity = (WelcomeActivity)activity;
                welcomeActivity.enableComponents(false);
                welcomeActivity.setWelcomeText("Loading...");
                getProceduresDAO().addListener(this);
                getProceduresDAO().fetchCategories();
            }
        }
    }

    /**
     * Gets called when the ProceduresDAO finishes fetching all the data from EDW.
     * If this is called and the data has still not been retrieved, the user needs to login again
     */
    @Override
    public void onProcedureInfoRetrieval(){
        if (!getProceduresDAO().needsData()) {
            welcomeActivity.setWelcomeText(WELCOME_MESSAGE);
            welcomeActivity.enableComponents(true);
            getProceduresDAO().printProcedures();
        }
        else{
            logout();
        }
    }

    /**
     * Logs the user out and returns them to the login page
     */
    private void logout(){
        LogoutTask httpTask = new LogoutTask(welcomeActivity);
        httpTask.execute();
    }

    /**
     * Should get called when the Procedures Activity has been started to initialize the list view.
     * @param listView The view to initialize.
     */
    public void initializeProceduresExpandableList(Context context, ExpandableListView listView){
        if (listView != null){
            ProceduresParentListAdapter adapter = new ProceduresParentListAdapter(context, getProceduresDAO().getCategoryNames());
            adapter.setWidth(context.getResources().getDimensionPixelSize(R.dimen.surgery_list_width)-200);
            listView.setAdapter(adapter);
        }
    }

    /**
     * Constructs a ProceduresSecondLevelAdapter with the correct data.
     * @param context The context of the activity the adapter is to modify/display
     * @param category The category of that the second level adapter will be under
     */
    public ProceduresSecondLevelAdapter getSecondLevelAdapter(Context context, String category){
        return new ProceduresSecondLevelAdapter(context, getProceduresDAO().getSubCategoryHeaders(category));
    }

    /**
     * Gets the subcategory header for the specified category and index
     */
    public String getProceduresSecondLevelHeader(String category, int index){
        return getProceduresDAO().getSubCategoryHeaders(category).get(index);
    }

    /**
     * Gets the list of third-level headers under the given category and subcategory. Null if doesn't exist.
     */
    public ArrayList<String> getProceduresThirdLevelHeaders(String category, String subCategory){
        return getProceduresDAO().getExtremityHeaders(category, subCategory);
    }

    /**
     * Given a cost and procedure description, removes the code on the description and
     * displays it with its associated cost.
     * @param cost The estimated cost of a procedure.
     * @param description The description of the procedure.
     */
    public void displayProcedureCost(Context context, int cost, String description){
        String message = NumberFormat.getNumberInstance(Locale.US).format(cost);
        if (cost > 0)
            message = String.format("The estimated patient cost of this procedure is $%s", message);
        else if (cost == Constants.ACCESS_DENIED_INT){
            message = Constants.SESSION_EXPIRED_MESSAGE;
        }
        else {
            message = "There was a problem retrieving the cost of this procedure. Please try again.";
        }

        // Start the results activity
        Intent intent = new Intent(context, ResultsActivity.class);
        intent.putExtra("query", getProceduresDAO().removeCode(description));
        intent.putExtra("result", message);
        context.startActivity(intent);
    }

    /**
     * Given a procedure's full description, retrieves the cost of the given procedure and displays it.
     * @param context
     * @param description The full description (including the code) of the procedure.
     */
    public void displayProcedureCost(Context context, String description){
        this.lastContext = context;
        ProcedureCostRetrieveTask task = new ProcedureCostRetrieveTask();
        task.addListener(this);
        task.execute(getProceduresDAO().getCode(description), description);
    }

    @Override
    public void onCostRetrieval(int cost, String description) {
        displayProcedureCost(lastContext, cost, description);
    }

    /**
     * Given a valid unit name, retrieves the current number of available beds in that unit and
     * displays the results in the results activity.
     * If the unit name is not valid, does nothing.
     *
     * If statusText is not null, also changes the text of the provided text view to show a message that data is being retrieved during
     * the retrieval process. The text is reverted to its previous message once the data is obtained.
     *
     * @param unit The unit to query.
     */
    public void displayOpenBedCount(Context context, final String unit){
        Controller.displayOpenBeds(context, unit, unit);
    }

    /**
     * Displays in the results activity that the given number of beds are available for the given unit or query.
     * @param query The query or unit containing the number of available beds
     * @param openBeds The number of available beds
     */
    private void displayOpenBedCount(Context context, String unit, String query, int openBeds){
        String message = "";
        if (openBeds == Constants.ACCESS_DENIED_INT){
            message = Constants.SESSION_EXPIRED_MESSAGE;
        }
        else if (openBeds < 0){
            message = "The unit '" + unit + "' is not recognized";
        }
        else
            message = String.format(Locale.US, "%1$s has %2$d available bed%3$s", unit, openBeds, (openBeds == 1)?"":"s");
        Intent intent = new Intent(context, ResultsActivity.class);
        intent.putExtra("query", query);
        intent.putExtra("result", message);
        context.startActivity(intent);
    }
}
