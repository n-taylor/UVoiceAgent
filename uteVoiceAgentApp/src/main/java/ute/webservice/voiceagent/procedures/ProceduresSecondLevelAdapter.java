package ute.webservice.voiceagent.procedures;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import ute.webservice.voiceagent.activities.ProceduresSelectActivity;
import ute.webservice.voiceagent.dao.ProceduresDAO;
import ute.webservice.voiceagent.util.Controller;
import ute.webservice.voiceagent.util.ParseResult;
import ute.webservice.voiceagent.R;
import ute.webservice.voiceagent.activities.ResultsActivity;
import ute.webservice.voiceagent.procedures.util.ProcedureCostRetrievalListener;
import ute.webservice.voiceagent.procedures.util.ProcedureCostRetrieveTask;

/**
 * Created by Nathan Taylor on 3/20/2018.
 */

public class ProceduresSecondLevelAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<String> subcategoryHeaders;

    private String currentCategory; // the category (parent-level header) under which this is displayed

    private boolean setMidColor;
    private boolean setBottomColor;
    private int midColor;
    private int bottomColor;

    private boolean setMidTextColor;
    private boolean setBottomTextColor;
    private int midTextColor;
    private int bottomTextColor;

    private int width = 1000;

    private ParseResult PR;

    public ProceduresSecondLevelAdapter(Context context, ArrayList<String> subcategoryHeaders){
        this.context = context;
        this.subcategoryHeaders = new ArrayList<>();
        this.subcategoryHeaders.addAll(subcategoryHeaders);
        PR = new ParseResult();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition)
    {
        String subcategory = (String)getGroup(groupPosition);
        return Controller.getProceduresDAO().getExtremityHeaders(currentCategory, subcategory).get(childPosition);
    }
    @Override
    public long getChildId(int groupPosition, int childPosition)
    {
        return childPosition;
    }


    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent)
    {
        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_items, parent, false);

            // DEFINE BACKGROUND COLOR HERE
            if (setBottomColor)
                convertView.setBackgroundColor(bottomColor);
            else
                convertView.setBackgroundColor(Color.WHITE);
        }
        TextView thirdLevelTextView = (TextView) convertView
                .findViewById(R.id.listItem);
        //txtListChild.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        thirdLevelTextView.setText(Controller.getProceduresDAO().removeCode(childText));
        thirdLevelTextView.setWidth(width);
        if (setBottomTextColor)
            thirdLevelTextView.setTextColor(bottomTextColor);
        else
            thirdLevelTextView.setTextColor(Color.BLACK);

        final String subcategoryText = (String)getGroup(groupPosition);
        if (!Controller.getProceduresDAO().isExtremity(currentCategory, subcategoryText, childText)){
            // Display the cost of procedure using the description/child text
            convertView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Controller.getController().displayProcedureCost(v.getContext(), childText);
                }
            });
        }
        else {
            // Call the results page when the third-level view is clicked.
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    displaySurgeries(currentCategory, subcategoryText, childText);
                }
            });
        }
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition)
    {
        try {
            return Controller.getProceduresDAO().getExtremityHeaders(currentCategory, (String)getGroup(groupPosition)).size();
        } catch (Exception e) {
            return 0;
        }
    }
    @Override
    public Object getGroup(int groupPosition)
    {
        return this.subcategoryHeaders.get(groupPosition);
    }
    @Override
    public int getGroupCount()
    {
        return this.subcategoryHeaders.size();
    }
    @Override
    public long getGroupId(int groupPosition)
    {
        return groupPosition;
    }


    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent)
    {
        final String headerTitle = (String) getGroup(groupPosition);
//        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group_second, parent, false);

            // SET BACKGROUND COLOR HERE
            if (setMidColor)
                convertView.setBackgroundColor(midColor);
            else
                convertView.setBackgroundColor(Color.GRAY);
//        }
        TextView secondLevelTextView = (TextView) convertView
                .findViewById(R.id.listHeaderSecond);
        secondLevelTextView.setText(headerTitle);
        secondLevelTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        if (setMidTextColor)
            secondLevelTextView.setTextColor(midTextColor);
        else
            secondLevelTextView.setTextColor(Color.WHITE);

        // If the current category is other, then display the cost of the surgery
        if (currentCategory.equals(ProceduresDAO.MISC_CATEGORY)){
            convertView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Controller.getController().displayProcedureCost(v.getContext(), headerTitle);
                }
            });
        }

        ArrayList<String> headers = Controller.getProceduresDAO().getExtremityHeaders(currentCategory, headerTitle);
        if (headers.size() <= 5){
            return convertView;
        }
        if (headers.size() > 5){
            if (!Controller.getProceduresDAO().isExtremity(currentCategory, headerTitle, headers.get(0))) {
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        displaySurgeries(currentCategory, headerTitle);
                    }
                });
            }
        }

        return convertView;
    }
    @Override
    public boolean hasStableIds() {
        return true;
    }
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    /**
     * Sets the background color of the second-level items
     * @param color The resolved color int (for example, use ContextCompat.getColor(this, R.color.color_slategrey))
     */
    public void setMidColor(int color){
        this.setMidColor = true;
        this.midColor = color;
    }

    /**
     * Sets the background color of the third-level items
     * @param color The resolved color int (for example, use ContextCompat.getColor(this, R.color.color_slategrey))
     */
    public void setBottomColor(int color){
        this.setBottomColor = true;
        this.bottomColor = color;
    }

    /**
     * Sets the text color of the second-level items
     * @param color The resolved color int (for example, use ContextCompat.getColor(this, R.color.color_slategrey))
     */
    public void setMidTextColor(int color){
        midTextColor = color;
        setMidTextColor = true;
    }

    /**
     * Sets the text color of the third-level items
     * @param color The resolved color int (for example, use ContextCompat.getColor(this, R.color.color_slategrey))
     */
    public void setBottomTextColor(int color){
        bottomTextColor = color;
        setBottomTextColor = true;
    }

    /**
     * Defines under which category this subcategory is.
     * @param category The parent-level header.
     */
    public void setCurrentCategory(String category){
        this.currentCategory = category;
    }

    /**
     * Displays all the procedures related to the extremity given.
     */
    private void displaySurgeries(String category, String subCategory, String extremity){
        ArrayList<String> procedures = Controller.getProceduresDAO().getProcedureDescriptionsByExtremity(category, subCategory, extremity);
        Intent intent = new Intent(context, ProceduresSelectActivity.class);
        intent.putExtra("procedures", procedures);
        context.startActivity(intent);
    }

    private void displaySurgeries(String category, String subcategory){
        ArrayList<String> procedures = Controller.getProceduresDAO().getExtremityHeaders(category, subcategory);
        Intent intent = new Intent(context, ProceduresSelectActivity.class);
        intent.putExtra("procedures", procedures);
        context.startActivity(intent);
    }

    public void setWidth(int width){
        this.width = width;
    }

}