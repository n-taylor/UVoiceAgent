package ai.api.sample;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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

    private Constants const_value;

    //TODO: load data from files
    private HashMap<String,HashMap<String,String>> Map_Sugery = new HashMap<String,HashMap<String,String>>();
    private HashMap<String,String> Hernia = new HashMap<String,String>();
    private HashMap<String,String> Bypass = new HashMap<String,String>();

    private String Question_type="";
    private String Surgery_type="";

    private HashMap<Integer,HashSet<String>> Admin_group = new HashMap<Integer,HashSet<String>>();

    /**
     * Initialize surgery database and administration with HashMap
     */
    DataAsked()
    {
        const_value=new Constants();
        //Test: initialize Hashmap by program
        //TODO initialize HashMap by loading file
        Hernia.put("price","6000");
        Hernia.put("success rate","98%");
        Hernia.put("recovery time","18");

        Bypass.put("price","70000");
        Bypass.put("success rate","90%");
        Bypass.put("recovery time","30");

        Map_Sugery.put(const_value.SURGERY_HERNIA,Hernia);
        Map_Sugery.put(const_value.SURGERY_BYPASS,Bypass);

        //Initialize administration group
        Admin_group.put(const_value.ACCESS_LEVEL_ADMIN,new HashSet<>(Arrays.asList(const_value.SURGERY_HERNIA,const_value.SURGERY_BYPASS)));
        Admin_group.put(const_value.ACCESS_LEVEL_HIGH,new HashSet<>(Arrays.asList(const_value.SURGERY_BYPASS)));
        Admin_group.put(const_value.ACCESS_LEVEL_LOW,new HashSet<>(Arrays.asList(const_value.SURGERY_HERNIA)));

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

    /**
     * Check if the current user can access data related to his/her question
     * @param access_level
     * @return accessable, return true.
     */
    public boolean IsAccessable(int access_level){
        if(Admin_group.containsKey(access_level)&&Admin_group.get(access_level).contains(this.Surgery_type))
            return true;
        return false;
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
