package ute.webservice.voiceagent.dao;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.entity.HttpEntityWrapperHC4;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import ute.webservice.voiceagent.R;
import ute.webservice.voiceagent.exceptions.AccessDeniedException;
import ute.webservice.voiceagent.exceptions.InvalidResponseException;
import ute.webservice.voiceagent.location.ClientLocation;
import ute.webservice.voiceagent.location.ClientLocationBuilder;
import ute.webservice.voiceagent.location.LocationController;
import ute.webservice.voiceagent.location.MapCoordinate;
import ute.webservice.voiceagent.location.TagLocation;
import ute.webservice.voiceagent.location.MapDimension;
import ute.webservice.voiceagent.location.TagLocationBuilder;
import ute.webservice.voiceagent.location.VendorData;
import ute.webservice.voiceagent.util.AccountCheck;
import ute.webservice.voiceagent.util.CertificateManager;
import ute.webservice.voiceagent.util.Constants;

/**
 * A Data Access Object for CISCO location services.
 * Created by Nathan Taylor on 5/3/2018.
 */

public class CiscoLocationDAO implements LocationDAO {

    private static final String USER_PASSWORD_PREFIX = "https://ITS-Innovation-VoiceApp:K75wz9PBp1AaCqeNfGMKVI5R@";
    private static final String GET_CLIENT_LOCATION_PARK = "mse-park.net.utah.edu/api/contextaware/v1/location/clients/";
    private static final String GET_CLIENT_LOCATION_EBC = "mse-ebc.net.utah.edu/api/contextaware/v1/location/clients/";
    private static final String GET_TAG_LOCATION_PARK = "mse-park.net.utah.edu/api/contextaware/v1/location/tags/";
    private static final String GET_TAG_LOCATION_EBC = "mse-ebc.net.utah.edu/api/contextaware/v1/location/tags/";
    private static final String GET_FLOOR_PLAN = "mse-park.net.utah.edu/api/contextaware/v1/maps/imagesource/";
    private static final String RETURN_TYPE = ".json";

    private static final String GET_CLIENT_LOCATION = "https://10.0.2.2:8042/cisco/client/location/";
    private static final String GET_TAG_LOCATION = "https://10.0.2.2:8042/cisco/tag/location/";

    private static final String UNKNOWN_FLOOR = "The area you are located in is not currently supported";

    private static final int MAX_WIDTH = 1800;
    private static final int MAX_HEIGHT = 1800;

    private static HashMap<String, Integer> floorMaps;

    private static final int bitmap_scale = 2;

    public CiscoLocationDAO(){
        if (floorMaps == null) {
            floorMaps = new HashMap<>();

            floorMaps.put("UofU-FtDouglas>0482-102Tower>Level 4", R.drawable.tower_level_4);
            floorMaps.put("UofU-Hospital>0525-UHOSP>Level 4", R.drawable.uhosp_level_4);
        }
    }

    private CloseableHttpClient getHttpClient(Context context, int campus){
        BasicCookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient httpClient;
        if (campus == PARK) {
            httpClient = HttpClients.custom()
                    .setDefaultCookieStore(cookieStore)
                    .setSslcontext(CertificateManager.getSSlContextByCampus(context, "mse-parknetutahedu.crt", campus))
                    .build();
        }
        else {
            httpClient = HttpClients.custom()
                    .setDefaultCookieStore(cookieStore)
                    .setSslcontext(CertificateManager.getSSlContextByCampus(context, "mse-ebcnetutahedu.crt", campus))
                    .build();
        }
//
//        httpClient = HttpClients.custom()
//                .setDefaultCookieStore(cookieStore)
//                .setSslcontext(CertificateManager.getSSlContext(context, "mse-parknetutahedu.crt"))
//                .build();

        return httpClient;
    }


