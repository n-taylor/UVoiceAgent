package ute.webservice.voiceagent;

import java.util.HashMap;

/**
 * Save global constant.
 * Created by u1076070 on 6/30/2017.
 */

final class Constants {
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
    public final static String ACCESS_DENIED = "Access Denied";
    public final static String AUTHENTIC_LINK = "https://clinweb.med.utah.edu/pricing-transparency-api/auth/login";
    public final static String AUTHENTIC_LINK_LOGOUT = "https://clinweb.med.utah.edu/pricing-transparency-api/auth/logout";

    public final static String CLINWEB_QUERY = "https://clinweb.med.utah.edu/pricing-transparency-api/pricing/get/";

    public final static String CLINWEB_SURGERY_CATEGORIES_QUERY = "https://clinweb.med.utah.edu/pricing-transparency-api/pricing/getCategories";

    public final static String CLINWEB_CENSUS_QUERY = "https://clinweb.med.utah.edu/pricing-transparency-api/census/getCensus";
    public final static String CLINWEB_CENSUS_SPECFIC_QUERY = "https://clinweb.med.utah.edu/pricing-transparency-api/census/getCensusByUnit/";

    public final static String GET_SURGERY_COST = "getSurgeryCost";
    public final static String GET_CENSUS = "getCensus";

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
