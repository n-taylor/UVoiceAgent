package ute.webservice.voiceagent.oncall;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import ute.webservice.voiceagent.util.ParseResult;

/**
 * Created by Nathan Taylor on 4/2/2018.
 */

public class ClientTestEmulator {

    // Singleton Implementation: In the context of this app,
    private static ClientTestEmulator _singletonInstance = null;

    // Default constructor is private to make sure it can't be accessed outside
    // the class
    private ClientTestEmulator () {
    }

    /**
     * Returns single instance of ClientTestEmulator
     * @return Instance of ClientTestEmulator
     */
    public static ClientTestEmulator getSingleClientEmulator() {
        if (_singletonInstance == null) {
            _singletonInstance = new ClientTestEmulator ();
        }
        return _singletonInstance;
    }

    /**
     * Waits on response from server
     * @param socket Server socket
     */
    public void readServerResponse(Socket socket) {
        final Socket s = socket;
        try {
//            //  this thread will close socket in a second
//            new Thread() {
//                public void run() {
//                    try {
//                        Thread.sleep(5000);
//                        s.close();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                };
//            }.start();

//            BufferedReader serverReader = new BufferedReader(
//                    new InputStreamReader(socket.getInputStream()));
//
//            String serverResponse = null;
//            while ((serverResponse = serverReader.readLine()) != null) {
//                System.out.println("Server Response: " + serverResponse);
//               if (serverResponse.contains("</procedureResult>"))
//                    break;
//                if (serverResponse.isEmpty()) {
//                    break;
//                }
//            }
//            serverReader.close();

            ParseResult PR = new ParseResult();
            HashMap<String, String> assignments = PR.parseCurrentAssignments(socket.getInputStream());

            for (String key : assignments.keySet()){
                System.out.println(key + " -> " + assignments.get(key));
            }

//
//            socket.close();

        } catch (IOException ex) {
            System.out.println("Error: Unable to read server response\n\t" + ex);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

}
