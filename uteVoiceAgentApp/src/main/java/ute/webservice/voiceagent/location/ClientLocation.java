package ute.webservice.voiceagent.location;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stores data about the location of a client.
 * Created by Nathan Taylor on 5/3/2018.
 */

public class ClientLocation {

    private static final String REGEX_BUILDING_FLOOR = "[\\w-]+>([\\w-]+)>([\\w- ]+)";

    private MapCoordinate mapCoordinate;

    private MapDimension mapDimension;

    private String imageName;

    private long floorRefId;
    private String mapHierarchy;

    private String apMacAddress;
    private String band;
    private float confidenceFactor;
    private boolean currentlyTracked;
    private String dot11Status;
    private String ipAddress;
    private String macAddress;
    private String userName;
    private String ssId;
    private boolean isGuestUser;

    private Pattern pattern;
    private Matcher matcher;

    /**
     * It is recommended that a ClientLocation be set using a ClientLocationBuilder object.
     * @param coordinate
     * @param info
     * @param imageName
     * @param floorRefId
     * @param mapHierarchy
     * @param apMacAddress
     * @param band
     * @param confidenceFactor
     * @param currentlyTracked
     * @param dot11Status
     * @param ipAddress
     * @param macAddress
     * @param userName
     * @param ssId
     * @param isGuestUser
     */
    public ClientLocation(MapCoordinate coordinate, MapDimension info, String imageName, long floorRefId, String mapHierarchy,
                          String apMacAddress, String band, float confidenceFactor, boolean currentlyTracked, String dot11Status,
                          String ipAddress, String macAddress, String userName, String ssId, boolean isGuestUser){
        this.mapCoordinate = coordinate;
        this.mapDimension = info;
        this.imageName = imageName;
        this.floorRefId = floorRefId;
        this.mapHierarchy = mapHierarchy;
        this.apMacAddress = apMacAddress;
        this.band = band;
        this.confidenceFactor = confidenceFactor;
        this.currentlyTracked = currentlyTracked;
        this.dot11Status = dot11Status;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.userName = userName;
        this.ssId = ssId;
        this.isGuestUser = isGuestUser;

        // Compute the regex
        pattern = Pattern.compile(REGEX_BUILDING_FLOOR);

        // For testing a specific floor, uncomment the line below
//        this.mapHierarchy = "UofU-Hospital>0482-102Tower>Level 5";
    }

    public MapCoordinate getMapCoordinate() {
        return mapCoordinate;
    }

    public MapDimension getMapDimension() {
        return mapDimension;
    }

    public String getImageName() {
        return imageName;
    }

    public long getFloorRefId() {
        return floorRefId;
    }

    public String getMapHierarchy() {
        return mapHierarchy;
    }

    public String getApMacAddress() {
        return apMacAddress;
    }

    public String getBand() {
        return band;
    }

    public float getConfidenceFactor() {
        return confidenceFactor;
    }

    public boolean isCurrentlyTracked() {
        return currentlyTracked;
    }

    public String getDot11Status() {
        return dot11Status;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getUserName() {
        return userName;
    }

    public String getSsId() {
        return ssId;
    }

    public boolean isGuestUser() {
        return isGuestUser;
    }

    /**
     * @return The building name, or null if the map hierarchy string is in the wrong format
     */
    public String getBuilding() {
        matcher = pattern.matcher(mapHierarchy);
        if (matcher.find()){
            return matcher.group(1);
        }
        else {
            return null;
        }
    }

    /**
     * @return The floor name, or null if the map hierarchy string is in the wrong format
     */
    public String getFloor(){
        matcher = pattern.matcher(mapHierarchy);
        if (matcher.find()){
            return matcher.group(2);
        }
        else {
            return null;
        }
    }
}
