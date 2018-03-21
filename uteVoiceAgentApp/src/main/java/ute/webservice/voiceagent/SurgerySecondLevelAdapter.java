package ute.webservice.voiceagent;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
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
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent)
    {
        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_items, parent, false);

            // DEFINE BACKGROUND COLOR HERE
            convertView.setBackgroundColor(Color.WHITE);
        }
        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.listItem);
        //txtListChild.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        txtListChild.setText(childText);
        txtListChild.setTextColor(Color.BLACK);
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
            convertView.setBackgroundColor(Color.GRAY);
        }
        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.listHeaderSecond);
        lblListHeader.setText(headerTitle);
        lblListHeader.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        lblListHeader.setTextColor(Color.WHITE);
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
}
