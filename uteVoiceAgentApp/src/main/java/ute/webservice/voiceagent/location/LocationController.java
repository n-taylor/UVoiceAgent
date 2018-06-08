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


    private ArrayList<String> deviceSearchType(String typeToFind){

        ArrayList<String> results = new ArrayList<String>();

        for (Map.Entry<String, Device> entry : Devices.entrySet()) {
            if (Objects.equals(typeToFind, entry.getValue().getType())) {
                results.add(entry.getKey());
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


    private class Device{

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
