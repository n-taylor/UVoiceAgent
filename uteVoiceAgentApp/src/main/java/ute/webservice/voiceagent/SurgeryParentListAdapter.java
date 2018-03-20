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
        String parentNode = (String) getGroup(groupPosition);
        secondLevelExpListView.setAdapter(new SurgerySecondLevelAdapter(this.context, secondLevel_Map.get(parentNode),
                thirdLevel_Map));
        secondLevelExpListView.setGroupIndicator(null);
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
            convertView = layoutInflater.inflate(R.layout.list_group, parent, false);
        }
        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.listHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setTextColor(Color.CYAN);
        lblListHeader.setText(headerTitle);
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

