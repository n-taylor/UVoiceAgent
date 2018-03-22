package ute.webservice.voiceagent;

import java.util.HashMap;

/**
 * Created by nith on 10/20/17.
 */

public class SurgeryInfo {
    private String name;
    private String cost;
    private HashMap<String, String> codes;

    SurgeryInfo(String _name, String _cost) {
        this.name = _name;
        this.cost = _cost;
    }

    SurgeryInfo(HashMap<String, String> codes){
        this.codes = codes;
    }

    public String getName(){return this.name;}

    public String getCost(){return this.cost;}

    public HashMap<String, String> getCodes(){return this.codes;}

}
