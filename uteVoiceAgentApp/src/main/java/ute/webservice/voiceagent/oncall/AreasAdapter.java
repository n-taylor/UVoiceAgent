package ute.webservice.voiceagent.oncall;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import ute.webservice.voiceagent.R;
import ute.webservice.voiceagent.util.Controller;

/**
 * The adapter for the list of areas in the OnCallListActivity
 * Created by Nathan Taylor on 5/8/2018.
 */

public class AreasAdapter extends ArrayAdapter<String> {

    private Context context;
    private ArrayList<String> areas;

    private int backColor;
    private int textColor;
    private boolean setBackColor = false;
    private boolean setTextColor = false;

    public AreasAdapter(Context context, ArrayList<String> areas){
        super(context, 0, areas);
        this.context = context;
        this.areas = areas;
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
        textView.setText(OnCallController.removeCode(description));
        if (setTextColor)
            textView.setTextColor(textColor);
        else
            textView.setTextColor(Color.BLACK);

        // Set the onClickListener
        convertView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                OnCallController.onAreaPressed(context, description);
            }
        });
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
