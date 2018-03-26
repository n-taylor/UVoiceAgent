package ute.webservice.voiceagent;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by nith on 10/20/17.
 */

public class SurgeryInfo {
    private String name;
    private String cost;

    SurgeryInfo(String _name, String _cost) {
        this.name = _name;
        this.cost = _cost;
    }

    public String getName(){return this.name;}

    public String getCost(){return this.cost;}

}
