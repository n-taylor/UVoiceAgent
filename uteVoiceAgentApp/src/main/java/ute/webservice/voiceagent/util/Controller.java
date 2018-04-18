package ute.webservice.voiceagent.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

import ai.api.model.AIResponse;
import ute.webservice.voiceagent.R;
import ute.webservice.voiceagent.activities.BaseActivity;
import ute.webservice.voiceagent.activities.OnCallActivity;
import ute.webservice.voiceagent.activities.OpenBedsActivity;
import ute.webservice.voiceagent.activities.ProceduresListActivity;
import ute.webservice.voiceagent.activities.ResultsActivity;
import ute.webservice.voiceagent.activities.WelcomeActivity;
import ute.webservice.voiceagent.dao.DAOFactory;
import ute.webservice.voiceagent.dao.EDWDAOFactory;
import ute.webservice.voiceagent.dao.OnCallDAO;
import ute.webservice.voiceagent.dao.OpenBedsDAO;
import ute.webservice.voiceagent.dao.ProceduresDAO;
import ute.webservice.voiceagent.dao.SpokDAOFactory;
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

    private static final String WELCOME_MESSAGE = "What do you want to know?";

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
                // Not implemented yet
            }
            else if (action.equals(Constants.GET_ONCALL)){
                String OCMID = ParseResult.extractOCMID(reply);
                displayPhoneNumbers(context, OCMID, query);
            }
            else if (action.equals(Constants.ACTION_UNKNOWN)){
                if (textView != null) {
                    String apology = "Sorry, '" + query + "' is not a known command";
                    textView.setText(apology);
                }
            }
        }
    }

    private static void displayOpenBeds(final Context context, final String unit, final String query){
        final String trimmedUnit = unit.replace(" ", "");

        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... voids) {
                return Controller.getOpenBedsDAO().getOpenBedCount(trimmedUnit);
            }

            @Override
            protected void onPostExecute(Integer count){
                Controller.getController().displayOpenBedCount(context, unit, query, count);
            }
        };
        task.execute();
    }

    private static void displayPhoneNumbers(final Context context, final String OCMID, final String query){
        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, HashMap<String, ArrayList<String>>> task = new AsyncTask<Void, Void, HashMap<String, ArrayList<String>>>() {
            @Override
            protected HashMap<String, ArrayList<String>> doInBackground(Void... voids) {
                return getOnCallDAO().getPhoneNumbers(OCMID);
            }

            @Override
            protected void onPostExecute(HashMap<String, ArrayList<String>> numbers){
                Intent intent = new Intent(context, OnCallActivity.class);
                intent.putExtra("query", query);
                intent.putExtra("phoneNumMap", numbers);
                context.startActivity(intent);
            }
        };
        task.execute();
    }


    private static void displayAllOpenBeds(final Context context, final String query){
        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, HashMap<String, Integer>> task = new AsyncTask<Void, Void, HashMap<String, Integer>>() {
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
                    toShow += String.format(" has %1$d bed%2$s available\n", beds, (beds == 1)?"":"s");
                }
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
        if (to.equals(ProceduresListActivity.class)){
            from.startActivity(intent);
        }
        else if (to.equals(OpenBedsActivity.class)){
            from.startActivity(intent);
        }
        else if (to.equals(OnCallActivity.class)){
            intent = new Intent(from, ResultsActivity.class);
            intent.putExtra("query", "On Call Finder");
            String toShow = "For which message group are you searching? For example, say \"Attending Burn\" " +
                    "or \"Dental\"";
            intent.putExtra("result", toShow);
            intent.putExtra("speak", false);
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

    private Controller(){

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
        openNewActivity(context, OnCallActivity.class);
    }

    /**
     * To be called when the logout button is pressed
     */
    public void onLogoutPressed(BaseActivity activity){
        // Create an LogoutTask and execute it to logout
        LogoutTask httpTask = new LogoutTask(activity);
        httpTask.execute();
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
     * Gets called when the ProceduresDAO finishes fetching all the data from EDW
     */
    @Override
    public void onProcedureInfoRetrieval(){
        welcomeActivity.setWelcomeText(WELCOME_MESSAGE);
        welcomeActivity.enableComponents(true);
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
        String value = NumberFormat.getNumberInstance(Locale.US).format(cost);
        value = String.format("The estimated patient cost of this procedure is $%s", value);

        // Start the results activity
        Intent intent = new Intent(context, ResultsActivity.class);
        intent.putExtra("query", getProceduresDAO().removeCode(description));
        intent.putExtra("result", value);
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
        String message = String.format(Locale.US, "%1$s has %2$d available bed%3$s", unit, openBeds, (openBeds == 1)?"":"s");
        Intent intent = new Intent(context, ResultsActivity.class);
        intent.putExtra("query", query);
        intent.putExtra("result", message);
        context.startActivity(intent);
    }
}
