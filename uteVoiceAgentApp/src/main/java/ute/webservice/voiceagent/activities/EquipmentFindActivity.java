package ute.webservice.voiceagent.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import ai.api.android.AIConfiguration;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Location;
import ai.api.ui.AIButton;
import ute.webservice.voiceagent.R;
import ute.webservice.voiceagent.dao.LocationDAO;
import ute.webservice.voiceagent.location.ClientLocation;
import ute.webservice.voiceagent.location.LocationController;
import ute.webservice.voiceagent.location.MapCoordinate;
import ute.webservice.voiceagent.location.MapDimension;
import ute.webservice.voiceagent.location.TagLocation;
import ute.webservice.voiceagent.util.Config;
import ute.webservice.voiceagent.util.Controller;
import ute.webservice.voiceagent.util.DataAsked;
import ute.webservice.voiceagent.util.SharedData;

import static android.net.wifi.WifiConfiguration.Status.strings;
import static ute.webservice.voiceagent.util.Controller.getLocationDAO;

/**
 * Created by u0450254 on 5/29/2018.
 */

public class EquipmentFindActivity extends BaseActivity implements AIButton.AIButtonListener {

    private static final String TAG = EquipmentFindActivity.class.toString();

    public static final String BITMAP_KEY = "bitmap";

    private AIButton aiButton;
    private String accountID;

    SharedData sessiondata;

    Context context = this;

    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;
    private ImageView mImageView;

    private Timer timer;
    private TimerTask repeatedTask;
    private final long timerDelay = 5000L;
    private final long timerPeriod = 5000L;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipmentfinder);
        setSupportActionBar((Toolbar) findViewById(R.id.setting_toolbar));
        ActionBar actionBar = getSupportActionBar();

        sessiondata = new SharedData(getApplicationContext());

        mImageView=(ImageView)findViewById(R.id.map);
        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        mImageView.setImageResource(R.drawable.white);

        initializeToolbar();
        initializeButtons();
        initializeSharedData();

        repeatedTask = new TimerTask() {
            public void run() {
                redrawTask();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageView.invalidate();
                    }
                });
               // mImageView.invalidate();
            }

        };
        timer = new Timer("Timer");

        timer.scheduleAtFixedRate(repeatedTask, timerDelay, timerPeriod);
    }

    private void redrawTask()
    {
        ClientLocation location = null;
        try {
//            location = getLocationDAO().getClientLocation("f8:34:41:bf:ab:ee",this);
            location = getLocationDAO().getClientLocation(Controller.getMacAddr().toLowerCase(Locale.US), context, LocationDAO.PARK);

        }
        catch (Exception e){
            e.printStackTrace();

        }
        LocationController.getInstance().setClientLocation(location);
        LocationController.getInstance().findTagLocation("00:12:b8:0d:0a:2b", context);
    }


    /**
     * Sets up the sessiondata, dataAsked and account data variables.
     */
    private void initializeSharedData(){
        SharedData sessiondata = new SharedData(getApplicationContext());
        accountID = sessiondata.getKeyAccount();
        int account_access = sessiondata.getKeyAccess();
        DataAsked dataAsked = new DataAsked();
    }

    /**
     * Sets up the toolbar elements for this activity
     */
    private void initializeToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Sets up the AI button and cancel button for this activity.
     */
    private void initializeButtons(){
        // configure the AI Button
        aiButton = (AIButton)findViewById(R.id.micButton);
        final AIConfiguration config = new AIConfiguration(Config.ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        config.setRecognizerStartSound(getResources().openRawResourceFd(R.raw.test_start));
        config.setRecognizerStopSound(getResources().openRawResourceFd(R.raw.test_stop));
        config.setRecognizerCancelSound(getResources().openRawResourceFd(R.raw.test_cancel));

        // set up the microphone/AI button
        aiButton.initialize(config);
        aiButton.setResultsListener(this);

        // set up the cancel button
        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Controller.getController().onCancelPressed();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        // stop the timer
        timer.cancel();

        // use this method to disconnect from speech recognition service
        // Not destroying the SpeechRecognition object in onPause method would block other apps from using SpeechRecognition service
        aiButton.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Start the timer again
        repeatedTask = new TimerTask() {
            public void run() {
                redrawTask();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageView.invalidate();
                    }
                });
                // mImageView.invalidate();
            }

        };
        timer = new Timer("Timer");

        timer.scheduleAtFixedRate(repeatedTask, timerDelay, timerPeriod);

        // use this method to reinit connection to recognition service
        aiButton.resume();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_aibutton_sample, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;

            case R.id.action_logout:
                Controller.getController().onLogoutPressed(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Show response from the API.AI server,
     * If parameters are enough and user said "Yes", try to get data from webservice.
     * @param response
     */
    @Override
    public void onResult(final AIResponse response) {
        Controller.processDialogFlowResponse(this, response, null);
    }

    @Override
    public void onError(final AIError error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onError");
            }
        });
    }

    @Override
    public void onCancelled() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onCancelled");
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        mScaleGestureDetector.onTouchEvent(motionEvent);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector){
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(1.0f,Math.min(mScaleFactor, 10.0f));
            mImageView.setScaleX(mScaleFactor);
            mImageView.setScaleY(mScaleFactor);
            return true;
        }
    }
}


