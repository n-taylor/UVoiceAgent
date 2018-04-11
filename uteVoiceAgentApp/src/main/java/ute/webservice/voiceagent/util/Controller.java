package ute.webservice.voiceagent.util;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import ai.api.model.AIResponse;
import ute.webservice.voiceagent.activities.OnCallActivity;
import ute.webservice.voiceagent.activities.OpenBedsActivity;
import ute.webservice.voiceagent.activities.ProceduresListActivity;
import ute.webservice.voiceagent.activities.ResultsActivity;
import ute.webservice.voiceagent.dao.DAOFactory;
import ute.webservice.voiceagent.dao.EDWDAOFactory;
import ute.webservice.voiceagent.dao.OpenBedsDAO;

/**
 * This class separates the model from the view. It provides a static method for each
 * function the activities need to perform that involves the handling of data.
 *
 * Created by Nathan Taylor on 4/11/2018.
 */

public class Controller {

    private static OpenBedsDAO openBedsDAO;

    /**
     *
     */
    private Controller(){

    }

    public static void processDialogFlowResponse(Context context, AIResponse response){

        Class senderClass = context.getClass();
        ParseResult parseResult = new ParseResult(response);

        boolean complete = parseResult.actionIsComplete();
        String action = parseResult.get_Action();
        String unit = parseResult.getCensusUnit();
        String query = parseResult.get_ResolvedQuery();

        if(!complete || unit == null || unit.isEmpty()){
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
                // TODO: Implement
            }
            else if (action.equals(Constants.GET_ONCALL)){
                // TODO: Implement
            }
        }
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
}
