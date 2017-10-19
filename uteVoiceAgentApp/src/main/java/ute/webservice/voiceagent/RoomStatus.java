package ute.webservice.voiceagent;

/**
 * Created by nith on 10/19/17.
 */

public class RoomStatus{

    private String name;
    private int occupiedBeds;

    RoomStatus(String _name, int _occupiedBeds) {
        this.name = _name;
        this.occupiedBeds = _occupiedBeds;
    }


    @Override
    public String toString() {
        return "Unit " + this.name + " has " + this.occupiedBeds + " occupied beds ";
    }
}