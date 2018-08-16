package ute.webservice.voiceagent.util;

import java.util.HashMap;

/**
 * Save global constant.
 * Created by u1076070 on 6/30/2017.
 */

public final class Constants {
    //TODO: load string variable from file
    //Define ACCESS LEVEL
    public final static int  ACCESS_LEVEL_ADMIN=0;
    public final static int  ACCESS_LEVEL_HIGH=1; //Can access bypass info
    public final static int  ACCESS_LEVEL_LOW=2;  //Can access hernia info

    public final static String SURGERY_HERNIA="\"hernia repair surgery\"";
    public final static String SURGERY_BYPASS="\"coronary artery bypass\"";

    public final static String QUESTION_TYPE="\"questionType\"";
    public final static String SURGERY_TYPE = "\"surgery\"";

    public final static String USER="\"username\"";
    public final static String PASSWORD = "\"password\"";
    public final static String CODE = "\"code\"";

    public final static String LOCALHOST = "10.0.2.2";
    public final static String SERVER_DEV = "172.20.150.205";
    public final static String SERVER_IP = SERVER_DEV;
    public final static int PORT = 8042;
    public final static String HOST = "https://" + SERVER_IP + ":" + PORT;

    public final static String ACCESS_DENIED = "Access Denied";
    public final static int ACCESS_DENIED_INT = -999;
    public final static String SESSION_EXPIRED_MESSAGE = "Your session has expired. Please log out and then log in again.";
    public final static String AUTHENTIC_LINK = "https://" + SERVER_IP + ":" + PORT + "/login";
//    public final static String AUTHENTIC_LINK_LOGOUT = "https://clinweb.med.utah.edu/pricing-transparency-api/auth/logout";
    public final static String AUTHENTIC_LINK_LOGOUT = "https://" + SERVER_IP + ":" + PORT + "/logout";


//    public final static String CLINWEB_PRICE_QUERY = "https://clinweb.med.utah.edu/pricing-transparency-api/pricing/get/";

    public final static String CLINWEB_SURGERY_CATEGORIES_QUERY = "https://" + SERVER_IP + ":" + PORT + "/procedures/allCategories";
    public final static String CLINWEB_SURGERY_CODES_QUERY = "https://" + SERVER_IP + ":" + PORT + "/procedures/codes";
    public final static String CLINWEB_SURGERY_COST_BY_CODE_QUERY = "https://" + SERVER_IP + ":" + PORT + "/procedures/cost";

    public final static String CLINWEB_CENSUS_QUERY = "https://" + SERVER_IP + ":" + PORT + "/bedCensus/all";
    public final static String CLINWEB_CENSUS_SPECFIC_QUERY = "https://" + SERVER_IP + ":" + PORT + "/bedCensus/unit/";

    public final static String GET_SURGERY_COST = "getSurgeryCost";
    public final static String GET_CENSUS = "getCensus";
    public final static String GET_ONCALL = "getOnCall";
    public final static String FIND_EQUIPMENT = "findEquipment";
    public final static String ACTION_UNKNOWN = "input.unknown";
    public final static String PARTIAL_ACTION = "partialReceived";

    /**
     * The category name that is received from Dialog Flow as a parameter value.
     * DF stands for Dialog Flow.
     */
    public final static String DF_PARAM_EQUIPMENT_CATEGORY = "EquipmentCategory";
    public final static String DF_PARAM_CENSUS_UNIT = "censusUnit";
    public final static String DF_PARAM_PROCEDURE_CATEGORY = "SurgeryCategory";

    public HashMap<String, String> units = new HashMap<String, String>();

    public Constants(){
        units.put("AIMA", "A I M A");
        units.put("AIMB","A I M B");
        units.put("BRN","B R N");
        units.put("CVICU","C V I C U");
        units.put("CVMU","C V M U");
        units.put("HCBMT","H C B M T");
        units.put("HCH4","H C H 4");
        units.put("HCH5","H C H 5");
        units.put("HCICU","H C I C U");
        units.put("ICN","I C N");
        units.put("IMR","I M R");
        units.put("LND","L N D");
        units.put("MICU","M I C U");
        units.put("MNBC","M N B C");
        units.put("NAC","N A C");
        units.put("NCCU","N C C U");
        units.put("NICU","N I C U");
        units.put("NNCCN","N N C C N");
        units.put("NSY","N S Y");
        units.put("OBGY","O B G Y");
        units.put("OTSS","O T S S");
        units.put("SICU","S I C U");
        units.put("SSTU","S S T U");
        units.put("UUOC","U U O C");
        units.put("GI", "G I");
        units.put("5 W", "Five West");
    }

}
