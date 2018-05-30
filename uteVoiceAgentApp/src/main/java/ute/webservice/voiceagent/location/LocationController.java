package ute.webservice.voiceagent.location;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;

import java.util.ArrayList;

import ute.webservice.voiceagent.activities.EquipmentFindActivity;
import ute.webservice.voiceagent.util.Controller;

/**
 * Controller for the equipment finder and other location services
 *
 * Created by Nathan Taylor on 5/30/2018.
 */

public class LocationController extends Controller {

    private Bitmap bitmap;

    private ClientLocation clientLocation;

    private static LocationController instance;

    private LocationController(){}

    public static LocationController getInstance(){
        if (instance == null){
            instance = new LocationController();
        }
        return instance;
    }

    public static void startActivity(Context context, Bitmap bitmap){
        LocationController.getInstance().bitmap = bitmap;
        Intent intent = new Intent(context, EquipmentFindActivity.class);
        context.startActivity(intent);
    }

    public Bitmap getImage(){
        return bitmap;
    }

    public void setClientLocation(ClientLocation location){
        clientLocation = location;
    }

    public MapCoordinate getUserLocation(){
        return clientLocation.getMapCoordinate();
    }

    public MapDimension getDimensions(){
        return clientLocation.getMapDimension();
    }

}
