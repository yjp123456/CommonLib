package com.commonlib.gl.basis;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

public class MTGLMagnifierFrameView extends AbsMTGLMagnifierFrameView {


    public MTGLMagnifierFrameView(Context context) {
        super(context);
    }

    public MTGLMagnifierFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MTGLMagnifierFrameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void drawContent(Canvas canvas) {
        if (mPaintContent != null && mPaintBorder != null) {
            canvas.drawCircle(mCircleX, mCircleY, mCircleRadius, mPaintContent);
            canvas.drawCircle(mCircleX, mCircleY, mCircleRadius, mPaintBorder);
            canvas.drawLine(mCircleX - mCircleRadius / 3, mCircleY, mCircleX + mCircleRadius / 3, mCircleY, mPaintBorder);
            canvas.drawLine(mCircleX, mCircleY - mCircleRadius / 3, mCircleX, mCircleY + mCircleRadius / 3, mPaintBorder);
        }
    }

}
