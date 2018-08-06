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
import java.util.List;

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

    private static final int MAX_TAG_WIDTH = 30;
    private static final int MAX_TAG_HEIGHT = 20;

    private HashMap<String, Bitmap> floorMaps;

    private String currentMapName = "";
    private String currentCategory;

    private ClientLocation clientLocation;
    private HashMap<String, TagLocation> tagLocations;
    private List<TagInfo> tagInfo;
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

        currentMapName="UofU-Hospital>0525-UHOSP>Level 4";

        return floorMaps.get(currentMapName);

        //return floorMaps.get(currentMapName);
    }

    public String getImageName() { return clientLocation.getImageName(); }

    public void setClientLocation(ClientLocation location){
        clientLocation = location;
    }

    public MapCoordinate getUserLocation(){
        if (clientLocation == null)
            return null;
        else
            return clientLocation.getMapCoordinate();
    }

    public MapDimension getDimensions(){
        return clientLocation.getMapDimension();
    }

    public String getCurrentMapName() {return  currentMapName;}

    public Bitmap getTagImage(Context context){
        if (tagBitmap == null){
            tagBitmap = decodeScaledResource(context.getResources(), TAG_ID, MAX_TAG_WIDTH, MAX_TAG_HEIGHT);
//            tagBitmap = BitmapFactory.decodeResource(context.getResources(), TAG_ID);
        }
        return tagBitmap;
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
    public void findTags(final Context context, final String clientMac, final String tagCategory){
        // Set the current tag category
        this.currentCategory = tagCategory;

        // Get the client location
        GetClientLocationTask task = new GetClientLocationTask(clientMac);
        task.setListener(new ClientLocationListener() {
            @Override
            public void onLocationReceived(ClientLocation location) {
                if (location == null) {
                    Toast.makeText(context, CLIENT_LOCATION_ERROR, Toast.LENGTH_SHORT).show();
                }
                else {
                    // Set the client location
                    setClientLocation(location);

                    // Get the tags' locations
                    retrieveTagInfo(location.getBuilding(), location.getFloor(), currentCategory); // Use this for actual location/floor
                    // TODO: TEST ON TAGS IN THE BURN UNIT

                    // Display the map with client and tag locations
                    displayClientLocation(clientMac, context);
                }
            }
        });
        task.execute();

        /*clientMac = clientMac.toLowerCase();
        tagLocations = new HashMap<>(); // clear the tags to display

        // Find the tags
        if (categoryExists(tagCategory)){
            currentCategory = tagCategory;
            for (String tagId : categories.get(tagCategory)){
                findTagLocation(tagId.toLowerCase(), context, tagCategory);
            }
        }

        // Display the client location
        displayClientLocation(clientMac, context);*/
    }

    /**
     * Gets the info of the tags that are of the category specified and stores them
     * @param building The building in which the client is located
     * @param floor The floor on which the client is located
     * @param category The tag category the user is requesting
     */
    private void retrieveTagInfo(String building, String floor, String category){
        GetTagCategoryLocationsTask task = new GetTagCategoryLocationsTask(building, floor, category);
        task.setListener(new TagInfoListener() {
            @Override
            public void onInfoReceived(List<TagInfo> tags) {
                tagInfo = tags;
            }
        });
        task.execute();
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

    public HashMap<String, TagLocation> getTagLocations(){
        if (tagLocations == null)
            tagLocations = new HashMap<>();
        return tagLocations;
    }

    /**
     * Gets the current list of tag info. May be empty.
     */
    public List<TagInfo> getTagInfo(){
        if (tagInfo == null){
            tagInfo = new ArrayList<>();
        }
        return tagInfo;
    }


    /**
     * Retrieves the client location if necessary and displays it in the Equipment find activity
     * with a map of the floor plan.
     *
     * @param id The mac address of the client
     * @param context used to start a new activity
     */
    public void displayClientLocation(String id, final Context context){

        if (clientLocation == null){
            GetClientLocationTask task = new GetClientLocationTask(id);
            task.setListener(new ClientLocationListener() {
                @Override
                public void onLocationReceived(ClientLocation location) {
                    if (location == null){
                        Toast.makeText(context, CLIENT_LOCATION_ERROR, Toast.LENGTH_SHORT).show();
                    }
                    else {
                        setClientLocation(location);
                        getLocationDAO().displayFloorMap(context, location.getMapHierarchy());
                    }
                }
            });
            task.execute();
        }
        else {
            getLocationDAO().displayFloorMap(context, clientLocation.getMapHierarchy());
        }

        /*@SuppressLint("StaticFieldLeak")
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
        task.execute(id);*/

        //openNewActivity(context, EquipmentFindActivity.class);
    }

    /**
     * Gets the
     */
    private static class GetTagCategoryLocationsTask extends AsyncTask<Void, Void, List<TagInfo>>{

        private String building;
        private String floor;
        private String category;

        private TagInfoListener listener;

        public GetTagCategoryLocationsTask(String building, String floor, String category){
            this.building = building;
            this.floor = floor;
            this.category = category;
        }

        public void setListener(TagInfoListener listener){
            this.listener = listener;
        }

        @Override
        protected List<TagInfo> doInBackground(Void... voids) {
            return getLocationDAO().getTagLocations(building, floor, category);
        }

        @Override
        public void onPostExecute(List<TagInfo> tags){
            listener.onInfoReceived(tags);
        }
    }

    /**
     * To be used in conjunction with GetTagCategoryLocationsTask
     */
    private interface TagInfoListener {
        void onInfoReceived(List<TagInfo> tags);
    }

    private static class GetClientLocationTask extends AsyncTask<Void, Void, ClientLocation> {

        private String macAddress;
        private ClientLocationListener listener;

        GetClientLocationTask(String macAddress){
            this.macAddress = macAddress;
        }

        void setListener(ClientLocationListener listener){
            this.listener = listener;
        }

        @Override
        protected ClientLocation doInBackground(Void... voids) {
            ClientLocation location = null;
            try {
                // Get the location of the client
                location = getLocationDAO().getClientLocation(macAddress);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return location;
        }

        @Override
        protected void onPostExecute(ClientLocation location){
            listener.onLocationReceived(location);
        }
    }

    private interface ClientLocationListener {
        /**
         * Called when the location of a client has been processed in the GetClientLocationTask class.
         * @param location The location of the client requested, or null if there was a problem.
         */
        void onLocationReceived(ClientLocation location);
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
                    location.setCategory(category);
                }
                return location;

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