package ute.webservice.voiceagent;

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

    public final static String CLINWEB_QUERY = "https://clinweb.med.utah.edu/pricing-transparency-api/pricing/query";

    public final static String CLINWEB_CENSUS_QUERY = "https://clinweb.med.utah.edu/pricing-transparency-api/census/getCensus";


}
