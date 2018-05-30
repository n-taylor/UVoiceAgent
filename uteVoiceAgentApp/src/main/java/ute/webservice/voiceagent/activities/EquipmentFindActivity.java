package ute.webservice.voiceagent.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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


    private static float MIN_ZOOM = 1f;
    private static float MAX_ZOOM = 5f;

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

        canvas.save();
        canvas.scale(scaleFactor, scaleFactor);

        if ((translateX * -1) < 0) {
            translateX = 0;
        } else if ((translateX * -1) > (scaleFactor - 1) * canvas.getWidth()) {
            translateX = (1 - scaleFactor) * canvas.getWidth();
        }

        if (translateY * -1 < 0) {
            translateY = 0;
        } else if ((translateY * -1) > (scaleFactor - 1) * canvas.getHeight()) {
            translateY = (1 - scaleFactor) * canvas.getHeight();
        }

        canvas.translate(translateX / scaleFactor, translateY / scaleFactor);
        Drawable d = getResources().getDrawable(R.drawable.testfloor);
        //Bitmap B = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.testfloor);
        Bitmap B = LocationController.getImage();
        if (B != null) {

            B = B.createScaledBitmap(B, canvas.getWidth(), canvas.getHeight(), true);

            d.setBounds(canvas.getClipBounds().left, canvas.getClipBounds().top, canvas.getClipBounds().right, canvas.getClipBounds().bottom);

            Paint paint = new Paint();

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.RED);

            canvas.drawBitmap(B, 0, 0, null);
            canvas.drawCircle(500.0f, 500.0f, 200.0f, paint);

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
}

