package ute.webservice.voiceagent.location;

/**
 * Builds a Tag Location object
 * Created by Nathan Taylor on 6/1/2018.
 */

public class TagLocationBuilder {


    private MapCoordinate mapCoordinate;

    private MapDimension mapDimension;

    private String imageName;

    private long floorRefId;
    private String mapHierarchy;

    private String lastBeaconTime;
    private int lastBeaconSequenceNumber;

    private VendorData vendorData;

    private float confidenceFactor;
    private boolean currentlyTracked;
    private String macAddress;

    public TagLocation create(){
        return new TagLocation(macAddress, currentlyTracked, confidenceFactor, lastBeaconTime,
                lastBeaconSequenceNumber, mapHierarchy, floorRefId, mapDimension, imageName,
                mapCoordinate, vendorData);
    }

    public TagLocationBuilder setMapCoordinate(MapCoordinate mapCoordinate) {
        this.mapCoordinate = mapCoordinate;
        return this;
    }

    public TagLocationBuilder setMapDimension(MapDimension mapDimension) {
        this.mapDimension = mapDimension;
        return this;
    }

    public TagLocationBuilder setImageName(String imageName) {
        this.imageName = imageName;
        return this;
    }

    public TagLocationBuilder setFloorRefId(long floorRefId) {
        this.floorRefId = floorRefId;
        return this;
    }

    public TagLocationBuilder setMapHierarchy(String mapHierarchy) {
        this.mapHierarchy = mapHierarchy;
        return this;
    }

    public TagLocationBuilder setLastBeaconTime(String lastBeaconTime) {
        this.lastBeaconTime = lastBeaconTime;
        return this;
    }

    public TagLocationBuilder setLastBeaconSequenceNumber(int lastBeaconSequenceNumber) {
        this.lastBeaconSequenceNumber = lastBeaconSequenceNumber;
        return this;
    }

    public TagLocationBuilder setVendorData(VendorData vendorData) {
        this.vendorData = vendorData;
        return this;
    }

    public TagLocationBuilder setConfidenceFactor(float confidenceFactor) {
        this.confidenceFactor = confidenceFactor;
        return this;
    }

    public TagLocationBuilder setCurrentlyTracked(boolean currentlyTracked) {
        this.currentlyTracked = currentlyTracked;
        return this;
    }

    public TagLocationBuilder setMacAddress(String macAddress) {
        this.macAddress = macAddress;
        return this;
    }
}
