package ute.webservice.voiceagent.dao;

/**
 * Produces DAO factories to access data from EDW (webservice) or Spok (on call)
 * Created by Nathan Taylor on 4/11/2018.
 */

public abstract class DAOFactory {

    public static final int EDW = 0;
    public static final int SPOK = 1;

    public static DAOFactory getDAOFactory(int type){
        switch (type){
            case EDW:
                return new EDWDAOFactory();
            case SPOK:
                return new SpokDAOFactory();
            default:
                return null;
        }
    }

}
