package ute.webservice.voiceagent;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
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

    /**
     * Gets the number of parent-level headers.
     */
    @Override
    public int getGroupCount() {
        return parentLevelHeaders.size();
    }

    /**
     * Gets the number of children (second-level) headers at a specified index.
     *
     * @param i The index of the parent-level header to check.
     * @return the number of second-level headers, or -1 if i is not a valid index, or -2 if
     * the parent headers list has errors.
     */
    @Override
    public int getChildrenCount(int i) {
        if (i < 0 || i >= getGroupCount())
            return -1;
        else if (secondLevel_Map.containsKey(parentLevelHeaders.get(i))){
            return secondLevel_Map.get(parentLevelHeaders.get(i)).size();
        }
        else
            return -2;
    }

    /**
     * Gets the data associated with the given group.
     * In this case, gets the list of second-level headers (strings) associated with the given header.
     * @param i The index of the parent-level header.
     * @return the List (of type String) of second-level headers, or null if the index is not valid.
     */
    @Override
    public Object getGroup(int i) {
        if (i < 0 || i >= getGroupCount())
            return null;
        else{
            return parentLevelHeaders.get(i);
        }
    }

    /**
     * Gets the data associated with the given child within the given group.
     * In this case, gets a list of third-level headers.
     * @param i The index of the parent-level header
     * @param i1 The index of the second-level header.
     * @return a List (of type String) of third-level headers, or null if an index is invalid
     */
    @Override
    public Object getChild(int i, int i1) {
        if (i < 0 || i >= getGroupCount())
            return null;
        if (i1 < 0 || i1 >= secondLevel_Map.size())
            return null;
        String key = parentLevelHeaders.get(i);
        if (secondLevel_Map.containsKey(key)){
            String key1 = secondLevel_Map.get(key).get(i1);
            if (thirdLevel_Map.containsKey(key1)){
                return thirdLevel_Map.get(key1);
            }
        }
        return null;
    }

    /**
     * Gets the ID of the parent-level header specified.
     * @param i The index of the header.
     * @return the hash of the header name plus its index, or -1 if the index is invalid.
     */
    @Override
    public long getGroupId(int i) {
        if (i < 0 || i >= getGroupCount())
            return -1;
        return parentLevelHeaders.get(i).hashCode() + i;
    }

    /**
     * Gets the ID of the second-level header specified.
     * @param i The index of the parent-level header.
     * @param i1 The index of the second-level header.
     * @return the hash of the header name plus its index, or -1 if the index is invalid.
     */
    @Override
    public long getChildId(int i, int i1) {
        if (i < 0 || i >= getGroupCount())
            return -1;
        if (!secondLevel_Map.containsKey(parentLevelHeaders.get(i)))
            return -1;
        if (i1 < 0 || i1 >= secondLevel_Map.get(parentLevelHeaders.get(i)).size())
            return -1;
        return secondLevel_Map.get(parentLevelHeaders.get(i)).get(i1).hashCode() + i1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group, parent, false);
        }
        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.listHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setTextColor(Color.BLUE);
        lblListHeader.setText(headerTitle);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        ExpandableListView secondLevelView = new ExpandableListView(context);
        String parentNode = (String) getGroup(groupPosition);
        secondLevelView.setAdapter(new SurgerySecondLevelAdapter(context, secondLevel_Map.get(parentNode), thirdLevel_Map));
        secondLevelView.setGroupIndicator(null);
        return secondLevelView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}

