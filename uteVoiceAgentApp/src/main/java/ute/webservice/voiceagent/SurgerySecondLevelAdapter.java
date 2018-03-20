package ute.webservice.voiceagent;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nathan Taylor on 3/20/2018.
 */

public class SurgerySecondLevelAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> headers;
    private Map<String, List<String>> children;

    public SurgerySecondLevelAdapter(Context context, List<String> headers, Map<String, List<String>> children){
        this.context = context;
        this.headers = new ArrayList<>();
        this.headers.addAll(headers);
        this.children = new HashMap<>();
        for (String key : children.keySet()){
            this.children.put(key, children.get(key));
        }
    }

    /**
     * Gets the number of parent-level headers.
     */
    @Override
    public int getGroupCount() {
        return headers.size();
    }

    /**
     * Gets the number of third-level items at a specified index.
     *
     * @param groupPosition The index of the second-level header to check.
     * @return the number of third-level items, or -1 if i is not a valid index, or -2 if
     * the parent headers list has errors.
     */
    @Override
    public int getChildrenCount(int groupPosition)
    {
        try {
            return this.children.get(this.headers.get(groupPosition)).size();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Gets the data associated with the given group.
     * In this case, gets the list of third-level items associated with the given second-level header.
     * @param i The index of the second-level header.
     * @return the List (of type String) of third-level items, or null if the index is not valid.
     */
    @Override
    public Object getGroup(int i) {
        if (i < 0 || i >= getGroupCount())
            return null;
        else{
            return headers.get(i);
        }
    }

    /**
     * Gets the data associated with the given child within the given group.
     * In this case, gets a third-level item.
     * @param i The index of the second-level header
     * @param i1 The index of the third-level item.
     * @return a third-level item, or null if an index is invalid
     */
    @Override
    public Object getChild(int i, int i1) {
        if (i < 0 || i >= getGroupCount())
            return null;
        if (i1 < 0 || i1 >= children.size())
            return null;
        String key = headers.get(i);
        if (children.containsKey(key)){
            return children.get(key).get(i1);
        }
        return null;
    }

    /**
     * Gets the ID of the second-level header specified.
     * @param i The index of the header.
     * @return the hash of the header name plus its index, or -1 if the index is invalid.
     */
    @Override
    public long getGroupId(int i) {
        if (i < 0 || i >= getGroupCount())
            return -1;
        return headers.get(i).hashCode() + i;
    }

    /**
     * Gets the ID of the third-level header specified.
     * @param i The index of the second-level header.
     * @param i1 The index of the third-level header.
     * @return the hash of the header name plus its index, or -1 if the index is invalid.
     */
    @Override
    public long getChildId(int i, int i1) {
        if (i < 0 || i >= getGroupCount())
            return -1;
        if (!children.containsKey(headers.get(i)))
            return -1;
        if (i1 < 0 || i1 >= children.get(headers.get(i)).size())
            return -1;
        return children.get(headers.get(i)).get(i1).hashCode() + i1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
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
        }
        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.listHeader);
        lblListHeader.setText(headerTitle);
        lblListHeader.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        lblListHeader.setTextColor(Color.GREEN);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent)
    {
        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_items, parent, false);
        }
        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.listItem);
        //txtListChild.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        txtListChild.setText(childText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
