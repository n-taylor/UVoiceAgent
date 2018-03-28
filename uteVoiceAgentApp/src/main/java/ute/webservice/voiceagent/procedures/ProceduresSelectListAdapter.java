package ute.webservice.voiceagent.procedures;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.ArrayList;

import ute.webservice.voiceagent.R;
import ute.webservice.voiceagent.activities.ResultsActivity;
import ute.webservice.voiceagent.procedures.util.ProcedureCostRetrievalListener;
import ute.webservice.voiceagent.procedures.util.ProcedureCostRetrieveTask;

/**
 * The Adapter for the list view with specific surgeries.
 * Created by Nathan Taylor on 3/22/2018.
 */

public class ProceduresSelectListAdapter extends ArrayAdapter<String> implements ProcedureCostRetrievalListener {

    private Context context;
    private ArrayList<String> procedures;

    private int backColor;
    private int textColor;
    private boolean setBackColor = false;
    private boolean setTextColor = false;

    public ProceduresSelectListAdapter(Context context, ArrayList<String> descriptions){
        super(context, 0, descriptions);
        this.context = context;
        this.procedures = descriptions;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        // Inflate the view if an existing view is not already being used
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.list_group, parent, false);
        }

        // Get the description for this position
        final String description = getItem(position);

        if (setBackColor)
            convertView.setBackgroundColor(backColor);
        // Lookup view for the description
        TextView textView = (TextView)convertView.findViewById(R.id.listHeader);
        textView.setText(ProcedureInfo.removeCode(description));
        if (setTextColor)
            textView.setTextColor(textColor);
        else
            textView.setTextColor(Color.BLACK);

        // Set the onClickListener
        convertView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                displayCost(description);
            }
        });
        return convertView;
    }

    /**
     * Determines the code associated with the given description, gets the cost of procedure and
     * displays it in the results activity.
     * @param description The description of the procedure.
     */
    private void displayCost(String description){
        ProcedureCostRetrieveTask task = new ProcedureCostRetrieveTask();
        task.addListener(this);
        task.execute(ProcedureInfo.getCode(description), description);
    }

    /**
     * Sets the background of the text list to the resolved color specified.
     * @param color The resolved color to use. For example, use ContextCompat.getColor(this, R.color.color_slategray)
     */
    public void setBackColor(int color){
        setBackColor = true;
        backColor = color;
    }

    /**
     * Sets the text color of the list to the resolved color specified.
     * @param color The resolved color to use. For example, use ContextCompat.getColor(R.color.color_slategray)
     */
    public void setTextColor(int color){
        setTextColor = true;
        textColor = color;
    }

    /**
     * Displays the results in the results activity.
     * @param cost The cost of the given procedure.
     */
    @Override
    public void onCostRetrieval(int cost, String description) {
        Intent intent = new Intent(context, ResultsActivity.class);
        String value = NumberFormat.getNumberInstance(Locale.US).format(cost);
        intent.putExtra("query", ProcedureInfo.removeCode(description));
        intent.putExtra("result", "The estimated patient cost of this procedure is $" + value);
        context.startActivity(intent);
    }
}
