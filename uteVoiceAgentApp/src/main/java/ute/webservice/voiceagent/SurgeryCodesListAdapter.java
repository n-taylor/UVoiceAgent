package ute.webservice.voiceagent;

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
import java.util.regex.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The Adapter for the list view with specific surgeries.
 * Created by Nathan Taylor on 3/22/2018.
 */

public class SurgeryCodesListAdapter extends ArrayAdapter<String> implements SurgeryCostRetrievalListener {

    private Context context;
    private HashMap<String, String> codes;

    private int backColor;
    private int textColor;
    private boolean setBackColor = false;
    private boolean setTextColor = false;

    public SurgeryCodesListAdapter(Context context, ArrayList<String> descriptions, HashMap<String, String> codes){
        super(context, 0, descriptions);
        this.context = context;
        this.codes = codes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        // Inflate the view if an existing view is not already being used
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.list_group, parent, false);
        }

        // Get the description for this position
        final String description = getItem(position);

        // Produce the text without the
        Pattern pattern = Pattern.compile("(\\s-\\s[0-9]{4,})");
        Matcher matcher = pattern.matcher(description);
        String toShow = description;
        if (matcher.find()) {
            String end = matcher.group();
            toShow = toShow.replace(end, "");
        }
        final String display = toShow;

        if (setBackColor)
            convertView.setBackgroundColor(backColor);
        // Lookup view for the description
        TextView textView = (TextView)convertView.findViewById(R.id.listHeader);
        textView.setText(toShow);
        if (setTextColor)
            textView.setTextColor(textColor);
        else
            textView.setTextColor(Color.BLACK);

        // Set the onClickListener
        convertView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                displayCost(description, display);
            }
        });
        return convertView;
    }

    /**
     * Determines the code associated with the given description, gets the cost of procedure and
     * displays it in the results activity.
     * @param description The description of the procedure.
     */
    private void displayCost(String description, String toShow){
        String code = "";
        for (String key : codes.keySet()){
            if (((String)codes.get(key)).equals(description))
                code = key;
        }
        SurgeryCostRetrieveTask task = new SurgeryCostRetrieveTask();
        task.addListener(this);
        task.execute(code, toShow);
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
        intent.putExtra("query", description);
        intent.putExtra("result", "The estimated patient cost of this procedure is $" + value);
        context.startActivity(intent);
    }
}
