package ute.webservice.voiceagent.oncall.util;

import android.os.AsyncTask;

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
 * This class retrieves a list of phone numbers for each personnel that is on call for a
 * specific on-call group. The execute method of this class should be passed only the OCMID
 * for the specific group.
 *
 * Created by Nathan Taylor on 4/4/2018.
 */
public class OnCallRetrieveTask extends AsyncTask<String, Void, HashMap<String, ArrayList<String>>> {

    private ArrayList<OnCallRetrievalListener> listeners;

    private ParseResult PR;

    public OnCallRetrieveTask(){
        listeners = new ArrayList<>();
        PR = new ParseResult();

    }
    @Override
    protected HashMap<String, ArrayList<String>> doInBackground(String... strings) {
        if (strings.length < 1)
            return null;

        HashMap<String, String> mids = getMIDs(strings[0]);

        for (String key : mids.keySet()){
            System.out.println(key + " -> " + mids.get(key));
        }

        HashMap<String, ArrayList<String>> numbers = getNumbers(mids);

        return numbers;
    }

    @Override
    protected void onPostExecute(HashMap<String, ArrayList<String>> numbers){
        for (OnCallRetrievalListener listener : listeners){
            listener.onOnCallRetrieval(numbers);
        }
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

    public void addListener(OnCallRetrievalListener listener){
        listeners.add(listener);
    }

}
