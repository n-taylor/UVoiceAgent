package ute.webservice.voiceagent.dao;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGetHC4;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import ute.webservice.voiceagent.openbeds.RoomStatus;
import ute.webservice.voiceagent.util.AccountCheck;
import ute.webservice.voiceagent.util.Constants;
import ute.webservice.voiceagent.util.ParseResult;

/**
 * Created by Nathan Taylor on 4/11/2018.
 */

public class EDWOpenBedsDAO implements OpenBedsDAO {

    public final static String TAG = EDWOpenBedsDAO.class.getName();

    // Open Beds constants
    public final static String CLINWEB_ALL_OPEN_BEDS_QUERY = "https://10.0.2.2:8042/bedCensus/all";
    public final static String CLINWEB_OPEN_BEDS_SPECFIC_QUERY = "https://10.0.2.2:8042/bedCensus/unit/";

    private static ArrayList<String> unitNames;
    private static HashMap<String, ArrayList<String>> unitCategories;

    /**
     * Retrieves the number of open beds in a given unit from EDW webservice.
     * @param unit The unit the beds are located in.
     * @return The number of available beds, or -1 if an error was encountered.
     */
    @Override
    public int getOpenBedCount(String unit) {
        int openBeds = -1;
        String trimmedUnit = unit.replace(" ", "").toUpperCase();
        String url = CLINWEB_OPEN_BEDS_SPECFIC_QUERY + trimmedUnit;

        try {
            HttpGetHC4 getRequest = new HttpGetHC4(url);
            CloseableHttpResponse response3 = AccountCheck.httpclient.execute(getRequest);
            HttpEntity entity = response3.getEntity();
            String responseString = "";

            if (entity != null) {
                BufferedReader rdSrch = new BufferedReader(
                        new InputStreamReader(response3.getEntity().getContent()));

                String lineSrch;
                while ((lineSrch = rdSrch.readLine()) != null) {
                    Log.d(TAG, lineSrch);
                    responseString += lineSrch;
                }

                if (responseString.equals(Constants.ACCESS_DENIED)) {
                    return Constants.ACCESS_DENIED_INT;
                } else {
                    ArrayList<RoomStatus> rooms = ParseResult.parseRooms(responseString);

                    if (rooms != null){
                        openBeds = rooms.get(0).getAvailableBeds();
                    }
                    return openBeds;
                }
            }

        } catch (Exception e) {
            return -1;
        }
        return openBeds;
    }

    /**
     * Retrieves information about the open bed count from the EDW webservice.
     * @return a mapping of all unit names to their respective count of available beds.
     */
    @Override
    public HashMap<String, Integer> getAllOpenBedCounts() {
        HashMap<String, Integer> openBeds = null;
        String responseString = "";

        try {

            HttpGetHC4 getRequest = new HttpGetHC4(CLINWEB_ALL_OPEN_BEDS_QUERY);
            long start = System.currentTimeMillis();
            CloseableHttpResponse response3 = AccountCheck.httpclient.execute(getRequest);
            long end = System.currentTimeMillis();
            long duration = end - start;
            HttpEntity entity = response3.getEntity();
            if (entity != null) {
                BufferedReader rdSrch = new BufferedReader(
                        new InputStreamReader(response3.getEntity().getContent()));

                String lineSrch;
                while ((lineSrch = rdSrch.readLine()) != null) {
                    Log.d(TAG, lineSrch);
                    responseString += lineSrch;
                }
                if (responseString.equals(Constants.ACCESS_DENIED)) {
                    responseString = "You are not allowed to access.";
                } else {
                    ArrayList<RoomStatus> formattedBeds = ParseResult.parseRooms(responseString);
                    openBeds = new HashMap<>();

                    for (RoomStatus r: formattedBeds) {
                        openBeds.put(r.getUnit(), r.getAvailableBeds());
                    }

                    return openBeds;
                }

            }

        } catch (Exception e) {
            return null;
        }

        return openBeds;
    }

    @Override
    public ArrayList<String> getAllUnitNames() {
        if (unitNames == null)
            fillUnits();
        return unitNames;
    }

    /**
     * Provides all the units organized into their categories/facilities.
     * @return A mapping of category/facility to list of units under that
     */
    @Override
    public HashMap<String, ArrayList<String>> getUnitCategories() {
        if (unitCategories == null)
            fillUnits();
        return unitCategories;
    }

    /**
     * Populates the unitNames and unitCategories data structures.
     */
    private void fillUnits(){
        unitNames = new ArrayList<>();
        unitCategories = new HashMap<>();

        // Adding child data
        unitNames.add("U Neuro Institute");
        unitNames.add("University Hospitals");
        unitNames.add("Huntsman Cancer Institute");

        // Adding child data

        ArrayList<String> UNI = new ArrayList<>();
        UNI.add("2A");
        UNI.add("2B");
        UNI.add("2EAST");
        UNI.add("2NORTH");
        UNI.add("2SOUTH");
        UNI.add("3NORTH");
        UNI.add("3SOUTH");
        UNI.add("4NORTH");
        UNI.add("4SOUTH");

        ArrayList<String> UH = new ArrayList<>();
        UH.add("5STB");
        UH.add("5W");
        UH.add("AIMA");
        UH.add("AIMB");
        UH.add("BRN");
        UH.add("CVICU");
        UH.add("CVMU");
        UH.add("ICN");
        UH.add("IMR");
        UH.add("LND");
        UH.add("MICU");
        UH.add("MNBC");
        UH.add("NAC");
        UH.add("NCCU");
        UH.add("NICU");
        UH.add("NSY");
        UH.add("OBGY");
        UH.add("OTSS");
        UH.add("SICU");
        UH.add("SSTU");
        UH.add("WP5");

        ArrayList<String> HC = new ArrayList<>();
        HC.add("HCBMT ");
        HC.add("HCH4");
        HC.add("HCH5");
        HC.add("HCICU");


        unitCategories.put(unitNames.get(0), UNI);
        unitCategories.put(unitNames.get(1), UH);
        unitCategories.put(unitNames.get(2), HC);
    }
}
