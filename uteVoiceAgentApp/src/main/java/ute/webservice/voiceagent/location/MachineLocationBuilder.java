package ute.webservice.voiceagent.location;

/**
 * Builds a Machine Location object
 * Created by Nathan Taylor on 5/3/2018.
 */

public class MachineLocationBuilder {

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

    public MachineLocation create(){
        return new MachineLocation(mapCoordinate, mapDimension, imageName, floorRefId, mapHierarchy, apMacAddress, band, confidenceFactor,
                currentlyTracked, dot11Status, ipAddress, macAddress, userName, ssId, isGuestUser);
    }

    public MachineLocationBuilder setMapCoordinate(float x, float y){
        mapCoordinate = new MapCoordinate(x, y);
        return this;
    }

    public MachineLocationBuilder setMapCoordinate(float x, float y, String unit){
        mapCoordinate = new MapCoordinate(x, y, unit);
        return this;
    }

    public MachineLocationBuilder setMapDimension(MapDimension mapDimension) {
        this.mapDimension = mapDimension;
        return this;
    }

    public MachineLocationBuilder setImageName(String imageName) {
        this.imageName = imageName;
        return this;
    }

    public MachineLocationBuilder setFloorRefId(long floorRefId) {
        this.floorRefId = floorRefId;
        return this;
    }

    public MachineLocationBuilder setMapHierarchy(String mapHierarchy) {
        this.mapHierarchy = mapHierarchy;
        return this;
    }

    public MachineLocationBuilder setApMacAddress(String apMacAddress) {
        this.apMacAddress = apMacAddress;
        return this;
    }

    public MachineLocationBuilder setBand(String band) {
        this.band = band;
        return this;
    }

    public MachineLocationBuilder setConfidenceFactor(float confidenceFactor) {
        this.confidenceFactor = confidenceFactor;
        return this;
    }

    public MachineLocationBuilder setCurrentlyTracked(boolean currentlyTracked) {
        this.currentlyTracked = currentlyTracked;
        return this;
    }

    public MachineLocationBuilder setDot11Status(String dot11Status) {
        this.dot11Status = dot11Status;
        return this;
    }

    public MachineLocationBuilder setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    public MachineLocationBuilder setMacAddress(String macAddress) {
        this.macAddress = macAddress;
        return this;
    }

    public MachineLocationBuilder setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public MachineLocationBuilder setSsId(String ssId) {
        this.ssId = ssId;
        return this;
    }

    public MachineLocationBuilder setGuestUser(boolean guestUser) {
        isGuestUser = guestUser;
        return this;
    }
}
