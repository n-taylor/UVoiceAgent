package ai.api.sample;

/**
 * Created by u1076070 on 6/30/2017.
 */
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class LoginAlertDialog {

    /**
     * Display Alert, disappear after clicking button "OK".
     * @param context application context
     * @param title title of the dialog
     * @param message message to user
     * @param status default: null
     */
    public void showAlertDialog(Context context, String title, String message,
                                Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        //TODO: Setting alert dialog icon
        //if(status != null)
        //    alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);

        // Setting OK Button
        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,"OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }
}
