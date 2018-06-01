package ute.webservice.voiceagent.dao;

import android.content.Context;

import ute.webservice.voiceagent.exceptions.AccessDeniedException;
import ute.webservice.voiceagent.exceptions.InvalidResponseException;
import ute.webservice.voiceagent.location.ClientLocation;
import ute.webservice.voiceagent.location.TagLocation;

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

    /**
     * Gets the tag location info for a given tag.
     *
     * @param id The mac address of the tag.
     * @return the location of the tag, or null if there was a problem retrieving its location.
     */
    TagLocation getTagLocation(String id, Context context) throws InvalidResponseException, AccessDeniedException;

    void getFloorPlanImage(Context context, String imageName);

}
