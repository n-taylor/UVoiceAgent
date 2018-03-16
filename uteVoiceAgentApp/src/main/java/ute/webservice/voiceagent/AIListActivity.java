package ute.webservice.voiceagent;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

/**
 * Created by u0450254 on 3/13/2018.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.TextView;
import android.widget.Toast;

import ai.api.ui.SoundLevelCircleDrawable;

public class AIListActivity extends Activity {

    ListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aibutton_sample_accordion);

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.resultListView);

        // preparing list data
        dummyListData();

        expListView.setBackgroundResource(R.drawable.menushape);

        listAdapter = new ListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);

    }

    /*
     * Preparing the list data
     */
    private void dummyListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add("North");
        listDataHeader.add("South");
        listDataHeader.add("East");
        listDataHeader.add("West");

        // Adding child data

        List<String> north = new ArrayList<String>();
        north.add("one ");
        north.add("Two");
        north.add("Three");

        List<String> south = new ArrayList<String>();
        south.add("one ");
        south.add("Two");
        south.add("Three");

        List<String> east = new ArrayList<String>();
        east.add("one ");
        east.add("Two");
        east.add("Three");

        List<String> west = new ArrayList<String>();
        west.add("one ");
        west.add("Two");
        west.add("Three");

        listDataChild.put(listDataHeader.get(0), north);
        listDataChild.put(listDataHeader.get(1), south);
        listDataChild.put(listDataHeader.get(2), east);
        listDataChild.put(listDataHeader.get(3), west);
    }
}
