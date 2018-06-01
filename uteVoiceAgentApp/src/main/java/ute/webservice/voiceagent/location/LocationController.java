package ute.webservice.voiceagent.location;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;

import ute.webservice.voiceagent.activities.EquipmentFindActivity;
import ute.webservice.voiceagent.exceptions.AccessDeniedException;
import ute.webservice.voiceagent.exceptions.InvalidResponseException;
import ute.webservice.voiceagent.util.Controller;

/**
 * Controller for the equipment finder and other location services
 *
 * Created by Nathan Taylor on 5/30/2018.
 */

public class LocationController extends Controller {

    private Bitmap bitmap;

    private ClientLocation clientLocation;
    private HashMap<String, TagLocation> tagLocations;

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

    public String getImageName() { return clientLocation.getImageName(); }

    public void setClientLocation(ClientLocation location){
        clientLocation = location;
    }

    public MapCoordinate getUserLocation(){
        return clientLocation.getMapCoordinate();
    }

    public MapDimension getDimensions(){
        return clientLocation.getMapDimension();
    }

    public void findTagLocation(String id, Context context){
        GetTagLocationTask task = new GetTagLocationTask(id, context);
        task.execute();
    }

    private void addTagLocation(TagLocation location){
        if (this.tagLocations == null)
            tagLocations = new HashMap<>();

        // Make sure not to add any duplicates
        tagLocations.remove(location.getMacAddress());
        tagLocations.put(location.getMacAddress(), location);
    }

    public HashMap<String, TagLocation> getTagLocations(){
        if (tagLocations == null)
            tagLocations = new HashMap<>();
        return tagLocations;
    }

    private static class GetTagLocationTask extends AsyncTask<Void, Void, TagLocation> {

        String id;
        Context context;

        GetTagLocationTask(String id, Context context){
            this.id = id;
            this.context = context;
        }

        @Override
        protected TagLocation doInBackground(Void... voids) {
            try {
                return Controller.getLocationDAO().getTagLocation(id, context);
            }
            catch (AccessDeniedException | InvalidResponseException ex) {
                ex.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(TagLocation location){
            LocationController.getInstance().addTagLocation(location);
        }
    }

}
