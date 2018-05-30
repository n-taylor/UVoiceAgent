package ute.webservice.voiceagent.location;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import ute.webservice.voiceagent.activities.EquipmentFindActivity;
import ute.webservice.voiceagent.util.Controller;

/**
 * Controller for the equipment finder and other location services
 *
 * Created by Nathan Taylor on 5/30/2018.
 */

public class LocationController extends Controller {

    private static Bitmap bitmap;

    public static void startActivity(Context context, Bitmap bitmap){
        LocationController.bitmap = bitmap;
        Intent intent = new Intent(context, EquipmentFindActivity.class);
        context.startActivity(intent);
    }

    public static Bitmap getImage(){
        return bitmap;
    }

}
