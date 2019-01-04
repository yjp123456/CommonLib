package com.commonlib.gl.basis;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.commonlib.gl.util.DeviceUtil;


/**
 * 放大镜边框与圆圈控件
 * 由于放大镜中图片内容由OpenGL实时渲染，且圆圈超出边缘情况，故独立使用一个View来绘制
 *
 * @author YJP 2018/2/26.
 */

public abstract class AbsMTGLMagnifierFrameView extends View {

    //画笔的边缘paint
    protected Paint mPaintBorder;
    // 笔触内容画笔
    protected Paint mPaintContent;
    // 边框画笔
    protected Paint mPaintFrame;
    //边框圆角
    private float mRoundCorners = 0;
    // 圆圈半径
    protected float mCircleRadius;
    // 笔触圆圈中心点
    protected float mCircleX, mCircleY;

    /**
     * 边框笔画宽度
     */
    private float mFrameStrokeWidth;

    /**
     * 是否展示放大镜框
     */
    private boolean mIsShow = false;

    private RectF mScreen;


    public AbsMTGLMagnifierFrameView(Context context) {
        super(context);
        initView(context, null);
    }

    public AbsMTGLMagnifierFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public AbsMTGLMagnifierFrameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    protected void initView(Context context, AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }

        mCircleRadius = DeviceUtil.dip2px(10);

        mPaintFrame = new Paint();
        mPaintFrame.setAntiAlias(true);
        mPaintFrame.setColor(Color.WHITE);
        mPaintFrame.setStyle(Paint.Style.STROKE);

        mPaintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBorder.setStyle(Paint.Style.STROKE);
        mPaintBorder.setColor(0xffa9a9a9);
        mPaintBorder.setAntiAlias(true);
        //mPaintBorder.setStrokeWidth(2 * MyData.nDensity);

        mPaintContent = new Paint();
        mPaintContent.setAntiAlias(true);
        mPaintContent.setStyle(Paint.Style.FILL);
        mPaintContent.setColor(Color.WHITE);
        mPaintContent.setAlpha(102);
    }

    /**
     * 设置放大镜边框画笔
     *
     * @param paint
     */
    public void setFramePaint(Paint paint) {
        mPaintFrame = paint;
    }


    /**
     * 设置笔触圆圈内容画笔
     *
     * @param paint
     */
    public void setContentPaint(Paint paint) {
        mPaintContent = paint;
    }

    /**
     * 设置笔触圆圈内容画笔
     *
     * @param paint
     */
    public void setBorderPaint(Paint paint) {
        mPaintBorder = paint;
    }

    /**
     * 设置笔触圆圈
     *
     * @param penSizePx
     */
    public void setPenSize(float penSizePx) {
        mCircleRadius = penSizePx;
    }

    /**
     * 设置边框圆角弧度
     *
     * @param raidus
     */
    public void setRoundCorner(float raidus) {
        mRoundCorners = raidus;
    }

    public void show(float offsetX, float offsetY) {
        mIsShow = true;
        mCircleX = getWidth() / 2 + offsetX;
        mCircleY = getHeight() / 2 + offsetY;

        if (mCircleX < 0f) {
            mCircleX = 0f;
        }
        if (mCircleX > getWidth()) {
            mCircleX = getWidth();
        }
        if (mCircleY < 0f) {
            mCircleY = 0f;
        }
        if (mCircleY > getHeight()) {
            mCircleY = getHeight();
        }

        postInvalidate();
    }

    public void dismiss() {
        mIsShow = false;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mIsShow) {
            if (mPaintFrame != null) {
                if (mScreen == null) {
                    mScreen = new RectF(0, 0, getWidth(), getHeight());
                } else {
                    mScreen.set(0, 0, getWidth(), getHeight());
                }
                canvas.drawRoundRect(mScreen, mRoundCorners, mRoundCorners, mPaintFrame);
            }
            drawContent(canvas);
        }
    }

    protected abstract void drawContent(Canvas canvas);


    /**
     * 设置放大镜边框笔画宽度
     *
     * @param frameStrokeWidth 边框笔画宽度
     */
    public void setFrameStrokeWidth(float frameStrokeWidth) {
        mFrameStrokeWidth = frameStrokeWidth;
        if (mPaintFrame != null) {
            // 边框宽度*2，一半会被裁减
            mPaintFrame.setStrokeWidth(frameStrokeWidth);
        }
    }

    /**
     * 获取放大镜边框笔画宽度
     *
     * @return 边框笔画宽度
     */
    public float getFrameStrokeWidth() {
        return mFrameStrokeWidth;
    }

}
