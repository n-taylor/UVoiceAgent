package ute.webservice.voiceagent.oncall;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ute.webservice.voiceagent.R;
import ute.webservice.voiceagent.activities.OnCallListActivity;
import ute.webservice.voiceagent.util.Controller;

/**
 * A controller for the OnCallListActivity
 * Created by Nathan Taylor on 5/8/2018.
 */

public class OnCallController extends Controller {

    private static String phone_regex = ":\\s*([\\d-]+)";
    private static ArrayList<String> areas;

    public static String NO_NUMBERS_AVAILABLE_LABEL = "No phone numbers available";

    /**
     * Provides the base number that extensions build off of.
     */
    private static String hospitalBasePhoneNumber = "8015810000";

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
            InputStream input = context.getResources().openRawResource(R.raw.oncall_semicolon);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line;
            while((line = reader.readLine()) != null){
                contents.append(line);
            }
            String[] areaNames = contents.toString().split(";");
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

    /**
     * Given an on-call area description, removes the code. That is, given the format
     * 'AREA NAME [12345]'
     * returns 'AREA NAME'
     */
    public static String removeCode(String description){
        return description.replaceAll("\\[\\d+]", "").trim();
    }

    /**
     * Given an on-call description, extracts and returns the code embedded. That is, for
     * 'AREA NAME [12345]'
     * returns '12345'
     *
     * Returns null if a match is not found.
     */
    public static String extractOCMID(String description){
        String code = null;
        Pattern pattern = Pattern.compile(".\\[(\\d+)]");
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()){
            code = matcher.group(1);
        }
        return code;
    }

    /**
     * To be called when the user presses a phone number
     * @param context
     */
    public static void onPhoneNumberPressed(Context context, String phoneDescrption){
        String number = extractPhoneNumber(phoneDescrption);
        Intent callIntent = new Intent(Intent.ACTION_DIAL);

        String toDial = "tel:+1";
        // If the number extracted is not a full number, append it as an extension to the base phone number
        if (number.length() < 7){
//            number = hospitalBasePhoneNumber.substring(0, hospitalBasePhoneNumber.length() - number.length()) + number;
            return;
        }
        else if (number.replace("-", "").length() == 7){
            number = "801-" + number;
        }
        toDial += number;

        callIntent.setData(Uri.parse(toDial));
        context.startActivity(callIntent);
    }

    private static String extractPhoneNumber(String description){
        String number = "";
        Pattern pattern = Pattern.compile(phone_regex);
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()){
            number = matcher.group(1);
        }
        return number;
    }

    /**
     * This should get called when an on-call area is selected from the OnCallListActivity.
     * Does nothing if the description is null or empty or if the code is not included.
     *
     * @param description The full description (including the code) of the on-call area
     */
    public static void onAreaPressed(Context context, String description){
        if (description == null || description.isEmpty()){
            return;
        }
        String ocmid = extractOCMID(description);
        if (ocmid == null || ocmid.isEmpty())
            return;

        // Get the phone numbers of the on-call area
        displayPhoneNumbers(context, ocmid, removeCode(description));
    }

}
