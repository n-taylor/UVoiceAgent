package ute.webservice.voiceagent.util;

import android.content.Context;
import android.content.Intent;

import ai.api.model.AIResponse;
import ute.webservice.voiceagent.activities.OnCallActivity;
import ute.webservice.voiceagent.activities.OpenBedsActivity;
import ute.webservice.voiceagent.activities.ProceduresListActivity;

/**
 * This class separates the model from the view. It provides a static method for each
 * function the activities need to perform that involves the handling of data.
 *
 * Created by Nathan Taylor on 4/11/2018.
 */

public class Controller {

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

        if(!complete){
            if (action.equals(Constants.GET_ONCALL))
                openNewActivity(context, OnCallActivity.class);
            else if (action.equals(Constants.GET_SURGERY_COST))
                openNewActivity(context, ProceduresListActivity.class);
            else
                openNewActivity(context, OpenBedsActivity.class);
            return;
        }


        if (senderClass.equals(ProceduresListActivity.class)){

        }
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

        }
    }
}
