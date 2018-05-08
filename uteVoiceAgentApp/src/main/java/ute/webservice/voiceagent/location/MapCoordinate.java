package ute.webservice.voiceagent.location;

/**
 * A class to define an x and y coordinate and its unit.
 * If a unit is not specified, FEET is the default
 *
 * Created by Nathan Taylor on 5/3/2018.
 */

public class MapCoordinate {
    private float x;
    private float y;
    private String unit = "FEET";

    public MapCoordinate(float x, float y, String unit){
        this.x = x;
        this.y = y;
        this.unit = unit;
    }

    public MapCoordinate(float x, float y){
        this.x = x;
        this.y = y;
    }

    public float getX(){
        return x;
    }

    public float getY(){
        return y;
    }

    public String getUnit(){ return unit;}
}
