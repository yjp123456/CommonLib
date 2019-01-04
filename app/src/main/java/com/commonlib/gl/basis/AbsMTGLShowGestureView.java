package com.commonlib.gl.basis;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


/**
 * 祛痘+眼睛放大View
 */
public abstract class AbsMTGLShowGestureView extends View {
    protected int mPenSize = 15;// 画笔的半径
    protected float mCurrentX;// 当前手势坐标
    protected float mCurrentY;

    public boolean isShowDrawPoint = false;
    private boolean isEnable = false;


    protected Paint mPaintBorder;// 画笔的边缘paint


    protected Paint mPaintContent;// 画笔内部Paint

    public boolean isShowCenterPen = false;// 是否展示预览画笔大小


    public AbsMTGLShowGestureView(Context context) {
        super(context);
        init();
    }

    public AbsMTGLShowGestureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    public void init() {
        mPaintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBorder.setStyle(Paint.Style.STROKE);
        mPaintBorder.setColor(0xffa9a9a9);
        mPaintBorder.setAntiAlias(true);
        //mPaintBorder.setStrokeWidth(2 * MyData.nDensity);

        mPaintContent = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintContent.setStyle(Paint.Style.FILL);
        mPaintContent.setColor(0x4c080808);
        mPaintContent.setAntiAlias(true);

    }

    public void setPaintBorder(Paint paintBorder) {
        this.mPaintBorder = paintBorder;
    }

    public void setPaintContent(Paint paintContent) {
        this.mPaintContent = paintContent;
    }

    public void setPenSize(int penSize) {
        mPenSize = penSize;
    }

    // 画笔的坐标
    public void setCurrentPoint(float x, float y) {
        mCurrentX = x;
        mCurrentY = y;
        Log.d("test", "getCurrentPoint " + "x = " + x + "y = " + y);
        invalidate();
    }

    public void setOperateEnable(boolean enable) {
        isEnable = enable;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if ((isShowDrawPoint && isEnable) || isShowCenterPen) {
            drawContent(canvas);
        }

    }

    protected abstract void drawContent(Canvas canvas);


}