    /**
     * Gets the client location info for a given client.
     *
     * @param ID A mac address, IP address or username.
     * @param campus Either Park or EBC, depending on the supposed location of the client
     * @return the location information of the client, or null if an error occurs.
     */
    @Override
    public ClientLocation getClientLocation(String ID , Context context, int campus) throws InvalidResponseException, AccessDeniedException {
//        String url = (campus == PARK) ? GET_CLIENT_LOCATION_PARK : GET_CLIENT_LOCATION_EBC;
//        String request = USER_PASSWORD_PREFIX + url + ID.trim() + RETURN_TYPE;

        String request = GET_CLIENT_LOCATION + ID.trim();
        String response = "";

        try {
            HttpGetHC4 getRequest = new HttpGetHC4(request);

//            CloseableHttpResponse httpResponse = getHttpClient(context, campus).execute(getRequest);
            CloseableHttpResponse httpResponse = AccountCheck.httpclient.execute(getRequest);
            HttpEntity entity = httpResponse.getEntity();

            if (entity != null){
                BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
                String line = "";

                while ((line = reader.readLine()) != null){
                    response += line;
                }

                if (response.equalsIgnoreCase(Constants.ACCESS_DENIED)){
                    throw new AccessDeniedException();
                }
                else if (response.isEmpty()){
                    throw new InvalidResponseException("No response was obtained from the server.");
                }
                else {
                    return parseJsonClientLocation(response);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }

    public TagLocation getTagLocation(String ID, Context context, int campus) throws AccessDeniedException, InvalidResponseException{
//        String url = (campus == PARK) ? GET_TAG_LOCATION_PARK : GET_TAG_LOCATION_EBC;
//        String request = USER_PASSWORD_PREFIX + url + ID.trim() + RETURN_TYPE;
        String request = GET_TAG_LOCATION + ID.trim();
        StringBuilder response = new StringBuilder();

        try{
            HttpGetHC4 getRequest = new HttpGetHC4(request);

//            CloseableHttpResponse httpResponse = getHttpClient(context, campus).execute(getRequest);
            CloseableHttpResponse httpResponse = AccountCheck.httpclient.execute(getRequest);
            HttpEntity entity = httpResponse.getEntity();

            if (entity != null){
                BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
                String line = "";

                while ((line = reader.readLine()) != null){
                    response.append(line);
                }

                if (response.toString().equalsIgnoreCase(Constants.ACCESS_DENIED)){
                    throw new AccessDeniedException();
                }
                else if (response.toString().isEmpty()){
                    throw new InvalidResponseException("No response was obtained from the server.");
                }
                else {
                    return parseJsonTagLocation(response.toString());
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }

        return null;
    }

    private TagLocation parseJsonTagLocation(String json){
        try {
            // Convert the string into a JsonObject
            JsonElement jsonElement = new JsonParser().parse(json);
            JsonObject top = jsonElement.getAsJsonObject().getAsJsonObject("TagLocation");

            // Use a Tag Location Builder
            TagLocationBuilder locationBuilder = new TagLocationBuilder()
                    .setMacAddress(top.get("macAddress").getAsString())
                    .setCurrentlyTracked(top.get("currentlyTracked").getAsBoolean())
                    .setConfidenceFactor(top.get("confidenceFactor").getAsFloat())
                    .setLastBeaconTime(top.get("lastBeaconTime").getAsString())
                    .setLastBeaconSequenceNumber(top.get("lastBeaconSequenceNumber").getAsInt());

            // Get Map Info
            JsonObject current = top.getAsJsonObject("MapInfo");
            locationBuilder.setMapHierarchy(current.get("mapHierarchyString").getAsString())
                    .setFloorRefId(current.get("floorRefId").getAsLong());
            JsonObject child = current.getAsJsonObject("Dimension");
            MapDimension dimension = new MapDimension(child.get("height").getAsFloat(),
                    child.get("length").getAsFloat(), child.get("width").getAsFloat(),
                    child.get("offsetX").getAsFloat(), child.get("offsetY").getAsFloat(),
                    child.get("unit").getAsString());
            locationBuilder.setMapDimension(dimension);
            child = current.getAsJsonObject("Image");
            locationBuilder.setImageName(child.get("imageName").getAsString());

            // Get Map Coordinate
            current = top.getAsJsonObject("MapCoordinate");
            MapCoordinate coordinate = new MapCoordinate(current.get("x").getAsFloat(),
                    current.get("y").getAsFloat(), current.get("unit").getAsString());
            locationBuilder.setMapCoordinate(coordinate);

            // Get the VendorData
            current = top.getAsJsonObject("VendorData");
            VendorData vendorData = new VendorData(current.get("vendorId").getAsLong(),
                    current.get("elementId").getAsInt(), current.get("data").getAsString(),
                    current.get("lastReceivedTime").getAsString(), current.get("lastReceivedSeqNum").getAsInt());
            locationBuilder.setVendorData(vendorData);

            return locationBuilder.create();

        }
        catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    private ClientLocation parseJsonClientLocation(String json){
        try {
            // Convert the string into a JsonObject
            JsonElement jsonElement = new JsonParser().parse(json);
            JsonObject top = jsonElement.getAsJsonObject().getAsJsonObject("WirelessClientLocation");

            // Use a Client Location Builder
            ClientLocationBuilder locationBuilder = new ClientLocationBuilder();
            locationBuilder = locationBuilder.setMacAddress(top.get("macAddress").getAsString())
                    .setCurrentlyTracked(top.get("currentlyTracked").getAsBoolean())
                    .setConfidenceFactor(top.get("confidenceFactor").getAsFloat())
                    //.setIpAddress(top.get("ipAddress").getAsString())
                    //.setUserName(top.get("userName").getAsString())
                    //.setSsId(top.get("ssId").getAsString())
                    .setBand(top.get("band").getAsString())
                    //.setApMacAddress(top.get("apMacAddress").getAsString())
                    .setGuestUser(top.get("isGuestUser").getAsBoolean())
                    .setDot11Status(top.get("dot11Status").getAsString());

            // Add Map Info
            JsonObject current = top.getAsJsonObject("MapInfo");
            locationBuilder = locationBuilder
                    .setMapHierarchy(current.get("mapHierarchyString").getAsString())
                    .setFloorRefId(current.get("floorRefId").getAsLong());
            JsonObject child = current.getAsJsonObject("Dimension");
            MapDimension mapDimension = new MapDimension(child.get("height").getAsFloat(), child.get("length").getAsFloat(),
                    child.get("width").getAsFloat(), child.get("offsetX").getAsFloat(), child.get("offsetY").getAsFloat(),
                    child.get("unit").getAsString());
            locationBuilder.setMapDimension(mapDimension);

            child = current.getAsJsonObject("Image");
            locationBuilder.setImageName(child.get("imageName").getAsString());

            // Add Map Coordinate data
            current = top.getAsJsonObject("MapCoordinate");
            locationBuilder.setMapCoordinate(current.get("x").getAsFloat(),
                    current.get("y").getAsFloat(), current.get("unit").getAsString());

            // Add Statistics data
            // TODO: Add statistics data


            return locationBuilder.create();
        } catch (Exception ex){
            return null;
        }
    }

    public void displayFloorMap(Context context, String imageName){
//        String url = USER_PASSWORD_PREFIX + GET_FLOOR_PLAN + imageName;
//        GetImageTask task = new GetImageTask(context, url, httpClient);
//        task.execute();

//        if (floorMaps.containsKey(imageName)) {
//            Bitmap map = decodeScaledResource(context.getResources(), floorMaps.get(imageName), MAX_WIDTH, MAX_HEIGHT);
//            LocationController.startActivity(context, map);
//        }
//        else {
//            Toast.makeText(context, UNKNOWN_FLOOR, Toast.LENGTH_LONG).show();
//        }

        // For testing purposes, just load the U Hospital Map
//        try {
//            Bitmap map = decodeScaledResource(context.getResources(), R.drawable.uhosp_level_4, MAX_WIDTH, MAX_HEIGHT);
//            LocationController.startActivity(context, map);
//        }
//        catch(Exception e){
//            e.printStackTrace();
//            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
//        }

        try {
            LocationController.startActivity(context, imageName);
        }
        catch(Exception e){
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
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

    /*
    private static class GetImageTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ProgressDialog progressDialog;
        private CloseableHttpClient httpClient;
        private Context context;

        GetImageTask(Context context, String url, CloseableHttpClient httpClient){
            this.context = context;
            this.url = url;
            this.httpClient = httpClient;

            // Create a progressdialog
            progressDialog = new ProgressDialog(context);
            // Set progressdialog title
            progressDialog.setTitle("Downloading Floor Plan");
            // Set progressdialog message
            progressDialog.setMessage("Loading...");
            progressDialog.setIndeterminate(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Show progress dialog
            progressDialog.show();
        }


        @Override
        protected Bitmap doInBackground(Void... voids) {

            Bitmap bitmap = null;

            try {
                HttpGetHC4 request = new HttpGetHC4(url);
                CloseableHttpResponse response = httpClient.execute(request);
                HttpEntityWrapperHC4 entity = new HttpEntityWrapperHC4(response.getEntity());
                bitmap = BitmapFactory.decodeStream(entity.getContent());
                bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/bitmap_scale, bitmap.getHeight()/bitmap_scale, true);
                return bitmap;
            } catch (IOException ex){
                ex.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // Close progressdialog
            progressDialog.dismiss();

            LocationController.startActivity(context, result);
        }
    }
    */
}

