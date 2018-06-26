package ute.webservice.voiceagent.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import ute.webservice.voiceagent.R;
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

    private static final int TAG_ID = R.drawable.tag_2;

    private Bitmap bitmap;
    private Bitmap tagBitmap;

    private static final int MAX_IMAGE_WIDTH = 1200;
    private static final int MAX_IMAGE_HEIGHT = 1200;

    private HashMap<String, Bitmap> floorMaps;

    private String currentMapName = "";
    private String currentCategory;

    private ClientLocation clientLocation;
    private HashMap<String, TagLocation> tagLocations;
    private HashMap<String, Device> Devices;

    /**
     * Maps Category Name to a list of MAC addresses of tags belonging to that category.
     * Each category name much match the name in the EquipmentCategories entity in Dialog Flow.
     */
    private static HashMap<String, ArrayList<String>> categories;

    private static LocationController instance;

    private LocationController(){
        if (categories == null){
            categories = new HashMap<>();

            // Create the categories
            categories.put("Misc", new ArrayList<String>());
            categories.put("IV Pump", new ArrayList<String>());
            categories.put("X Ray", new ArrayList<String>());
            categories.put("MRI", new ArrayList<String>());
            categories.put("Dialysis", new ArrayList<String>());
            categories.put("CT Scanner", new ArrayList<String>());

            // Populate the categories
            categories.get("Misc").add("00:12:B8:0D:6B:58");
            categories.get("IV Pump").add("00:12:B8:0D:68:AD");
            categories.get("X Ray").add("00:12:B8:0D:68:90");
            categories.get("X Ray").add("00:12:B8:0D:6C:07");
            categories.get("MRI").add("00:12:B8:0D:5C:07");
            categories.get("MRI").add("00:12:B8:0D:21:66");
            categories.get("Dialysis").add("00:12:B8:0D:26:0D");
            categories.get("Dialysis").add("00:12:B8:0D:68:6B");
            categories.get("Dialysis").add("00:12:B8:0D:59:5E");
            categories.get("CT Scanner").add("00:12:B8:0D:5D:C8");
        }

        if (floorMaps == null){
            floorMaps = new HashMap<>();
        }
    }

    /**
     * Checks if the floor map images have been loaded. If they have not, try calling loadBitmaps(Context context);
     * @return true if all maps have been loaded.
     */
    public boolean mapsAreLoaded(){
        return floorMaps != null && !floorMaps.isEmpty();
    }

    /**
     * Retrieves the available floor maps from resources and adds them to the floorMaps hash map.
     */
    public void loadBitmaps(Context context){
        try {
            floorMaps = new HashMap<>();

            floorMaps.put("UofU-FtDouglas>0482-102Tower>Level 4",
                    decodeScaledResource(context.getResources(), R.drawable.tower_level_4, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT));
            floorMaps.put("UofU-Hospital>0525-UHOSP>Level 4",
                    decodeScaledResource(context.getResources(), R.drawable.uhosp_level_4, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT));
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * Gets the image of the floor map of a given floor. If the floor requested does not exist,
     * is not included or is misspelled (case-sensitive) or the images have not been loaded yet,
     * this method will return null.
     *
     * @param mapHierarchyString The map hierarchy string associated with the floor.
     * @return The bitmap of the given floor, or null if there is an issue.
     */
    public Bitmap getFloorMap(String mapHierarchyString){
        if (floorMaps == null)
            return null;

        return floorMaps.get(mapHierarchyString);
    }

    private Bitmap decodeScaledResource(Resources res, int resId, int reqWidth, int reqHeight){
        // First decode just checking dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate the inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with the inSampleSize set now
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    private int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


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

    /**
     * Starts the EquipmentFind activity and sets the current image to be displayed as the imageName
     * specified.
     */
    public static void startActivity(Context context, String imageName){
        LocationController.getInstance().currentMapName = imageName;
        Intent intent = new Intent(context, EquipmentFindActivity.class);
        context.startActivity(intent);
    }

    /**
     * @return the image of the currently specified floor plan.
     */
    public Bitmap getImage(){
        // For testing purposes, right not just show the burn unit
        return floorMaps.get("UofU-Hospital>0525-UHOSP>Level 4");

        //return floorMaps.get(currentMapName);
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

    public Bitmap getTagImage(Context context){
        if (tagBitmap == null){
            tagBitmap = BitmapFactory.decodeResource(context.getResources(), TAG_ID);
        }
        return tagBitmap;
    }

    public void recycleImages(){
        if (tagBitmap != null) {
            tagBitmap.recycle();
            tagBitmap = null;
        }

        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    /**
     * Updates the location of the tag associated with the given ID.
     * @param id
     * @param context
     * @param category May be null. Otherwise, the category of the device the tag is attached to
     */
    public void findTagLocation(String id, Context context, String category){
        GetTagLocationTask task = new GetTagLocationTask(id, context, category);
        task.execute();
    }

    public void findDeviceInfo(String id, Context context){

        Devices = new HashMap<>();

        findTagLocation("00:12:b8:0d:6b:58", context,  "testTag");
      //   findTagLocation("00:12:b8:0d:68:ad", context);
      //  findTagLocation("00:12:b8:0d:68:90", context);
      //  findTagLocation("00:12:b8:0d:6c:07", context);
      //  findTagLocation("00:12:b8:0d:5c:07", context);
      //  findTagLocation("00:12:b8:0d:21:66", context);
      //  findTagLocation("00:12:b8:0d:26:0d", context);
      //  findTagLocation("00:12:b8:0d:68:6b", context);
      //  findTagLocation("00:12:b8:0d:59:5e", context);
      //  findTagLocation("00:12:b8:0d:5b:c8", context);

        Device device1 = new Device("00:12:b8:0d:6b:58",  "testTag", tagLocations.get("00:12:b8:0d:6b:58"));
      //  Device device2 = new Device("00:12:b8:0d:68:ad",  "testTag", tagLocations.get("00:12:b8:0d:68:ad"));
      //   Device device3 = new Device("00:12:b8:0d:68:90",  "testTag", tagLocations.get("00:12:b8:0d:68:90"));
      //  Device device4 = new Device("00:12:b8:0d:6c:07",  "testTag", tagLocations.get("00:12:b8:0d:6c:07"));
      //  Device device5 = new Device("00:12:b8:0d:5c:07",  "testTag", tagLocations.get("00:12:b8:0d:5c:07"));
      //  Device device6 = new Device("00:12:b8:0d:21:66",  "testTag", tagLocations.get("00:12:b8:0d:21:66"));
      //  Device device7 = new Device("00:12:b8:0d:26:0d",  "testTag", tagLocations.get("00:12:b8:0d:26:0d"));
      //  Device device8 = new Device("00:12:b8:0d:68:6b",  "testTag", tagLocations.get("00:12:b8:0d:68:6b"));
      //  Device device9 = new Device("00:12:b8:0d:59:5e",  "testTag", tagLocations.get("00:12:b8:0d:59:5e"));
      //  Device device10 = new Device("00:12:b8:0d:5b:c8",  "testTag", tagLocations.get("00:12:b8:0d:5b:c8"));

        Devices.put("00:12:b8:0d:6b:58", device1);
      //  Devices.put("00:12:b8:0d:68:ad", device2);
      //  Devices.put("00:12:b8:0d:68:90", device3);
      //  Devices.put("00:12:b8:0d:6c:07", device4);
      // Devices.put("00:12:b8:0d:5c:07", device5);
      //  Devices.put("00:12:b8:0d:21:66", device6);
      //  Devices.put("00:12:b8:0d:26:0d", device7);
      // Devices.put("00:12:b8:0d:68:6b", device8);
      //  Devices.put("00:12:b8:0d:59:5e", device9);
      //  Devices.put("00:12:b8:0d:5b:c8", device10);


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
     * Assumes that the category exists. If it does not, no tags will be displayed.
     *
     * @param context used to start a new activity to display the client and tags
     * @param clientMac The mac address of the user (client)
     * @param tagCategory The category of device/tag to retrieve and display
     */
    public void findTags(Context context, String clientMac, String tagCategory){
        clientMac = clientMac.toLowerCase();
        tagLocations = new HashMap<>(); // clear the tags to display

        // Find the tags
        if (categoryExists(tagCategory)){
            currentCategory = tagCategory;
            for (String tagId : categories.get(tagCategory)){
                findTagLocation(tagId.toLowerCase(), context, tagCategory);
            }
        }

        // Display the client location
        displayClientLocation(clientMac, context);
    }

    /**
     * Updates the locations of all the tags currently in the tagLocations hashmap
     */
    public void refreshTagLocations(Context context){
        for (String tagId : tagLocations.keySet()){
            findTagLocation(tagId.toLowerCase(), context, currentCategory);
        }
    }

    /**
     * Checks if the given category is a valid tag category.
     *
     * @param category The general category of tag (e.g 'IV Pump' or 'X Ray')
     * @return True if the category exists.
     */
    public boolean categoryExists(String category){
        return categories != null && categories.containsKey(category);
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
        String category;

        GetTagLocationTask(String id, Context context, String category){
            this.id = id;
            this.context = context;
            this.category = category;
        }

        @Override
        protected TagLocation doInBackground(Void... voids) {
            try {
                TagLocation location = Controller.getLocationDAO().getTagLocation(id, context, LocationDAO.EBC);
                if (location == null){
                    location = Controller.getLocationDAO().getTagLocation(id, context, LocationDAO.PARK);
                }

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
