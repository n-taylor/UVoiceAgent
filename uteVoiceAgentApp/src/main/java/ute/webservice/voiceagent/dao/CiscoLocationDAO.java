package ute.webservice.voiceagent.dao;

import android.content.Context;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import ute.webservice.voiceagent.location.ClientLocation;
import ute.webservice.voiceagent.location.ClientLocationBuilder;
import ute.webservice.voiceagent.location.MapCoordinate;
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
    private static final String RETURN_TYPE = ".json";

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
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setDefaultCookieStore(cookieStore)
                   .setSslcontext(CertificateManager.getSSlContext(context, "mse-parknetutahedu.crt"))
                    .build();
            CloseableHttpResponse httpResponse = httpclient.execute(getRequest);
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