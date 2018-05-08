package ute.webservice.voiceagent.dao;

/**
 * Produces a LocationDAO
 * Created by Nathan Taylor on 5/3/2018.
 */

public class CiscoDAOFactory extends DAOFactory {

    public LocationDAO getLocationDAO() { return new CiscoLocationDAO(); }

}
