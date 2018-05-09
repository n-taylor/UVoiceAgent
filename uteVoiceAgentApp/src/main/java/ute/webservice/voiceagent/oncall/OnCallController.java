package ute.webservice.voiceagent.oncall;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ute.webservice.voiceagent.R;
import ute.webservice.voiceagent.activities.OnCallListActivity;
import ute.webservice.voiceagent.util.Controller;

/**
 * A controller for the OnCallListActivity
 * Created by Nathan Taylor on 5/8/2018.
 */

public class OnCallController extends Controller {

    private static ArrayList<String> areas;
    private static String fileName = "oncall_areas.txt";

    /**
     * Given a search string, populates the list view with all the On Call areas containing the search parameter.
     * If the search string is null or empty, populates the list view with all On Call areas.
     *
     * @param context The context of the OnCallListActivity
     * @param listView The list view to populate
     * @param search The search to narrow results by.
     */
    public void populateListView(Context context, ListView listView, String search){
        ArrayList<String> list = (search != null && !search.isEmpty()) ? getAreas(context, search) : getAreas(context);
        AreasAdapter adapter = new AreasAdapter(context, list);
        adapter.setBackColor(ContextCompat.getColor(context, R.color.color_slategrey));
        adapter.setTextColor(Color.WHITE);
        listView.setAdapter(adapter);
    }

    /**
     * Fills the areas array list from the appropriate source (most likely a .txt file)
     * @param context Used to access the file. Must not be null.
     * @return The complete list of on-call areas
     */
    private ArrayList<String> populateAreas(Context context){
        ArrayList<String> list = new ArrayList<>();

//        list.add("Burn unit");
//        list.add("Neuro unit");
//        list.add("Other hospital units");

        try {
            StringBuilder contents = new StringBuilder();
            InputStream input = context.getResources().openRawResource(R.raw.oncall_areas);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line;
            while((line = reader.readLine()) != null){
                contents.append(line);
            }
            String[] areaNames = contents.toString().split(",");
            list = new ArrayList<String>(Arrays.asList(areaNames));
            reader.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        areas = list;
        return list;
    }

    /**
     * Gets all the on-call areas.
     *
     * @param context
     * @return A list of all on-call areas.
     */
    private ArrayList<String> getAreas(Context context){
        return (areas != null) ? areas : populateAreas(context);
    }

    /**
     * Gets only the areas that contain the given search parameter.
     *
     * @param context
     * @param search
     * @return A list of all areas whose description contains the search parameter.
     */
    private ArrayList<String> getAreas(Context context, String search){
        ArrayList<String> list = new ArrayList<>();
        for (String area : getAreas(context)){
            if (area.toLowerCase().contains(search.trim().toLowerCase())){
                list.add(area);
            }
        }
        return list;
    }

    /**
     * Call this when the search button is pressed to update the list view with the appropriate items.
     * @param activity The OnCallListActivity to update.
     */
    public void onSearchButtonPressed(OnCallListActivity activity){
        populateListView(activity.getBaseContext(), activity.getListView(), activity.getSearchText());
    }

}
