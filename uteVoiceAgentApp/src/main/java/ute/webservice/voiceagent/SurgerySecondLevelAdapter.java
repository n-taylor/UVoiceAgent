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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nathan Taylor on 3/20/2018.
 */

public class SurgerySecondLevelAdapter extends BaseExpandableListAdapter implements SurgeryCodeRetrievalListener{

    private Context context;
    private ArrayList<String> headers;
    private Map<String, ArrayList<String>> children;

    private String currentCategory; // the category (parent-level header) under which this is displayed

    private boolean setMidColor;
    private boolean setBottomColor;
    private int midColor;
    private int bottomColor;

    private boolean setMidTextColor;
    private boolean setBottomTextColor;
    private int midTextColor;
    private int bottomTextColor;

    public SurgerySecondLevelAdapter(Context context, ArrayList<String> headers, Map<String, ArrayList<String>> children){
        this.context = context;
        this.headers = new ArrayList<>();
        this.headers.addAll(headers);
        this.children = new HashMap<>();
        for (String key : children.keySet()){
            this.children.put(key, children.get(key));
        }
    }

    @Override
    public Object getChild(int groupPosition, int childPosition)
    {
        return this.children.get(this.headers.get(groupPosition))
                .get(childPosition);
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
        thirdLevelTextView.setText(childText);
        if (setBottomTextColor)
            thirdLevelTextView.setTextColor(bottomTextColor);
        else
            thirdLevelTextView.setTextColor(Color.BLACK);

        // Call the results page when the third-level view is clicked.
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displaySurgeries(currentCategory, (String)getGroup(groupPosition), childText);
            }
        });
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition)
    {
        try {
            return this.children.get(this.headers.get(groupPosition)).size();
        } catch (Exception e) {
            return 0;
        }
    }
    @Override
    public Object getGroup(int groupPosition)
    {
        return this.headers.get(groupPosition);
    }
    @Override
    public int getGroupCount()
    {
        return this.headers.size();
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
        String headerTitle = (String) getGroup(groupPosition);
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
     * Makes a call to the server to retrieve the specific surgeries and their codes.
     */
    private void displaySurgeries(final String category, final String subcategory, final String extremity){
        SurgeryCodeRetrieveTask task = new SurgeryCodeRetrieveTask();
        task.addListener(this);
        task.execute(category, subcategory, extremity);
    }

    /**
     * If codes is null or empty, displays an error message on the results activity. Otherwise,
     * if there is only one sugery type in the hash map, displays the surgery cost in the results activity.
     * Otherwise, displays all the surgeries in the surgery codes activity.
     * @param codes
     */
    public void onCodeRetrieval(HashMap<String, String> codes){
        if (codes == null || codes.size() <= 0){
            startResultsActivity("", "No procedures were returned.");
        }
        else if (codes.size() == 1){
            for (String code : codes.keySet()){
                displaySurgeryCost(code, codes.get(code));
            }
        }
        else {
            startSurgeryCodesActivity(null, codes);
        }
    }

    /**
     * Sends a REST call to the server to get the cost of the given procedure.
     * @param code The procedure code
     * @param description The description to display.
     */
    private void displaySurgeryCost(String code, String description){
        // TODO: Make a class to query the surgery cost and execute it here
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