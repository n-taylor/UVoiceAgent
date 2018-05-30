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
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import ute.webservice.voiceagent.R;
import ute.webservice.voiceagent.location.LocationController;
import ute.webservice.voiceagent.location.MapCoordinate;
import ute.webservice.voiceagent.location.MapDimension;
import ute.webservice.voiceagent.util.SharedData;

/**
 * Created by u0450254 on 5/29/2018.
 */

public class EquipmentFindActivity extends BaseActivity {

    public static final String BITMAP_KEY = "bitmap";


    SharedData sessiondata;

    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;
    private ImageView mImageView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipmentfinder);
        setSupportActionBar((Toolbar) findViewById(R.id.setting_toolbar));
        ActionBar actionBar = getSupportActionBar();

        sessiondata = new SharedData(getApplicationContext());

        mImageView=(ImageView)findViewById(R.id.map);
        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
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

    public MapImageView(Context context) {
        super(context);
    }

    public MapImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MapImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);




       // Drawable d = getResources().getDrawable(R.drawable.testfloor);
        //Bitmap B = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.testfloor);
        Bitmap B = LocationController.getInstance().getImage();
        if (B != null) {

            B = B.createScaledBitmap(B, canvas.getWidth(), canvas.getHeight(), true);


            //d.setBounds(canvas.getClipBounds().left, canvas.getClipBounds().top, canvas.getClipBounds().right, canvas.getClipBounds().bottom);


            Paint paint = new Paint();

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.RED);

            canvas.drawBitmap(B, 0, 0, null);
            MapCoordinate userLoc = LocationController.getInstance().getUserLocation();
            if (userLoc != null){
                drawScaledCircle(canvas, userLoc.getX(), userLoc.getY(), 20.0f, paint);
            }
            //canvas.drawCircle(500.0f, 500.0f, 20.0f, paint);
        }
    }

    private void drawScaledCircle(Canvas canvas, float x, float y, float radius, Paint paint){
        // Get the dimensions of the floor map
        MapDimension dimension = LocationController.getInstance().getDimensions();
        float mapWidth = dimension.getWidth();
        float mapHeight = dimension.getLength();

        // Determine position ratios
        float ratX = x / mapWidth;
        float ratY = y / mapHeight;

        // Convert to position relative to the view size
        float posX = canvas.getWidth() * ratX;
        float posY = canvas.getHeight() * ratY;

        // Draw the dot
        canvas.drawCircle(posX, posY, radius, paint);
    }
}

