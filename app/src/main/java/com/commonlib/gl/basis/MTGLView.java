package com.commonlib.gl.basis;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class MTGLView extends View {

    private Bitmap bitmap;
    public Point mRockerPosition;
    public Point mCtrlPoint;
    private int mRudderRadius = 25;
    public int mWheelRadius = 80;
    private float scale;
    public int isHide = 0;
    private Paint mPaint;

    public MTGLView(Context context) {
        super(context);
    }

    public MTGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
