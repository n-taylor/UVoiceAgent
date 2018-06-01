package ute.webservice.voiceagent.location;

/**
 * Created by u0450254 on 6/1/2018.
 */

public class TagLocation {

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

        /**
         * It is recommended that a ClientLocation be set using a ClientLocationBuilder object.
         */
        public TagLocation(String macAddress, boolean currentlyTracked, float confidenceFactor,
                           String lastBeaconTime, int lastBeaconSequenceNumber,
                           String mapHierarchy, long floorRefId, MapDimension mapDimension,
                           String imageName, MapCoordinate mapCoordinate, VendorData vendorData){
            this.macAddress = macAddress;
            this.currentlyTracked = currentlyTracked;
            this.confidenceFactor = confidenceFactor;
            this.lastBeaconTime = lastBeaconTime;
            this.lastBeaconSequenceNumber = lastBeaconSequenceNumber;
            this.mapHierarchy = mapHierarchy;
            this.floorRefId = floorRefId;
            this.mapDimension = mapDimension;
            this.imageName = imageName;
            this.mapCoordinate = mapCoordinate;
            this.vendorData = vendorData;
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

        public float getConfidenceFactor() {
            return confidenceFactor;
        }

        public boolean isCurrentlyTracked() {
            return currentlyTracked;
        }

        public String getMacAddress() {
            return macAddress;
        }
    }
