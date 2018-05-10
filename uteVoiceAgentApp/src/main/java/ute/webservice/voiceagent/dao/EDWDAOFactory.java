package ute.webservice.voiceagent.dao;

/**
 * Created by Nathan Taylor on 4/11/2018.
 */

public class EDWDAOFactory extends DAOFactory {

    public OpenBedsDAO getOpenBedsDAO(){
        return new EDWOpenBedsDAO();
    }

    public ProceduresDAO getProceduresDAO(){
        return new EDWProceduresDAO();
    }

}
