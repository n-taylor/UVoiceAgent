package ai.api.sample;

//import android.app.Application;

import java.util.HashMap;

/**
 * Created by u1076070 on 6/29/2017.
 */

public class AccountCheck {
    //TODO: Connect to account database
    private Constants const_value;
    //final static int  ACCESS_LEVEL_HIGH=1;
    //final static int  ACCESS_LEVEL_LOW=2;
    //final static int  ACCESS_LEVEL_ADMIN=0;

    private HashMap<String,String> account_map = new HashMap<String, String>();
    private HashMap<String,Integer> admin_map = new HashMap<String, Integer>();

    private String accountID;

    public AccountCheck(){

        const_value=new Constants();

        account_map.put("account02","abcd");
        account_map.put("account01","1234");
        account_map.put("admin","admin");

        admin_map.put("admin",const_value.ACCESS_LEVEL_ADMIN);
        admin_map.put("account01",const_value.ACCESS_LEVEL_HIGH);
        admin_map.put("account02",const_value.ACCESS_LEVEL_LOW);
    }
    public String getAccountID() {
        return accountID;
    }
    private void setAccountID(String inputID){
        accountID=inputID;
    }

    public int getAccessLevel(){
        if(accountID!=null && !accountID.isEmpty())
            return admin_map.get(accountID);
        return -1;
    }

    /**
     * Connect to account database, and then check if account exist and password is correct.
     * @param ID Input account ID
     * @param password Input password
     * @return return true if all input information is right; else false.
     */
    public boolean isAccountCorrect(String ID, String password){
        if(account_map.containsKey(ID)==false)
            return false;
        if(account_map.get(ID).equals(password)==false)
            return false;

        setAccountID(ID);
        return true;
    }

}
