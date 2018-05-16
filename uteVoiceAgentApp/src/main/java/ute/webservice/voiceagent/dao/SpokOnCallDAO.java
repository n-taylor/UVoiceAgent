package ute.webservice.voiceagent.dao;

import android.content.Context;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import ute.webservice.voiceagent.oncall.util.SpokParser;
import ute.webservice.voiceagent.util.ParseResult;

/**
 * Created by Nathan Taylor on 4/11/2018.
 */

public class SpokOnCallDAO implements OnCallDAO {

    private ParseResult PR;

    public SpokOnCallDAO(){
        PR = new ParseResult();
    }

    private final int timeout = 5000;
    private static String IPAddress = "155.100.69.40";


    /**
     * Given an OCMID, maps all of the on-call assignments' names to their available phone numbers.
     *
     * @param OCMID
     * @return
     */
    @Override
    public HashMap<String, ArrayList<String>> getPhoneNumbers(Context context, String OCMID) {
        if (OCMID.equals("000")){
            HashMap<String, ArrayList<String>> testNumsMap = new HashMap<>();
            ArrayList<String> testNums = new ArrayList<>();
            testNums.add("801-420-8471 : Test Number");
            testNumsMap.put("Nathan", testNums);
            return testNumsMap;
        }
        HashMap<String, String> mids = getMIDs(OCMID); // maps MID -> Name

        if (mids == null)
        {
            return null;
        }
        HashMap<String, ArrayList<String>> numbers = getNumbers(mids);
        return numbers;
    }

    /**
     * Given a mapping of MIDs to Names, makes a call to retrieve the phone numbers associated
     * with the MID.
     * @param mids MID -> Name
     * @return A mapping of Names to phone numbers
     */
    private HashMap<String, ArrayList<String>> getNumbers(HashMap<String, String> mids) {
        try {
            HashMap<String, ArrayList<String>> numbers = new HashMap<>();

            for (String mid : mids.keySet()) {

                Socket socket = new Socket(IPAddress, 9720);

                socket.setSoTimeout(timeout);

                // Create the XML string to send
                String toRead = ParseResult.getPhoneNumberCall(mid);
                BufferedReader reader = new BufferedReader(new StringReader(toRead));
                String line;
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                // Send xml data to server

                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println(stringBuilder.toString());

                ArrayList<String> phoneNumbers = PR.parsePhoneNumbers(socket.getInputStream());
                phoneNumbers = appendPagers(phoneNumbers, mid);
                numbers.put(mids.get(mid), phoneNumbers);

                writer.close();
                if (!socket.isClosed())
                    socket.close();
            }

            return numbers;

        } catch (XmlPullParserException | IOException ex) {
            return null;
        }
    }

    /**
     * Given a list of phone numbers, uses the given socket to make a call to the Spok webservice to retrieve any pager IDs
     * associated with the given mid.
     * @param numbers
     * @return an empty ArrayList<String> if an error occurs
     */
    private ArrayList<String> appendPagers(ArrayList<String> numbers, String mid){
        try {

            Socket socket = new Socket(IPAddress, 9720);

            socket.setSoTimeout(timeout);

            // Create the XML string to send
            String toRead = SpokParser.getPagerIdCall(mid);
            BufferedReader reader = new BufferedReader(new StringReader(toRead));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            // Send xml data to server
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(stringBuilder.toString());

            ArrayList<String> pagerIds = SpokParser.parsePagerIdResponse(socket.getInputStream());
            numbers.addAll(pagerIds);
        }
        catch (IOException | XmlPullParserException ex){
            ex.printStackTrace();
            return numbers;
        }
        return numbers;
    }

    private HashMap<String, String> getMIDs(String OCMID){
        try {
            Socket socket = new Socket(IPAddress, 9720);
            socket.setSoTimeout(timeout);
            String toRead = ParseResult.getCurrentAssignmentsCall(OCMID);
            BufferedReader reader = new BufferedReader(new StringReader(toRead));
            String line;
            StringBuilder  stringBuilder = new StringBuilder();
            while((line = reader.readLine() ) != null) {
                stringBuilder.append(line);
            }

            // Send xml data to server

            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(stringBuilder.toString());

            // Wait for server response
            HashMap<String, String> assignments = PR.parseCurrentAssignments(socket.getInputStream());
            if (!socket.isClosed())
                socket.close();
            writer.close();

            return assignments;

        } catch (XmlPullParserException | IOException ex)
        {
            return null;
        }
    }
}
