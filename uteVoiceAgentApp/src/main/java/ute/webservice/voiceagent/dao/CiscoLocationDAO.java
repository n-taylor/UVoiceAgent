package ute.webservice.voiceagent.dao;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;

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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import ute.webservice.voiceagent.location.ClientLocation;
import ute.webservice.voiceagent.location.ClientLocationBuilder;
import ute.webservice.voiceagent.location.MapDimension;
import ute.webservice.voiceagent.util.CertificateManager;
import ute.webservice.voiceagent.util.Constants;

/**
 * A Data Access Object for CISCO location services.
 * Created by Nathan Taylor on 5/3/2018.
 */

public class CiscoLocationDAO implements LocationDAO {

    private static final String USER_PASSWORD_PREFIX = "https://ITS-Innovation-VoiceApp:K75wz9PBp1AaCqeNfGMKVI5R@";
    private static final String GET_CLIENT_LOCATION = "mse-park.net.utah.edu/api/contextaware/v1/location/clients/";
    private static final String GET_FLOOR_PLAN = "mse-park.net.utah.edu/api/contextaware/v1/maps/imagesource/";
    private static final String RETURN_TYPE = ".json";

    private CloseableHttpClient httpClient;

    public CiscoLocationDAO(){

    }


    /**
     * Gets the client location info for a given client.
     *
     * @param ID A mac address, IP address or username.
     * @return the location information of the client
     */
    @Override
    public ClientLocation getClientLocation(String ID , Context context) throws InvalidResponseException, AccessDeniedException {
        String request = USER_PASSWORD_PREFIX + GET_CLIENT_LOCATION + ID.trim() + RETURN_TYPE;
        String response = "";

        try {
            HttpGetHC4 getRequest = new HttpGetHC4(request);
            BasicCookieStore cookieStore = new BasicCookieStore();
            httpClient = HttpClients.custom()
                    .setDefaultCookieStore(cookieStore)
                   .setSslcontext(CertificateManager.getSSlContext(context, "mse-parknetutahedu.crt"))
                    .build();
            CloseableHttpResponse httpResponse = httpClient.execute(getRequest);
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
        catch (AccessDeniedException e){ throw e;}
        catch (Exception e) {
            e.printStackTrace();
            throw new InvalidResponseException();
        }

        return null;
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
                    .setIpAddress(top.get("ipAddress").getAsString())
                    .setUserName(top.get("userName").getAsString())
                    .setSsId(top.get("ssId").getAsString())
                    .setBand(top.get("band").getAsString())
                    .setApMacAddress(top.get("apMacAddress").getAsString())
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

    public void getFloorPlanImage(Context context, String imageName){
        String url = USER_PASSWORD_PREFIX + GET_FLOOR_PLAN + imageName;
        GetImageTask task = new GetImageTask(context, url, httpClient);
        task.execute();
    }

    private static class GetImageTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ProgressDialog progressDialog;
        private CloseableHttpClient httpClient;

        GetImageTask(Context context, String url, CloseableHttpClient httpClient){
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
                return bitmap;
            } catch (IOException ex){
                ex.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // Close progressdialog
            progressDialog.dismiss();
        }
    }
}

/**
 * Throw this when the response from a webservice is not what was expected.
 */
class InvalidResponseException extends Exception {
    public InvalidResponseException(String message){
        super(message);
    }
    public InvalidResponseException(){ super(); }
}

class AccessDeniedException extends Exception{}