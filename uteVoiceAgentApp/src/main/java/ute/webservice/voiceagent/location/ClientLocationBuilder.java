package ute.webservice.voiceagent.location;

/**
 * Builds a Client Location object
 * Created by Nathan Taylor on 5/3/2018.
 */

public class ClientLocationBuilder {

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

    public ClientLocation create(){
        return new ClientLocation(mapCoordinate, mapDimension, imageName, floorRefId, mapHierarchy, apMacAddress, band, confidenceFactor,
                currentlyTracked, dot11Status, ipAddress, macAddress, userName, ssId, isGuestUser);
    }

    public ClientLocationBuilder setMapCoordinate(float x, float y){
        mapCoordinate = new MapCoordinate(x, y);
        return this;
    }

    public ClientLocationBuilder setMapCoordinate(float x, float y, String unit){
        mapCoordinate = new MapCoordinate(x, y, unit);
        return this;
    }

    public ClientLocationBuilder setMapDimension(MapDimension mapDimension) {
        this.mapDimension = mapDimension;
        return this;
    }

    public ClientLocationBuilder setImageName(String imageName) {
        this.imageName = imageName;
        return this;
    }

    public ClientLocationBuilder setFloorRefId(long floorRefId) {
        this.floorRefId = floorRefId;
        return this;
    }

    public ClientLocationBuilder setMapHierarchy(String mapHierarchy) {
        this.mapHierarchy = mapHierarchy;
        return this;
    }

    public ClientLocationBuilder setApMacAddress(String apMacAddress) {
        this.apMacAddress = apMacAddress;
        return this;
    }

    public ClientLocationBuilder setBand(String band) {
        this.band = band;
        return this;
    }

    public ClientLocationBuilder setConfidenceFactor(float confidenceFactor) {
        this.confidenceFactor = confidenceFactor;
        return this;
    }

    public ClientLocationBuilder setCurrentlyTracked(boolean currentlyTracked) {
        this.currentlyTracked = currentlyTracked;
        return this;
    }

    public ClientLocationBuilder setDot11Status(String dot11Status) {
        this.dot11Status = dot11Status;
        return this;
    }

    public ClientLocationBuilder setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    public ClientLocationBuilder setMacAddress(String macAddress) {
        this.macAddress = macAddress;
        return this;
    }

    public ClientLocationBuilder setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public ClientLocationBuilder setSsId(String ssId) {
        this.ssId = ssId;
        return this;
    }

    public ClientLocationBuilder setGuestUser(boolean guestUser) {
        isGuestUser = guestUser;
        return this;
    }
}
