package ute.webservice.voiceagent;

/**
 * Created by nith on 10/20/17.
 */

public class SurgeryInfo {
    private String name;
    private int cost;

    SurgeryInfo(String _name, int _cost) {
        this.name = _name;
        this.cost = _cost;
    }

    public String getName(){return this.name;}

    public int getCost(){return this.cost;}

}
