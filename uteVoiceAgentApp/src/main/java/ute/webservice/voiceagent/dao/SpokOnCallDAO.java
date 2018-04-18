package ute.webservice.voiceagent.dao;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import ute.webservice.voiceagent.util.ParseResult;

/**
 * Created by Nathan Taylor on 4/11/2018.
 */

public class SpokOnCallDAO implements OnCallDAO {

    private ParseResult PR;

    public SpokOnCallDAO(){
        PR = new ParseResult();
    }


    /**
     * Given an OCMID, maps all of the on-call assignments' names to their available phone numbers.
     *
     * @param OCMID
     * @return
     */
    @Override
    public HashMap<String, ArrayList<String>> getPhoneNumbers(String OCMID) {
        HashMap<String, String> mids = getMIDs(OCMID);
        HashMap<String, ArrayList<String>> numbers = getNumbers(mids);
        return numbers;
    }

    private HashMap<String, ArrayList<String>> getNumbers(HashMap<String, String> mids){
        try {
            HashMap<String, ArrayList<String>> numbers = new HashMap<>();

            for (String mid : mids.keySet()){

                Socket socket = new Socket("155.100.69.40", 9720);

                // Create the XML string to send
                String toRead = ParseResult.getPhoneNumberCall(mid);
                BufferedReader reader = new BufferedReader(new StringReader(toRead));
                String line;
                StringBuilder  stringBuilder = new StringBuilder();
                while((line = reader.readLine() ) != null) {
                    stringBuilder.append(line);
                }

                // Send xml data to server

                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println(stringBuilder.toString());

                numbers.put(mids.get(mid), PR.parsePhoneNumbers(socket.getInputStream()));

                writer.close();
                if (!socket.isClosed())
                    socket.close();
            }

            return numbers;

        } catch (XmlPullParserException | IOException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    private HashMap<String, String> getMIDs(String OCMID){
        try {
            Socket socket = new Socket("155.100.69.40", 9720);
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
            ex.printStackTrace();
        }
        return null;
    }
}