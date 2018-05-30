package ute.webservice.voiceagent.dao;

import android.content.Context;

import ute.webservice.voiceagent.location.ClientLocation;

/**
 * The interface to access location services.
 * Created by Nathan Taylor on 5/3/2018.
 */

public interface LocationDAO {
    /**
     * Gets the client location info for a given client.
     *
     * @param ID A mac address, IP address or username.
     * @return the location information of the client
     */
    ClientLocation getClientLocation(String ID, Context context) throws InvalidResponseException, AccessDeniedException;

    void getFloorPlanImage(Context context, String imageName);

}
