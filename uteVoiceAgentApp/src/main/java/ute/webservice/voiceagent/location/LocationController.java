package ute.webservice.voiceagent.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import ute.webservice.voiceagent.activities.EquipmentFindActivity;
import ute.webservice.voiceagent.dao.LocationDAO;
import ute.webservice.voiceagent.exceptions.AccessDeniedException;
import ute.webservice.voiceagent.exceptions.InvalidResponseException;
import ute.webservice.voiceagent.util.Controller;

/**
 * Controller for the equipment finder and other location services
 *
 * Created by Nathan Taylor on 5/30/2018.
 */

public class LocationController extends Controller {

    private static final String CLIENT_LOCATION_ERROR = "An error occurred while retrieving the device location";

    private Bitmap bitmap;

    private ClientLocation clientLocation;
    private HashMap<String, TagLocation> tagLocations;
    private HashMap<String, Device> Devices;

    private static LocationController instance;

    private LocationController(){}


    public static LocationController getInstance(){
        if (instance == null){
            instance = new LocationController();
        }

        return instance;
        }

    public static void startActivity(Context context, Bitmap bitmap){
        LocationController.getInstance().bitmap = bitmap;
        Intent intent = new Intent(context, EquipmentFindActivity.class);
        context.startActivity(intent);
    }

    public Bitmap getImage(){
        return bitmap;
    }

    public String getImageName() { return clientLocation.getImageName(); }

    public void setClientLocation(ClientLocation location){
        clientLocation = location;
    }

    public MapCoordinate getUserLocation(){
        return clientLocation.getMapCoordinate();
    }

    public MapDimension getDimensions(){
        return clientLocation.getMapDimension();
    }

    /**
     * Updates the location of the tag associated with the given ID.
     * @param id
     * @param context
     */
    public void findTagLocation(String id, Context context){
        GetTagLocationTask task = new GetTagLocationTask(id, context);
        try {
            task.execute();
            task.get();
        }
        catch(InterruptedException ie)
        {

        }
        catch(ExecutionException ee)
        {

        }
    }

    public void findDeviceInfo(String id, Context context){

        Devices = new HashMap<>();

        findTagLocation("00:12:b8:0d:6b:58", context);
        // findTagLocation("00:12:b8:0d:68:ad", context);
       // findTagLocation("00:12:b8:0d:68:90", context);
       // findTagLocation("00:12:b8:0d:6c:07", context);
       // findTagLocation("00:12:b8:0d:5c:07", context);
       // findTagLocation("00:12:b8:0d:21:66", context);
       // findTagLocation("00:12:b8:0d:26:0d", context);
       // findTagLocation("00:12:b8:0d:68:6b", context);
       // findTagLocation("00:12:b8:0d:59:5e", context);
       // findTagLocation("00:12:b8:0d:5b:c8", context);

        Device device1 = new Device("00:12:b8:0d:6b:58",  "testTag", tagLocations.get("00:12:b8:0d:6b:58"));
      //  Device device2 = new Device("00:12:b8:0d:68:ad",  "testTag", tagLocations.get("00:12:b8:0d:68:ad"));
      //  Device device3 = new Device("00:12:b8:0d:68:90",  "testTag", tagLocations.get("00:12:b8:0d:68:90"));
      //  Device device4 = new Device("00:12:b8:0d:6c:07",  "testTag", tagLocations.get("00:12:b8:0d:6c:07"));
     //   Device device5 = new Device("00:12:b8:0d:5c:07",  "testTag", tagLocations.get("00:12:b8:0d:5c:07"));
      //  Device device6 = new Device("00:12:b8:0d:21:66",  "testTag", tagLocations.get("00:12:b8:0d:21:66"));
      //  Device device7 = new Device("00:12:b8:0d:26:0d",  "testTag", tagLocations.get("00:12:b8:0d:26:0d"));
      //  Device device8 = new Device("00:12:b8:0d:68:6b",  "testTag", tagLocations.get("00:12:b8:0d:68:6b"));
      //  Device device9 = new Device("00:12:b8:0d:59:5e",  "testTag", tagLocations.get("00:12:b8:0d:59:5e"));
      //  Device device10 = new Device("00:12:b8:0d:5b:c8",  "testTag", tagLocations.get("00:12:b8:0d:5b:c8"));

        Devices.put("00:12:b8:0d:6b:58", device1);
       // Devices.put("00:12:b8:0d:68:ad", device2);
      //  Devices.put("00:12:b8:0d:68:90", device3);
      //  Devices.put("00:12:b8:0d:6c:07", device4);
      //  Devices.put("00:12:b8:0d:5c:07", device5);
      //  Devices.put("00:12:b8:0d:21:66", device6);
      //  Devices.put("00:12:b8:0d:26:0d", device7);
     //  Devices.put("00:12:b8:0d:68:6b", device8);
     //   Devices.put("00:12:b8:0d:59:5e", device9);
     //   Devices.put("00:12:b8:0d:5b:c8", device10);


       // GetDeviceInfoTask task = new GetDeviceInfoTask(id, context);
      //  task.execute();
    }

