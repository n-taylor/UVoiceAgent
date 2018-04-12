package ute.webservice.voiceagent.util;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
import ute.webservice.voiceagent.dao.EDWProceduresDAO;
import ute.webservice.voiceagent.dao.OnCallDAO;
import ute.webservice.voiceagent.dao.OpenBedsDAO;
import ute.webservice.voiceagent.dao.ProceduresDAO;
import ute.webservice.voiceagent.dao.SpokDAOFactory;
import ute.webservice.voiceagent.dao.SpokOnCallDAO;
import ute.webservice.voiceagent.procedures.ProcedureInfoListener;

/**
 * This class separates the model from the view. It provides a static method for each
 * function the activities need to perform that involves the handling of data.
 *
 * Created by Nathan Taylor on 4/11/2018.
 */

public class Controller implements ProcedureInfoListener{

    private WelcomeActivity welcomeActivity;

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

    public static void processDialogFlowResponse(Context context, AIResponse response){

        ParseResult parseResult = new ParseResult(response);

        boolean complete = parseResult.actionIsComplete();
        String action = parseResult.get_Action();
        String unit = parseResult.getCensusUnit();
        String query = parseResult.get_ResolvedQuery();
        String reply = parseResult.get_reply();

        if(!complete || (action.equals(Constants.GET_CENSUS) && (unit == null || unit.isEmpty()))){
            if (action.equals(Constants.GET_ONCALL))
                openNewActivity(context, OnCallActivity.class);
            else if (action.equals(Constants.GET_SURGERY_COST))
                openNewActivity(context, ProceduresListActivity.class);
            else
                openNewActivity(context, OpenBedsActivity.class);
        }
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
        }
    }

    private static void displayPhoneNumbers(final Context context, final String OCMID, final String query){
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

    private static void displayOpenBeds(final Context context, final String unit, final String query){
        AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... voids) {
                return getOpenBedsDAO().getOpenBedCount(unit);
            }

            @Override
            protected void onPostExecute(Integer beds){
                String toShow = unit + String.format(" has %1$d bed%2$s available\n", beds, (beds == 1)?"":"s");
                // Open a results activity with the information
                Intent intent = new Intent(context, ResultsActivity.class);
                intent.putExtra("query", query);
                intent.putExtra("result", toShow);
                context.startActivity(intent);
            }
        };
        task.execute();
    }


    private static void displayAllOpenBeds(final Context context, final String query){
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

    private static OpenBedsDAO getOpenBedsDAO(){
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

    private static OnCallDAO getOnCallDAO(){
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

    private static ProceduresDAO getProceduresDAO(){
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
    public void onInfoRetrieval(){
        welcomeActivity.setWelcomeText(WELCOME_MESSAGE);
        welcomeActivity.enableComponents(true);
    }
}
