package ute.webservice.voiceagent.location;

/**
 * Builds a Client Location object
 * Created by Nathan Taylor on 5/3/2018.
 */

public class ClientLocationBuilder {

    private MapCoordinate mapCoordinate;

    private MapInfo mapInfo;

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

    public ClientLocation create(){
        return new ClientLocation(mapCoordinate, mapInfo, imageName, floorRefId, mapHierarchy, apMacAddress, band, confidenceFactor,
                currentlyTracked, dot11Status, ipAddress, macAddress, userName, ssId, isGuestUser);
    }

    public void setMapCoordinate(float x, float y){
        mapCoordinate = new MapCoordinate(x, y);
    }

    public void setMapCoordinate(float x, float y, String unit){
        mapCoordinate = new MapCoordinate(x, y, unit);
    }

    public void setMapInfo(MapInfo mapInfo) {
        this.mapInfo = mapInfo;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public void setFloorRefId(long floorRefId) {
        this.floorRefId = floorRefId;
    }

    public void setMapHierarchy(String mapHierarchy) {
        this.mapHierarchy = mapHierarchy;
    }

    public void setApMacAddress(String apMacAddress) {
        this.apMacAddress = apMacAddress;
    }

    public void setBand(String band) {
        this.band = band;
    }

    public void setConfidenceFactor(float confidenceFactor) {
        this.confidenceFactor = confidenceFactor;
    }

    public void setCurrentlyTracked(boolean currentlyTracked) {
        this.currentlyTracked = currentlyTracked;
    }

    public void setDot11Status(String dot11Status) {
        this.dot11Status = dot11Status;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setSsId(String ssId) {
        this.ssId = ssId;
    }

    public void setGuestUser(boolean guestUser) {
        isGuestUser = guestUser;
    }
}
