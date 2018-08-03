package ute.webservice.voiceagent.location;

/**
 * Stores basic information of a Tag, including its location.
 * Created by Nathan Taylor on 8/3/2018
 */
public class TagInfo {

    private static final String campus = "";

    private String macAddress;
    private String category;
    private String building;
    private String floor;
    private float x;
    private float y;

    public String getMacAddress() {
        return macAddress;
    }

    public String getCategory() {
        return category;
    }

    public String getBuilding() {
        return building;
    }

    public String getFloor() {
        return floor;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public String getMapHierarchy() {
        return campus + ">" + building + ">" + floor;
    }

    public TagInfo(String macAddress, String category, String building, String floor, float x, float y){
        this.macAddress = macAddress;
        this.category = category;
        this.building = building;
        this.floor = floor;
        this.x = x;
        this.y = y;
    }

}
