package com.commonlib.gl.basis;

import android.graphics.Paint;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.commonlib.gl.util.DeviceUtil;


public class MTGLMagnifierListener extends AbsMTGLGestureListener {

    private AbsMTGLMagnifierFrameView mMagnifierFrameView;
    private GLSurfaceView mSurfaceView;
    /**
     * 放大镜笔触圆圈与边框LayoutParams
     */
    private RelativeLayout.LayoutParams mMagnifierLayoutParams;

    /**
     * 放大镜宽度比例(默认120dp)
     */
    private float mMagnifierWidthRatio;

    /**
     * 放大镜边缘距离顶部比例(默认10dp)
     */
    private float mMagnifierPaddingTopRatio;

    /**
     * 放大镜边缘距离左侧比例(默认10dp)
     */
    private float mMagnifierPaddingLeftRatio;

    /**
     * 放大镜边框大小比例(默认2dp)
     */
    private float mMagnifierFrameWidthRatio;

    /**
     * 放大镜笔触圆圈x,y坐标便宜
     */
    private float mMagnifierOffsetX, mMagnifierOffsetY;

    private boolean mHasInitMagnifierPosition;
    private boolean mIsMagnifierLeft;
    private AbsBasicRenderTool mRender;


    /**
     * 放大镜顶点数组
     */
    private float[] mMagnifierVertexArray = new float[12];

    /**
     * 放大镜纹理数组
     */
    private float[] mMagnifierTextureArray = new float[12];

    /**
     * 放大镜shader数据
     */
    private float[] mMagnifierData = new float[24];


    public MTGLMagnifierListener(AbsMTGLMagnifierFrameView magnifierFrameView, GLSurfaceView surfaceView) {
        mMagnifierFrameView = magnifierFrameView;
        mSurfaceView = surfaceView;
        init();
    }

    private void init() {
        mMagnifierFrameView.setFrameStrokeWidth(DeviceUtil.dp2px(2));
        setMagnifierData(DeviceUtil.getScreenWidth() / 3, 0, 45, 2);
        setContentPaint();
        setBorderPaint();
        setMagnifierFramePaint();
    }

    /**
     * 设置涂抹笔触大小，放大镜笔触大小
     *
     * @param px 笔触大小，单位px
     */
    public void setPenSize(float px) {
        if (mMagnifierFrameView != null) {
            mMagnifierFrameView.setPenSize(px);
        }
    }

    /**
     * 设置笔触圆圈内容画笔
     */
    public void setContentPaint() {
        Paint paintContent = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintContent.setStyle(Paint.Style.FILL);
        paintContent.setColor(0x4c080808);
        paintContent.setAntiAlias(true);
        if (mMagnifierFrameView != null) {
            mMagnifierFrameView.setContentPaint(paintContent);
        }
    }

    /**
     * 设置画笔边缘
     */
    public void setBorderPaint() {
        Paint paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setColor(0xffa9a9a9);
        paintBorder.setAntiAlias(true);
        //paintBorder.setStrokeWidth(2 * MyData.nDensity);
        if (mMagnifierFrameView != null) {
            mMagnifierFrameView.setBorderPaint(paintBorder);
        }
    }

    /**
     * 设置放大镜边框画笔
     */
    public void setMagnifierFramePaint() {
        Paint windowPaintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        windowPaintBorder.setStyle(Paint.Style.STROKE);
        windowPaintBorder.setColor(0xffffffff);
        windowPaintBorder.setAntiAlias(true);
        //windowPaintBorder.setStrokeWidth(2 * MyData.nDensity);

        if (mMagnifierFrameView != null) {
            mMagnifierFrameView.setFramePaint(windowPaintBorder);
        }
    }


