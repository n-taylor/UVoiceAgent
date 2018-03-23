package ute.webservice.voiceagent;

import android.content.Context;
import android.widget.ExpandableListView;

/**
 * Created by Nathan Taylor on 3/20/2018.
 */

public class CustomExpListView extends ExpandableListView
{

    private int preferredWidth = 1000;

    public CustomExpListView(Context context)
    {
        super(context);
    }
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(preferredWidth, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(100000, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Sets the preferred width of the second and third-level items
     * @param width
     */
    public void setPreferredWidth(int width){
        this.preferredWidth = width;
    }
}