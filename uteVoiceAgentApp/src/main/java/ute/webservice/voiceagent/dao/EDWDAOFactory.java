package ute.webservice.voiceagent.dao;

import java.util.ArrayList;
import java.util.HashMap;

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
