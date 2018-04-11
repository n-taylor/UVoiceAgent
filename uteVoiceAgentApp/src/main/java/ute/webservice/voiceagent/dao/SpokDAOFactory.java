package ute.webservice.voiceagent.dao;

/**
 * Created by Nathan Taylor on 4/11/2018.
 */

public class SpokDAOFactory extends DAOFactory {

    public OnCallDAO getOnCallDAO(){
        return new SpokOnCallDAO();
    }
}
