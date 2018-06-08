package ute.webservice.voiceagent.location;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
        task.execute();
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
                    return location;
                }
                else
                    return Controller.getLocationDAO().getTagLocation(id, context, LocationDAO.PARK);
            }
            catch (AccessDeniedException | InvalidResponseException ex) {
                ex.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(TagLocation location){
            if (location != null)
                LocationController.getInstance().addTagLocation(location);
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
