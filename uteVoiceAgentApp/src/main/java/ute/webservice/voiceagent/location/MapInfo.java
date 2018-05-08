package ute.webservice.voiceagent.location;

/**
 * Stores map dimensions and info
 * Created by Nathan Taylor on 5/3/2018.
 */

public class MapInfo {
    private float height;
    private float length;
    private float width;
    private float offsetX;
    private float offsetY;
    private String unit;

    public MapInfo (float height, float length, float width, float offsetX, float offsetY, String unit){
        this.height = height;
        this.length = length;
        this.width = width;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.unit = unit;
    }

    public float getHeight() {
        return height;
    }

    public float getLength() {
        return length;
    }

    public float getWidth() {
        return width;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public String getUnit() {
        return unit;
    }
}