    /**
     * 检查放大镜位置，并设置放大镜边框控件位置
     * 注意：因为设置控件位置，所以需要在UI主线程中执行
     *
     * @param x
     * @param y
     */
    private void checkMagnifierPosition(float x, float y) {
        if (mMagnifierFrameView == null) {
            return;
        }

        if (mMagnifierLayoutParams == null) {
            mMagnifierLayoutParams = (RelativeLayout.LayoutParams) mMagnifierFrameView.getLayoutParams();
        }

        if (!mHasInitMagnifierPosition) {
            mIsMagnifierLeft = true;
            setMagnifierFramePosition(true);
            mHasInitMagnifierPosition = true;
        }

        int frameViewLeft = mMagnifierLayoutParams.leftMargin;
        int frameViewRight = mMagnifierLayoutParams.width + mMagnifierLayoutParams.leftMargin;
        int frameViewBottom = mMagnifierLayoutParams.height + mMagnifierLayoutParams.topMargin;

        if (mIsMagnifierLeft && x < frameViewRight && y < frameViewBottom) {
            mIsMagnifierLeft = false;
            setMagnifierFramePosition(false);
        } else if (!mIsMagnifierLeft && x > frameViewLeft && y < frameViewBottom) {
            mIsMagnifierLeft = true;
            setMagnifierFramePosition(true);
        }
    }


    /**
     * 设置放大镜边框控件位置
     *
     * @param isLeft
     */
    private void setMagnifierFramePosition(boolean isLeft) {
        if (mMagnifierFrameView == null) {
            return;
        }
        int screenWidth = DeviceUtil.getScreenWidth();
        if (mMagnifierLayoutParams == null) {
            mMagnifierLayoutParams = (RelativeLayout.LayoutParams) mMagnifierFrameView.getLayoutParams();
        }
        int size = (int) (mMagnifierWidthRatio * screenWidth);
        int paddingLeft = (int) (mMagnifierPaddingLeftRatio * screenWidth);
        int paddingTop = (int) (mMagnifierPaddingTopRatio * screenWidth);
        if (isLeft) {
            mMagnifierLayoutParams.topMargin = paddingTop;
            mMagnifierLayoutParams.leftMargin = paddingLeft;
        } else {
            mMagnifierLayoutParams.topMargin = paddingTop;
            mMagnifierLayoutParams.leftMargin = screenWidth - size - paddingLeft;
        }
        mMagnifierLayoutParams.width = size;
        mMagnifierLayoutParams.height = size;
        mMagnifierFrameView.setLayoutParams(mMagnifierLayoutParams);
    }

    /**
     * 设置放大镜显示参数，单位px
     *
     * @param width       宽度
     * @param paddingLeft 左边距
     * @param paddingTop  顶边距
     * @param frameWidth  边框宽度
     */
    public void setMagnifierData(float width, float paddingLeft, float paddingTop, float frameWidth) {
        mMagnifierWidthRatio = width / DeviceUtil.getScreenWidth();
        mMagnifierPaddingLeftRatio = paddingLeft / DeviceUtil.getScreenWidth();
        mMagnifierPaddingTopRatio = paddingTop / DeviceUtil.getScreenWidth();
        mMagnifierFrameWidthRatio = frameWidth / DeviceUtil.getScreenWidth();
    }


    public void setMTGLRender(GLSurfaceView.Renderer render) {
        mRender = (AbsBasicRenderTool) render;
    }

    @Override
    public void handleActionPointerDown(MotionEvent event) {
        super.handleActionPointerDown(event);
        mRender.hideMagnifier();
        mMagnifierFrameView.dismiss();
    }

    @Override
    public void handleActionMove(MotionEvent event) {
        super.handleActionMove(event);
        if (event.getPointerCount() < 2) {
            checkMagnifierPosition(event.getX(), event.getY());
            setMagnifierPosition(mIsMagnifierLeft);
            changeMagnifierTextureArray(event.getX(), event.getY());
            showMagnifier();
            mMagnifierFrameView.show(mMagnifierOffsetX, mMagnifierOffsetY);
        }
    }

    @Override
    public void handleActionUp(MotionEvent event) {
        super.handleActionUp(event);
        mRender.hideMagnifier();
        mMagnifierFrameView.dismiss();
        mSurfaceView.requestRender();
    }

    @Override
    public void handleLongPressUp(MotionEvent event) {
        super.handleLongPressUp(event);
        mRender.hideMagnifier();
        mMagnifierFrameView.dismiss();
        mSurfaceView.requestRender();
    }

