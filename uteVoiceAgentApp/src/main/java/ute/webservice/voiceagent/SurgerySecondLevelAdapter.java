package ute.webservice.voiceagent;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Nathan Taylor on 3/20/2018.
 */

public class SurgerySecondLevelAdapter extends BaseExpandableListAdapter implements SurgeryCostRetrievalListener {

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

    private ParseResult PR;

    public SurgerySecondLevelAdapter(Context context, ArrayList<String> subcategoryHeaders){
        this.context = context;
        this.subcategoryHeaders = new ArrayList<>();
        this.subcategoryHeaders.addAll(subcategoryHeaders);
        PR = new ParseResult();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition)
    {
        String subcategory = (String)getGroup(groupPosition);
        return ProcedureInfo.getExtremityHeaders(currentCategory, subcategory).get(childPosition);
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
        thirdLevelTextView.setText(ProcedureInfo.removeCode(childText));
        if (setBottomTextColor)
            thirdLevelTextView.setTextColor(bottomTextColor);
        else
            thirdLevelTextView.setTextColor(Color.BLACK);

        final String subcategoryText = (String)getGroup(groupPosition);
        if (!ProcedureInfo.isExtremity(currentCategory, subcategoryText, childText)){
            // Display the cost of procedure using the description/child text
            convertView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    displayCost(childText);
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
            return ProcedureInfo.getExtremityHeaders(currentCategory, (String)getGroup(groupPosition)).size();
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
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group_second, parent, false);

            // SET BACKGROUND COLOR HERE
            if (setMidColor)
                convertView.setBackgroundColor(midColor);
            else
                convertView.setBackgroundColor(Color.GRAY);
        }
        TextView secondLevelTextView = (TextView) convertView
                .findViewById(R.id.listHeaderSecond);
        secondLevelTextView.setText(headerTitle);
        secondLevelTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        if (setMidTextColor)
            secondLevelTextView.setTextColor(midTextColor);
        else
            secondLevelTextView.setTextColor(Color.WHITE);

        // If the current category is other, then display the cost of the surgery
        if (currentCategory.equals(ProcedureInfo.MISC_CATEGORY_TITLE)){
            convertView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    displayCost(headerTitle);
                }
            });
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
        ArrayList<String> procedures = ProcedureInfo.getExtremityProcedureDescriptions(category, subCategory, extremity);
        Intent intent = new Intent(context, SurgeryCodesActivity.class);
        intent.putExtra("procedures", procedures);
        context.startActivity(intent);
    }

    private void displayCost(String description){
        SurgeryCostRetrieveTask task = new SurgeryCostRetrieveTask();
        task.addListener(this);
        task.execute(ProcedureInfo.getCode(description), description);
    }

    /**
     * Displays the results in the results activity.
     * @param cost The cost of the given procedure.
     */
    @Override
    public void onCostRetrieval(int cost, String description) {
        String value = NumberFormat.getNumberInstance(Locale.US).format(cost);
        value = String.format("The estimated patient cost of this procedure is $" + value);
        startResultsActivity(ProcedureInfo.removeCode(description), value);
    }

    /**
     * Creates an intent and starts the surgery codes activity.
     * @param message The message to display in the new activity.
     * @param codes The codes and their associated descriptions.
     */
    private void startSurgeryCodesActivity(String message, HashMap<String, String> codes){
        Intent intent = new Intent(context, SurgeryCodesActivity.class);
        intent.putExtra("message", message);
        intent.putExtra("codes", codes);
        context.startActivity(intent);
    }

    /**
     * Creates an intent and starts the result activity.
     * @param query The query to display in the results activity
     * @param result The result message to display in the results activity.
     */
    private void startResultsActivity(String query, String result){
        Intent intent = new Intent(context, ResultsActivity.class);
        intent.putExtra("query", query);
        intent.putExtra("result", result);
        context.startActivity(intent);
    }

}