class MapImageView extends AppCompatImageView {

    private boolean beginning = true;

    private Bitmap B;
    private Paint clientPaint;
    private Paint clientHalo;
    private Paint tagPaint;
    private Paint tagHalo;

    public MapImageView(Context context) {
        super(context);
        initializeValues();
    }

    public MapImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeValues();
    }

    public MapImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeValues();
    }

    private static float MIN_ZOOM = 0.1f;
    private static float MAX_ZOOM = 5f;

    private static float DOT_SIZE = 20.0f;
    private static float HALO_SIZE = 150.0f;
    private static final int ALPHA = 90;

    private float scaleFactor = 1.f;
    private ScaleGestureDetector detector = new ScaleGestureDetector(getContext(), new ScaleListener());

    private static int NONE = 0;
    private static int DRAG = 1;
    private static int ZOOM = 2;

    private int mode;

    private float startX = 0f;
    private float startY = 0f;

    private float translateX = 0f;
    private float translateY = 0f;

    private float previousTranslateX = 0f;
    private float previousTranslateY = 0f;
    private boolean dragged = false;

    private void initializeValues(){
        B = LocationController.getInstance().getImage();

        clientPaint = new Paint();
        clientPaint.setStyle(Paint.Style.FILL);
        clientPaint.setColor(Color.RED);

        clientHalo = new Paint();
        clientHalo.setStyle(Paint.Style.FILL);
        clientHalo.setColor(clientPaint.getColor());
        clientHalo.setAlpha(ALPHA);

        tagPaint = new Paint();
        tagPaint.setStyle(Paint.Style.FILL);
        tagPaint.setColor(Color.BLUE);

        tagHalo = new Paint();
        tagHalo.setStyle(Paint.Style.FILL);
        tagHalo.setColor(tagPaint.getColor());
        tagHalo.setAlpha(ALPHA);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mode = DRAG;

                startX = event.getX() - previousTranslateX;
                startY = event.getY() - previousTranslateY;
                break;
            case MotionEvent.ACTION_MOVE:
                translateX = event.getX() - startX;
                translateY = event.getY() - startY;

                double distance = Math.sqrt(Math.pow(event.getX() - (startX + previousTranslateX), 2) +
                        Math.pow(event.getY() - (startY + previousTranslateY), 2)
                );
                if(distance > 0) {
                    dragged = true;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = ZOOM;
                break;
            case MotionEvent.ACTION_UP:
                mode = NONE;
                dragged = false;
                previousTranslateX = translateX;
                previousTranslateY = translateY;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = DRAG;
                previousTranslateX = translateX;
                previousTranslateY = translateY;
                break;
        }
        detector.onTouchEvent(event);
        if ((mode == DRAG && scaleFactor != 1f && dragged) || mode == ZOOM) {
            invalidate();
        }
        return true;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (beginning){
            scaleFactor = (float)canvas.getWidth() / (float)B.getWidth();
            beginning = false;
        }

        canvas.save();
        canvas.scale(scaleFactor, scaleFactor);

        float scaledWidth = B.getWidth() *scaleFactor;
        float scaledHeight= B.getHeight() *scaleFactor;

//        System.out.println("TX: "+translateX);
//        System.out.println("TX2: "+scaledWidth);

        // TEST ------------------------------------------------------------

        if (scaledWidth < canvas.getWidth()){
            // If the map is more narrow than the view, don't allow it to move past the edges
            if (translateX < 0){
                translateX = 0;
            }
            else if ((translateX + scaledWidth) > canvas.getWidth()){
                translateX = canvas.getWidth() - scaledWidth;
            }
        }
        else {
            // If the map is wider than the view, don't allow the edge to move into the view
            if (translateX > 0){
                translateX = 0;
            }
            else if ((translateX + scaledWidth) < canvas.getWidth()){
                translateX = canvas.getWidth() - scaledWidth;
            }
        }

        if (scaledHeight < canvas.getHeight()){
            // If the map is shorter than the view, don't allow it to move past the edges
            if (translateY < 0){
                translateY = 0;
            }
            else if ((translateY + scaledHeight) > canvas.getHeight()){
                translateY = canvas.getHeight() - scaledHeight;
            }
        }
        else {
            // If the map is taller than the view, don't allow its edge to move into the view
            if (translateY > 0){
                translateY = 0;
            }
            else if ((translateY + scaledHeight) < canvas.getHeight()){
                translateY = canvas.getHeight() - scaledHeight;
            }
        }

        // TEST --------------------------------------------------------------

        /*if (translateX > 0) {
            translateX = 0;
        }
        else if (translateX + scaledWidth < canvas.getWidth()){
            translateX = canvas.getWidth() - scaledWidth;
        }
        if (translateY > 0) {
            translateY = 0;
        }
        else if (scaledHeight < canvas.getHeight() && (translateY + scaledHeight + (canvas.getHeight() - scaledHeight)) < canvas.getHeight())
        {

        }
        else if ((translateY + scaledHeight) > canvas.getHeight()){
            translateY = canvas.getHeight() - scaledHeight;
        }*/

        canvas.translate(translateX / scaleFactor, translateY / scaleFactor);

        //Drawable d = getResources().getDrawable(R.drawable.testfloor);
        //Bitmap B = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.testfloor);

        if (B != null) {

            canvas.drawBitmap(B, 0, 0, null);

            MapCoordinate userLoc = LocationController.getInstance().getUserLocation();
            if (userLoc != null){
                drawScaledCircle(canvas, B, userLoc.getX(), userLoc.getY(), DOT_SIZE, clientPaint);
                //drawScaledCircle(canvas, B, userLoc.getX(), userLoc.getY(), HALO_SIZE, clientHalo);
            }

            HashMap<String, TagLocation> tags = LocationController.getInstance().getTagLocations();
            for (String key : tags.keySet()){
                TagLocation loc = tags.get(key);
                if (loc != null && LocationController.getInstance().getImageName().equals(loc.getImageName())){
                    MapCoordinate coordinate = loc.getMapCoordinate();
                    drawScaledCircle(canvas, B, coordinate.getX(), coordinate.getY(), DOT_SIZE, tagPaint);
                    //drawScaledCircle(canvas, B, coordinate.getX(), coordinate.getY(), HALO_SIZE, tagHalo);
                }
            }
            //canvas.drawCircle(500.0f, 500.0f, 20.0f, paint);

            canvas.restore();
        }
    }
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            scaleFactor *= detector.getScaleFactor();

            scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));

            return true;
        }
    }

    private void drawScaledCircle(Canvas canvas, Bitmap image, float x, float y, float radius, Paint paint){
        // Get the dimensions of the floor map
        MapDimension dimension = LocationController.getInstance().getDimensions();
        float mapWidth = dimension.getWidth();
        float mapHeight = dimension.getLength();

        // Determine position ratios
        float ratX = x / mapWidth;
        float ratY = y / mapHeight;

        // Convert to position relative to the view size
        float posX = image.getWidth() * ratX;
        float posY = image.getHeight() * ratY;

        // Draw the dot
        canvas.drawCircle(posX, posY, radius, paint);
    }
}