    @Override
    public void handleLongPressDown(MotionEvent event) {
        super.handleLongPressDown(event);
        checkMagnifierPosition(event.getX(), event.getY());
        setMagnifierPosition(mIsMagnifierLeft);
        changeMagnifierTextureArray(event.getX(), event.getY());
        showMagnifier();
        mMagnifierFrameView.show(mMagnifierOffsetX, mMagnifierOffsetY);
    }

    private void showMagnifier() {
        int index = 0;
        for (int i = 0; i < mMagnifierVertexArray.length; i += 2) {
            mMagnifierData[index++] = mMagnifierVertexArray[i];
            mMagnifierData[index++] = mMagnifierVertexArray[i + 1];
            mMagnifierData[index++] = mMagnifierTextureArray[i];
            mMagnifierData[index++] = mMagnifierTextureArray[i + 1];
        }
        mRender.showMagnifier(mMagnifierData);
        mSurfaceView.requestRender();
    }

    @Override
    public void handlePointerUp(MotionEvent event) {
        super.handlePointerUp(event);
    }


    /**
     * 构造放大镜纹理数组
     *
     * @param x
     * @param y
     */
    private void changeMagnifierTextureArray(float x, float y) {
        float mScale = mRender.getScale();
        int mOutputWidth = mRender.getOutputWidth();
        int mOutputHeight = mRender.getOutputHeight();

        float[] result = mRender.translateToTexCoord(x, y);
        x = result[0];
        y = result[1];


        // 通过x,y坐标构建正方形纹理数组，注意：FBO中纹理需要翻转
        float lengthWidth = getMagnifierTextureWidth();
        float lengthHeight = getMagnifierTextureHeight();
        float xLeft = x - lengthWidth / 2;
        float xRight = x + lengthWidth / 2;
        float yTop = y - lengthHeight / 2;
        float yBottom = y + lengthHeight / 2;

        mMagnifierTextureArray[0] = (xLeft + xRight) / 2;
        mMagnifierTextureArray[1] = (yTop + yBottom) / 2;
        mMagnifierTextureArray[2] = xLeft;
        mMagnifierTextureArray[3] = yBottom;
        mMagnifierTextureArray[4] = xRight;
        mMagnifierTextureArray[5] = yBottom;
        mMagnifierTextureArray[6] = xRight;
        mMagnifierTextureArray[7] = yTop;
        mMagnifierTextureArray[8] = xLeft;
        mMagnifierTextureArray[9] = yTop;
        mMagnifierTextureArray[10] = xLeft;
        mMagnifierTextureArray[11] = yBottom;

        mMagnifierOffsetX = 0f;
        mMagnifierOffsetY = 0f;
        // 检验x轴左侧是否越界
        if (mMagnifierTextureArray[2] < 0.0f) {
            mMagnifierOffsetX = mMagnifierTextureArray[2];
            mMagnifierTextureArray[2] = 0;
            mMagnifierTextureArray[8] = 0;
            mMagnifierTextureArray[10] = 0;
            mMagnifierTextureArray[4] = lengthWidth;
            mMagnifierTextureArray[6] = lengthWidth;
            mMagnifierTextureArray[0] = lengthWidth / 2;

        }
        // 检验x轴右侧是否越界
        if (mMagnifierTextureArray[4] > 1.0f) {
            mMagnifierOffsetX = mMagnifierTextureArray[4] - 1.0f;
            mMagnifierTextureArray[4] = 1.0f;
            mMagnifierTextureArray[6] = 1.0f;

            mMagnifierTextureArray[2] = 1.0f - lengthWidth;
            mMagnifierTextureArray[8] = 1.0f - lengthWidth;
            mMagnifierTextureArray[10] = 1.0f - lengthWidth;
            mMagnifierTextureArray[0] = 1.0f - lengthWidth / 2;

        }
        // 检验y轴下侧是否越界
        if (mMagnifierTextureArray[3] > 1.0f) {
            mMagnifierOffsetY = mMagnifierTextureArray[3] - 1.0f;
            mMagnifierTextureArray[3] = 1.0f;
            mMagnifierTextureArray[5] = 1.0f;
            mMagnifierTextureArray[11] = 1.0f;

            mMagnifierTextureArray[7] = 1.0f - lengthHeight;
            mMagnifierTextureArray[9] = 1.0f - lengthHeight;
            mMagnifierTextureArray[1] = 1.0f - lengthHeight / 2;

        }
        // 检验y轴上侧是否越界
        if (mMagnifierTextureArray[7] < 0.0f) {
            mMagnifierOffsetY = mMagnifierTextureArray[7];
            mMagnifierTextureArray[7] = 0.0f;
            mMagnifierTextureArray[9] = 0.0f;

            mMagnifierTextureArray[3] = lengthHeight;
            mMagnifierTextureArray[5] = lengthHeight;
            mMagnifierTextureArray[11] = lengthHeight;
            mMagnifierTextureArray[1] = lengthHeight / 2;
        }

        float ratioImg = (float) mRender.getImageWidth() / mRender.getImageHeight();
        float ratioOutput = (float) mOutputWidth / mOutputHeight;

        // 当图片放大时，放大镜显示区域的纹理坐标长宽lengthWidth、lengthHeight会变小，所以传给上层绘制的mMagnifierOffsetX需要乘以图片放大系数
        if (ratioImg > ratioOutput) {
            mMagnifierOffsetX = mMagnifierOffsetX * mOutputWidth * mScale;
            mMagnifierOffsetY = mMagnifierOffsetY * mOutputWidth / ratioImg * mScale;
        } else {
            mMagnifierOffsetX = mMagnifierOffsetX * mOutputHeight * ratioImg * mScale;
            mMagnifierOffsetY = mMagnifierOffsetY * mOutputHeight * mScale;
        }
    }

