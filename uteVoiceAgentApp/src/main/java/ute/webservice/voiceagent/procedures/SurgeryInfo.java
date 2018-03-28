package ute.webservice.voiceagent.procedures;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by nith on 10/20/17.
 */

public class SurgeryInfo {
    private String name;
    private String cost;

    public SurgeryInfo(String _name, String _cost) {
        this.name = _name;
        this.cost = _cost;
    }

    public String getName(){return this.name;}

    public String getCost(){return this.cost;}

}
