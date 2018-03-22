package ute.webservice.voiceagent;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.regex.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The Adapter for the list view with specific surgeries.
 * Created by Nathan Taylor on 3/22/2018.
 */

public class SurgeryCodesListAdapter extends ArrayAdapter<String> {

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
        String description = getItem(position);

        // Produce the text without the
        Pattern pattern = Pattern.compile("(\\s-\\s[0-9]{4,})");
        Matcher matcher = pattern.matcher(description);
        String toShow = description;
        if (matcher.find()) {
            String end = matcher.group();
            toShow = toShow.replace(end, "");
        }

        if (setBackColor)
            convertView.setBackgroundColor(backColor);
        // Lookup view for the description
        TextView textView = (TextView)convertView.findViewById(R.id.listHeader);
        textView.setText(toShow);
        if (setTextColor)
            textView.setTextColor(textColor);
        else
            textView.setTextColor(Color.BLACK);
        return convertView;
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
}