    /**
     * 设置放大镜图片内容位置
     *
     * @param isLeft
     */
    public void setMagnifierPosition(boolean isLeft) {
        int mOutputWidth = mRender.getOutputWidth();
        int mOutputHeight = mRender.getOutputHeight();
        float width = mMagnifierWidthRatio * 2;
        float height = width * mOutputWidth / mOutputHeight;
        float paddingX = mMagnifierPaddingLeftRatio * 2;
        float paddingY = mMagnifierPaddingTopRatio * 2 * mOutputWidth / mOutputHeight;
        float framePaddingX = mMagnifierFrameWidthRatio;
        float framePaddingY = framePaddingX * mOutputWidth / mOutputHeight;
        float xLeft, xRight, yTop, yBottom;
        if (isLeft) {
            xLeft = -1.0f + paddingX + framePaddingX;
            xRight = -1.0f + width + paddingX - framePaddingX;
            yTop = 1.0f - paddingY - framePaddingY;
            yBottom = 1.0f - height - paddingY + framePaddingY;
        } else {
            xLeft = 1.0f - width - paddingX + framePaddingX;
            xRight = 1.0f - paddingX - framePaddingX;
            yTop = 1.0f - paddingY - framePaddingY;
            yBottom = 1.0f - height - paddingY + framePaddingY;

        }
        mMagnifierVertexArray[0] = (xLeft + xRight) / 2;
        mMagnifierVertexArray[1] = (yTop + yBottom) / 2;
        mMagnifierVertexArray[2] = xLeft;
        mMagnifierVertexArray[3] = yBottom;
        mMagnifierVertexArray[4] = xRight;
        mMagnifierVertexArray[5] = yBottom;
        mMagnifierVertexArray[6] = xRight;
        mMagnifierVertexArray[7] = yTop;
        mMagnifierVertexArray[8] = xLeft;
        mMagnifierVertexArray[9] = yTop;
        mMagnifierVertexArray[10] = xLeft;
        mMagnifierVertexArray[11] = yBottom;

    }

    /**
     * 获取放大镜内容纹理宽度
     *
     * @return
     */
    private float getMagnifierTextureWidth() {
        float scaleWidth = mRender.getScaleWidth();
        float scale = mRender.getScale();
        return (mMagnifierWidthRatio - mMagnifierFrameWidthRatio * 2) * scaleWidth / scale;
    }

    /**
     * 获取放大镜内容纹理高度
     *
     * @return
     */
    private float getMagnifierTextureHeight() {
        int width = mRender.getImageWidth();
        int height = mRender.getImageHeight();
        return getMagnifierTextureWidth() * width / height;
    }
}
