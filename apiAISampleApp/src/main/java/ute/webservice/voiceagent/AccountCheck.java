package ute.webservice.voiceagent;

import java.util.HashMap;

/**
 * Connect to server and verify login authentication.
 * Created by u1076070 on 6/29/2017.
 */

public class AccountCheck {
    //TODO: Connect to account database

    private HashMap<String,String> account_map = new HashMap<String, String>();
    private HashMap<String,Integer> admin_map = new HashMap<String, Integer>();

    private String accountID;

    public AccountCheck(){

        account_map.put("account02","abcd");
        account_map.put("account01","1234");
        account_map.put("admin","admin");

        admin_map.put("admin", Constants.ACCESS_LEVEL_ADMIN);
        admin_map.put("account01", Constants.ACCESS_LEVEL_HIGH);
        admin_map.put("account02", Constants.ACCESS_LEVEL_LOW);
    }

    /**
     * Return current user ID, it may be null.
     * @return
     */
    public String getAccountID() {
        return accountID;
    }

    /**
     * Save current user ID
     * @param inputID
     */
    private void setAccountID(String inputID){
        accountID=inputID;
    }

    /**
     * Return authorization level
     * @return
     */
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
        if(!account_map.containsKey(ID))
            return false;
        if(!account_map.get(ID).equals(password))
            return false;

        setAccountID(ID);
        return true;
    }

}
