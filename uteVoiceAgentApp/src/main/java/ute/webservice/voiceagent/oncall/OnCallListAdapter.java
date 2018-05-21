package ute.webservice.voiceagent.oncall;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ute.webservice.voiceagent.R;
import ute.webservice.voiceagent.util.Controller;

/**
 * Created by Nathan Taylor on 4/4/2018.
 */

public class OnCallListAdapter extends BaseExpandableListAdapter {

    private Context context;

    private ArrayList<String> names;
    private HashMap<String, ArrayList<String>> numbers;

    private int width = 200;
    private boolean setTopTextColor;
    private boolean setMidTextColor;
    private boolean setTopColor;
    private boolean setMidColor;

    private int topColor;
    private int topTextColor;
    private int midColor;
    private int midTextColor;

    public OnCallListAdapter(Context context, ArrayList<String> names, HashMap<String, ArrayList<String>> numbers){
        this.context = context;
        this.names = names;
        this.numbers = numbers;
    }

    @Override
    public int getGroupCount() {
        return names.size();
    }

    @Override
    public int getChildrenCount(int i) {
        if (i < 0 || i >= names.size())
            return 0;
        return numbers.get(names.get(i)).size();
    }

    @Override
    public Object getGroup(int i) {
        if (i < 0 || i >= getGroupCount())
            return null;
        return names.get(i);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (groupPosition < 0 || groupPosition >= getGroupCount())
        return null;
        String name = (String) getGroup(groupPosition);
        ArrayList<String> nums = numbers.get(name);

        if (childPosition < 0 || childPosition >= nums.size())
            return null;
        return nums.get(childPosition);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        final String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group, null);
        }
        TextView headerTextView = (TextView) convertView
                .findViewById(R.id.listHeader);
        headerTextView.setTypeface(null, Typeface.BOLD);
        headerTextView.setText(headerTitle);
        if (setTopTextColor)
            headerTextView.setTextColor(topTextColor);
        else
            headerTextView.setTextColor(Color.WHITE);
        // SET BACKGROUND COLOR HERE
        if (setTopColor)
            convertView.setBackgroundColor(topColor);
        else
            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.color_slategrey));
        if (groupPosition == 0)
        {
            convertView.setBackgroundResource(R.drawable.menushape);
        }
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent)
    {
        final String childText = (String) getChild(groupPosition, childPosition);

        LayoutInflater layoutInflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(R.layout.list_items, parent, false);

        // DEFINE BACKGROUND COLOR HERE
        if (setMidColor)
            convertView.setBackgroundColor(midColor);
        else
            convertView.setBackgroundColor(Color.WHITE);

        TextView childTextView = (TextView) convertView
                .findViewById(R.id.listItem);
        //txtListChild.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        childTextView.setText(Controller.getProceduresDAO().removeCode(childText));
        childTextView.setWidth(width);
        if (setMidTextColor)
            childTextView.setTextColor(midTextColor);
        else
            childTextView.setTextColor(Color.BLACK);

        if (!childText.toLowerCase().contains("pager") && !childText.equals(OnCallController.NO_NUMBERS_AVAILABLE_LABEL)){
            convertView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    OnCallController.onPhoneNumberPressed(context, childText);
                }
            });
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return (getChild(i, i1) != null);
    }

    /**
     * Sets the width of the second and third-level headers
     * @param width
     */
    public void setWidth(int width){
        this.width = width;
    }

    /**
     * Sets the color of the parent-level items
     * @param color The resolved color int (for example, use ContextCompat.getColor(this, R.color.color_slategrey))
     */
    public void setTopColor(int color){
        topColor = color;
        setTopColor = true;
    }

    /**
     * Sets the background color of the second-level items
     * @param color The resolved color int (for example, use ContextCompat.getColor(this, R.color.color_slategrey))
     */
    public void setMidColor(int color){
        midColor = color;
        setMidColor = true;
    }

    /**
     * Sets the text color of the parent-level items
     * @param color The resolved color int (for example, use ContextCompat.getColor(this, R.color.color_slategrey))
     */
    public void setTopTextColor(int color){
        topTextColor = color;
        setTopTextColor = true;
    }

    /**
     * Sets the text color of the second-level items
     * @param color The resolved color int (for example, use ContextCompat.getColor(this, R.color.color_slategrey))
     */
    public void setMidTextColor(int color){
        midTextColor = color;
        setMidTextColor = true;
    }
}