    private void addTagLocation(TagLocation location){
        if (this.tagLocations == null)
            tagLocations = new HashMap<>();

        // Make sure not to add any duplicates
        tagLocations.remove(location.getMacAddress());
        tagLocations.put(location.getMacAddress(), location);
    }

    private void addDevice(Device device){
        if (this.Devices == null)
            Devices = new HashMap<>();

        // Make sure not to add any duplicates
        Devices.remove(device.getMAC());
        Devices.put(device.getMAC(), device);
    }

    /**
     * Displays the location of the user on the floor they are located. Also gets the locations
     * of all the tags associated with the given tag category and displays them if they
     * are located on the same floor as the client.
     *
     * @param context used to start a new activity to display the client and tags
     * @param clientMac The mac address of the user (client)
     * @param tagCategory The category of device/tag to retrieve and display
     */
    public void findTags(Context context, String clientMac, String tagCategory){
        clientMac = clientMac.toLowerCase();

    }

    public ArrayList<String> deviceSearchType(String typeToFind){

        ArrayList<String> results = new ArrayList<String>();

        for (Map.Entry<String, Device> entry : Devices.entrySet()) {
            if (Objects.equals(typeToFind, entry.getValue().getType())) {
                results.add(entry.getKey());
            }
        }

        return results;

    }


    public ArrayList<TagLocation> deviceSearchLocations(ArrayList<String>  macList){

        ArrayList<TagLocation> results = new ArrayList<TagLocation>();

        for (String entry : macList) {
            {
                TagLocation location = Devices.get(entry).getLocation();
                results.add(location);
            }
        }
        return results;
    }



    public HashMap<String, TagLocation> getTagLocations(){
        if (tagLocations == null)
            tagLocations = new HashMap<>();
        return tagLocations;
    }

    //---
    public HashMap<String, Device> getDevices(){
        if (Devices == null)
            Devices = new HashMap<>();
        return Devices;
    }


    /**
     * Retrieves the location of the client with the given ID, and displays it in the Equipment find activity
     * with a map of the floor plan.
     *
     * @param id The mac address of the client
     * @param context used to start a new activity
     */
    public static void displayClientLocation(String id, final Context context){
        @SuppressLint("StaticFieldLeak")
        AsyncTask<String, Void, ClientLocation> task = new AsyncTask<String, Void, ClientLocation>() {
            @Override
            protected ClientLocation doInBackground(String... strings) {
                ClientLocation location = null;
                try {
                    // Try finding the client at the hospital. If it's not there, check research park
                    location = getLocationDAO().getClientLocation(strings[0], context, LocationDAO.EBC);
                    if (location == null) {
                        location = getLocationDAO().getClientLocation(strings[0], context, LocationDAO.PARK);
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                return location;
            }

            @Override
            protected void onPostExecute(ClientLocation location){
                if (location != null) {
                    float x = location.getMapCoordinate().getX();
                    float y = location.getMapCoordinate().getY();
                    String message = "Coordinates: (" + x + ", " + y + ")";
                    System.out.println(message);

                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    getLocationDAO().displayFloorMap(context, location.getMapHierarchy());
                    LocationController.getInstance().setClientLocation(location);
                }
                else {
                    Toast.makeText(context, CLIENT_LOCATION_ERROR, Toast.LENGTH_SHORT).show();
                }
            }
        };
        task.execute(id);

        //openNewActivity(context, EquipmentFindActivity.class);
    }

    private static class GetTagLocationTask extends AsyncTask<Void, Void, TagLocation> {

        String id;
        Context context;

        GetTagLocationTask(String id, Context context){
            this.id = id;
            this.context = context;
        }

        @Override
        protected TagLocation doInBackground(Void... voids) {
            try {
                TagLocation location = Controller.getLocationDAO().getTagLocation(id, context, LocationDAO.EBC);
                if (location != null){
                    LocationController.getInstance().addTagLocation(location);
                    return location;
                }
                else{
                    TagLocation tag = Controller.getLocationDAO().getTagLocation(id, context, LocationDAO.PARK);
                    LocationController.getInstance().addTagLocation(tag);
                    return tag;
                }
            }
            catch (AccessDeniedException | InvalidResponseException ex) {
                ex.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(TagLocation location){
            if (location != null){}

        }
    }

    /*private static class GetDeviceTask extends AsyncTask<Void, Void, Device> {

        String id;
        Context context;

        GetDeviceTask(String id, Context context){
            this.id = id;
            this.context = context;
        }


        @Override
        protected Device doInBackground(Void... voids) {
           try {
                return Controller.getLocationDAO().getDevice(id, context);
            }
            catch (AccessDeniedException | InvalidResponseException ex) {
                ex.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Device device){
            LocationController.getInstance().addDevice(device);
        }
    }*/


    public class Device{

        private String MAC;
        private String type;
        private TagLocation location;

        public Device(String MACAddress, String deviceType, TagLocation deviceLocation){
            MAC = MACAddress;
            type = deviceType;
            location = deviceLocation;
        }

        public TagLocation getLocation(){return location;}
        public String getType(){return type;}
        public String getMAC(){return MAC;}

        public void setMAC(String newMAC){MAC = newMAC;}
        public void setType(String newType){type = newType;}
        public void setLocation(TagLocation newLocation){location = newLocation;}
    }

}
