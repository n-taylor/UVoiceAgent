package ai.api.sample;

import java.util.HashMap;
import java.util.List;

/**
 * Created by u1076070 on 6/14/2017.
 */

//A Hashmap to save queries and answers which are already asked.

public class DataAsked {
    enum type_enum{
        price,
        //success rate,

    }

    //TODO: load data from files
    private HashMap<String,HashMap<String,String>> Map_Sugery = new HashMap<String,HashMap<String,String>>();
    private HashMap<String,String> Hernia = new HashMap<String,String>();
    private HashMap<String,String> Bypass = new HashMap<String,String>();
    //private List<>
    private String Question_type="";
    private String Surgery_type="";

    DataAsked()
    {
        //Test: initialize Hashmap by program
        //TODO initialize HashMap by loading file
        Hernia.put("price","6000");
        Hernia.put("success rate","98%");
        Hernia.put("recovery time","18");

        Bypass.put("price","70000");
        Bypass.put("success rate","90%");
        Bypass.put("recovery time","30");

        Map_Sugery.put("hernia repair surgery",Hernia);
        Map_Sugery.put("coronary artery bypass",Bypass);
    }

    public boolean isParameter_Enough(){
        if(Question_type.equals("")==false && Surgery_type.equals("")==false)
            return true;
        return false;
    }

    public void clear_params(){
        this.Question_type = "";
        this.Surgery_type = "";
    }

    public void assign_params(String question_t, String surgery_t){
        this.Question_type = question_t;
        this.Surgery_type = surgery_t;
    }

    public String get_info(){
        HashMap<String,String> info= Map_Sugery.get(Surgery_type);
        //info.get(Question_type);
        if(Question_type.equals("price")) {
            return new String("The average "+Question_type+" of "+Surgery_type+" is "+info.get(Question_type)+"$.");
        }
        else if(Question_type.equals("recovery time")){
            return new String("The average "+Question_type+" of "+Surgery_type+" is "+info.get(Question_type)+" days.");
        }
        else{
            return new String("The average "+Question_type+" of "+Surgery_type+" is "+info.get(Question_type)+".");
        }
    }




}
