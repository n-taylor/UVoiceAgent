package ute.webservice.voiceagent;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by Nathan Taylor on 3/20/2018.
 */

public class SurgeryParentListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> parentLevelHeaders;
    private Map<String, List<String>> secondLevel_Map;
    private Map<String, List<String>> thirdLevel_Map;
    private int width = 800; // the width of the second and third-level headers

    private boolean setTopColor = false;
    private boolean setMidColor = false;
    private boolean setBottomColor = false;
    private int topColor;
    private int midColor;
    private int bottomColor;

    private boolean setTopTextColor = false;
    private boolean setMidTextColor = false;
    private boolean setBottomTextColor = false;
    private int topTextColor;
    private int midTextColor;
    private int bottomTextColor;

    public SurgeryParentListAdapter(Context context, List<String> parentLevelHeaders, Map<String, List<String>> secondLevel_Map,
                                    Map<String, List<String>> thirdLevel_Map){
        this.context = context;

        this.parentLevelHeaders = new ArrayList<>();
        this.parentLevelHeaders.addAll(parentLevelHeaders);

        this.secondLevel_Map = new HashMap<>();
        for (String key : secondLevel_Map.keySet()){
            this.secondLevel_Map.put(key, secondLevel_Map.get(key));
        }

        this.thirdLevel_Map = new HashMap<>();
        for (String key : thirdLevel_Map.keySet()){
            this.thirdLevel_Map.put(key, thirdLevel_Map.get(key));
        }

    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childPosition;
    }
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }


    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final CustomExpListView secondLevelExpListView = new CustomExpListView(this.context);
        //final ExpandableListView expListView = new ExpandableListView(context);
        String parentNode = (String) getGroup(groupPosition);
        SurgerySecondLevelAdapter secondLevel = new SurgerySecondLevelAdapter(this.context, secondLevel_Map.get(parentNode),
                thirdLevel_Map);
        if (setMidColor)
            secondLevel.setMidColor(midColor);
        if (setBottomColor)
            secondLevel.setBottomColor(bottomColor);

        if (setMidTextColor)
            secondLevel.setMidTextColor(midTextColor);
        if (setBottomTextColor)
            secondLevel.setBottomTextColor(bottomTextColor);

        secondLevelExpListView.setAdapter(secondLevel);
        secondLevelExpListView.setGroupIndicator(null);
        secondLevelExpListView.setPreferredWidth(width);
        return secondLevelExpListView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }
    @Override
    public Object getGroup(int groupPosition) {
        return this.parentLevelHeaders.get(groupPosition);
    }
    @Override
    public int getGroupCount() {
        return this.parentLevelHeaders.size();
    }
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }


    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group, null);
            // SET BACKGROUND COLOR HERE
            if (setTopColor)
                convertView.setBackgroundColor(topColor);
            else
                convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.color_slategrey));
        }
        TextView headerTextView = (TextView) convertView
                .findViewById(R.id.listHeader);
        headerTextView.setTypeface(null, Typeface.BOLD);
        headerTextView.setText(headerTitle);
        if (setTopTextColor)
            headerTextView.setTextColor(topTextColor);
        else
            headerTextView.setTextColor(Color.WHITE);
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
     * Sets the background color of the third-level items
     * @param color The resolved color int (for example, use ContextCompat.getColor(this, R.color.color_slategrey))
     */
    public void setBottomColor(int color){
        bottomColor = color;
        setBottomColor = true;
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

    /**
     * Sets the text color of the third-level items
     * @param color The resolved color int (for example, use ContextCompat.getColor(this, R.color.color_slategrey))
     */
    public void setBottomTextColor(int color){
        bottomTextColor = color;
        setBottomTextColor = true;
    }
}