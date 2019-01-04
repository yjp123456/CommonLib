package com.commonlib.gl.basis;

import android.animation.ValueAnimator;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.MotionEvent;

import com.commonlib.gl.util.MTGLUtil;


/**
 * GL平移、缩放处理类
 */
public class MTGLMatrixListener extends AbsMTGLGestureListener {
    private static final int DEFAULT_ANIMATION_DURATION_PER_SCALE = 300;
    /**
     * 最大放大系数
     */
    private final float SCALE_MAX = 50.0f;

    /**
     * 缩放到极限值的力度阻碍系数
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final float SCALE_MODULUS = 3 / 4f;

    /**
     * 平移系数
     */
    @SuppressWarnings("unused")
    private final float MOVE_MODULUS = 2 / 3f;

    /**
     * 动画最大时间
     */
    private final int DEFAULT_ANIM_DURATION = 200;

    /**
     * 动画每帧间隔
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final long FRAME_INTERVAL = 1000L / 60;

    /**
     * 动画的帧率（一秒120帧）
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final float ANIM_FRAME_RATE = 60f / 1000;

    private static final int MATRIX_SIZE = 16;


    /**
     * 投影矩阵
     */
    private float[] mProjectionMatrix = new float[MATRIX_SIZE];

    private float oldDist = 1f;
    private PointF midStart = new PointF();
    private PointF mid = new PointF();
    private MTGLSurfaceView mSurfaceView;
    private MTGLRender mRenderer;

    private ValueAnimator mAnimation;

//    /**
//     * 回弹动画x轴目标位置
//     */
//    private float transXC;
//
//    /**
//     * 回弹动画y轴目标位置
//     */
//    private float transYC;
//
//    /**
//     * 回弹动画最终缩放值
//     */
//    private float scaleC;

    /**
     * 控制动画是否中断
     */
    private boolean isPause = false;
//    private float mAutoScaleMaxValue = 1.5f;

    private boolean hasMultiPointer = false;

    @Override
    public void handleActionPointerDown(MotionEvent event) {
        initMid(event);
        hasMultiPointer = true;
    }

    @Override
    public void handleActionMove(MotionEvent event) {
        translateZoom(event);
    }

    @Override
    public void handleActionUp(MotionEvent event) {
        //手指全部放开后才进行回弹动画
        if (hasMultiPointer) {
            touchUpAnim();
            hasMultiPointer = false;
        }
    }

    @Override
    public void handleActionDown(MotionEvent event) {

    }

    @Override
    public void handlePointerUp(MotionEvent event) {

    }

    @Override
    public boolean handleDoubleClick(MotionEvent event) {
        boolean isNeedScale = false;
        AnimModel animModel = new AnimModel();
        if (getScale() < 1.0f || getScale() > 1.0f) {
            animModel.resultScale = 1.0f;
            animModel.resultTransX = 0f;
            animModel.resultTransY = 0f;
            isNeedScale = true;
        }

        if (isNeedScale) {
            mid.set(0, 0);
            animModel.duration = DEFAULT_ANIM_DURATION;
            asyAnim(animModel);
            return true;
        }

        return false;
    }

    @Override
    public void setMTGLRender(GLSurfaceView.Renderer render) {
        if (render != null && render instanceof MTGLRender) {
            mRenderer = (MTGLRender) render;
        }
    }

    public MTGLMatrixListener(MTGLSurfaceView view) {
        if (view != null) {
            mSurfaceView = view;
        }
        Matrix.setIdentityM(mProjectionMatrix, 0);
    }


    /**
     * 改变投影矩阵，实现缩放平移
     */
    public void requestChange() {
        if (mRenderer != null) {
            mRenderer.handleChangeMatrix(getHandleChangeMatrix());
            mSurfaceView.requestRender();
        }
    }

    private float[] getHandleChangeMatrix() {
        return mProjectionMatrix;
    }


    private void translateZoom(final MotionEvent event) {
        // 缩放前记录当前缩放大小
        if (event.getPointerCount() >= 2) {
            float savedScale = getScale();
            float newDist = spacing(event);
            float scale = newDist / oldDist;
            //noinspection StatementWithEmptyBody
            if (getScale() < 1.0f && newDist < oldDist) {
                // 小于原图的缩放时，减小手势的力度
                //scale += (1 - scale) * SCALE_MODULUS;
            } else if (getScale() > SCALE_MAX && newDist > oldDist) {
                // 小于原图的缩放时，减小手势的力度
                scale -= (scale - 1) * SCALE_MODULUS;
            }
            oldDist = newDist;
            // 缩放
            Matrix.scaleM(mProjectionMatrix, 0, scale, scale, 0);

            midPointAndMappingGL(mid, event);
            float transX = mid.x - midStart.x;
            float transY = mid.y - midStart.y;
            midStart.x = mid.x;
            midStart.y = mid.y;
            // 双指平移
            Matrix.translateM(mProjectionMatrix, 0, transX / getScale(), transY / getScale(), 0.f);

            // 缩放时，以双指为中心的平移调整（达到以某点为中心的缩放的效果）
            // 公式:transX = x1 - x2, (x1 - Ori)/Scale1 = x2/Scale2, Ori = getTransX();
            setTransX(mid.x - (mid.x - getTransX()) / savedScale * getScale());
            setTransY(mid.y - (mid.y - getTransY()) / savedScale * getScale());

            requestChange();
        }
    }

    private void initMid(final MotionEvent event) {
        //双指点击时中断之前的动画
        isPause = true;
        midPointAndMappingGL(midStart, event);
        oldDist = spacing(event);
    }

    /**
     * 获取缩放倍数
     */
    private float getScale() {
        return mProjectionMatrix[0];
    }

    /**
     * 获取缩放倍数
     */
    private void setScale(float scale) {
        mProjectionMatrix[0] = scale;
        mProjectionMatrix[5] = scale;
    }

    /**
     * 设置X轴平移距离
     */
    private void setTransX(float transX) {
        mProjectionMatrix[12] = transX;
    }

    /**
     * 获取X轴平移距离
     */
    private float getTransX() {
        return mProjectionMatrix[12];
    }

    /**
     * 设置Y轴平移距离
     */
    private void setTransY(float transY) {
        mProjectionMatrix[13] = transY;
    }

    /**
     * 获取Y轴平移距离
     */
    private float getTransY() {
        return mProjectionMatrix[13];
    }

    /**
     * 手势抬起回弹动画
     */
    private void touchUpAnim() {
        touchUpAnim(DEFAULT_ANIM_DURATION);
    }

    /**
     * 手势抬起回弹动画（包括边界回弹与最大放大系数回弹）
     */
    private void touchUpAnim(int durationMillis) {
        AnimModel animModel = new AnimModel();
        animModel.duration = durationMillis;
        // 放大极限回弹引发的x,y坐标平移
        float scaleLimitTransX = getTransX();
        float scaleLimitTransY = getTransY();

        if (getScale() <= 0.5f) {
            animModel.resultScale = 0.5f;
            animModel.resultTransX = 0.0f;
            animModel.resultTransY = 0.0f;
        } else {
            if (getScale() > SCALE_MAX) {
                animModel.resultScale = SCALE_MAX;
                // 放大极限回弹引发的x,y坐标平移
                scaleLimitTransX = mid.x - (mid.x - getTransX()) / getScale() * SCALE_MAX;
                scaleLimitTransY = mid.y - (mid.y - getTransY()) / getScale() * SCALE_MAX;

                // 放大极限回弹后矩阵
                float[] projectionMatrix = new float[MATRIX_SIZE];
                Matrix.setIdentityM(projectionMatrix, 0);
                projectionMatrix[0] = animModel.resultScale;
                projectionMatrix[5] = animModel.resultScale;
                projectionMatrix[12] = scaleLimitTransX;
                projectionMatrix[13] = scaleLimitTransY;
                outCheck(projectionMatrix); // 判断放大极限回弹是否出现边界超出的情况
            } else {
                animModel.resultScale = getScale();
                outCheck(mProjectionMatrix);
//                if (outCheck(mProjectionMatrix)) { // 缩放系数与平移系数都无需回弹的情况
//                    return;
//                }
            }

            if (isLeftIn() && isRightIn()) {
                // 左右边界都在视口中的情况，需要将X坐标矫正回原点
                animModel.resultTransX = 0.0f;
            } else if (isRightIn()) {
                if (animModel.resultScale * getImgRatioWith() > 1) {
                    // 宽度在放大到scale情况下，超出视口可容纳区域
                    animModel.resultTransX = 1 - animModel.resultScale * getImgRatioWith();
                } else {
                    animModel.resultTransX = 0.0f;
                }
            } else if (isLeftIn()) {
                if (animModel.resultScale * getImgRatioWith() > 1) {
                    animModel.resultTransX = animModel.resultScale * getImgRatioWith() - 1;
                } else {
                    animModel.resultTransX = 0.0f;
                }
            } else {
                animModel.resultTransX = scaleLimitTransX;
            }

            if (isTopIn() && isBtmIn()) {
                // 上下边界都在视口中的情况，需要将Y坐标矫正回原点
                animModel.resultTransY = 0.0f;
            } else if (isBtmIn()) {
                if (animModel.resultScale * getImgRatioHeight() > 1) {
                    // 高度在放大到scale情况下，超出视口可容纳区域
                    animModel.resultTransY = animModel.resultScale * getImgRatioHeight() - 1;
                } else {
                    animModel.resultTransY = 0.0f;
                }
            } else if (isTopIn()) {
                if (animModel.resultScale * getImgRatioHeight() > 1) {
                    animModel.resultTransY = 1 - animModel.resultScale * getImgRatioHeight();
                } else {
                    animModel.resultTransY = 0.0f;
                }
            } else {
                animModel.resultTransY = scaleLimitTransY;
            }

        }

        asyAnim(animModel);
    }

    // 变换后，4个顶点的坐标（x,y,z,w）
    private float[] leftBtmNew = new float[4];
    private float[] rightBtmNew = new float[4];
    private float[] leftTopNew = new float[4];
    private float[] rightTopNew = new float[4];

    /**
     * 边界判断
     *
     * @param projectionMatrix 投影矩阵
     */
    private void outCheck(float[] projectionMatrix) {
//        boolean isLBOut;
//        boolean isRBOut;
//        boolean isLTOut;
//        boolean isRTOut;

        float[] adjustCube = MTGLUtil.VERTEX;
        if (mRenderer != null) {
            adjustCube = mRenderer.getAdjustCube();
            if (adjustCube == null) {
                adjustCube = MTGLUtil.VERTEX;
            }
        }
        float[] leftBtm = {adjustCube[0], adjustCube[1], 0, 1};
        float[] rightBtm = {adjustCube[2], adjustCube[3], 0, 1};
        float[] leftTop = {adjustCube[4], adjustCube[5], 0, 1};
        float[] rightTop = {adjustCube[6], adjustCube[7], 0, 1};

        Matrix.multiplyMV(leftBtmNew, 0, projectionMatrix, 0, leftBtm, 0);
        Matrix.multiplyMV(rightBtmNew, 0, projectionMatrix, 0, rightBtm, 0);
        Matrix.multiplyMV(leftTopNew, 0, projectionMatrix, 0, leftTop, 0);
        Matrix.multiplyMV(rightTopNew, 0, projectionMatrix, 0, rightTop, 0);

//        isLBOut = leftBtmNew[0] < -1f || leftBtmNew[1] < -1f;
//        isRBOut = rightBtmNew[0] > 1f || rightBtmNew[1] < -1f;
//        isLTOut = leftTopNew[0] < -1f || leftTopNew[1] > 1f;
//        isRTOut = rightTopNew[0] > 1f || rightTopNew[1] > 1f;

    }

    // 左边界是否进入视口中
    private boolean isLeftIn() {
        return leftBtmNew[0] > -1f;
    }

    // 底边界是否进入视口中
    private boolean isBtmIn() {
        return leftBtmNew[1] > -1f;
    }

    // 右边界是否进入视口中
    private boolean isRightIn() {
        return rightTopNew[0] < 1f;
    }

    // 上边界是否进入视口中
    private boolean isTopIn() {
        return rightTopNew[1] < 1f;
    }

    public float[] getProjectionMatrix() {
        return mProjectionMatrix;
    }


    /**
     * 动画数据
     */
    private class AnimModel {

        // 缩放平移系数结果值
        float resultScale;
        float resultTransX;
        float resultTransY;

        // 动画时间
        int duration = DEFAULT_ANIM_DURATION;
    }

    /**
     * 通过动画的总耗时，获取动画的帧数
     *
     * @param durationMillis 动画耗时，单位毫秒
     * @return 动画总帧数
     */
    private int getAnimFrames(int durationMillis) {
        return (int) (durationMillis * ANIM_FRAME_RATE);
    }


    /**
     * 异步线程播放动画
     *
     * @param animModel 动画数据
     */
    private void asyAnim(final AnimModel animModel) {
        if (animModel == null || animModel.duration < 0) {
            return;
        }

        // 动画帧数
        final int frames = getAnimFrames(animModel.duration);

        // 每帧动画的改变量
        final float scaleC = (animModel.resultScale - getScale()) / frames;
        final float transXC = (animModel.resultTransX - getTransX()) / frames;
        final float transYC = (animModel.resultTransY - getTransY()) / frames;
        isPause = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < frames; i++) {
                    if (isPause)
                        break;
                    if (scaleC != 0f) {
                        setScale(getScale() + scaleC);
                    }
                    if (transXC != 0f) {
                        setTransX(getTransX() + transXC);
                    }
                    if (transYC != 0f) {
                        setTransY(getTransY() + transYC);
                    }
                    requestChange();
                    try {
                        Thread.sleep(FRAME_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!isPause) {
                    setScale(animModel.resultScale);
                    setTransX(animModel.resultTransX);
                    setTransY(animModel.resultTransY);
                    requestChange();
                }
            }
        }).start();

    }

//    private void doAnimation(float frames) {
//        boolean index = true;
//        final int outputWidth = mRenderer.getOutputWidth();
//        final int outputHeight = mRenderer.getOutputHeight();
//
//        while (index && !isStop) {
//            index = false;
//            //当前位置与目标位置精度小于1像素时继续动画
//            if ((Math.abs(transXC - getTransX()) * outputWidth > 1f
//                    || Math.abs(transYC - getTransY()) * outputHeight > 1f)) {
//                setTransX(getTransX() + (transXC - getTransX()) / frames);
//                setTransY(getTransY() + (transYC - getTransY()) / frames);
//                index = true;
//            }
//
//            //当前缩放值与目标缩放值精度大于0.001时继续动画
//            if (Math.abs(getScale() - scaleC) > 0.001f) {
//                float savedScale = getScale();
//                float scale = (float) Math.sqrt(Math.sqrt(Math.sqrt(scaleC / getScale())));
//                setScale(getScale() * scale);
//                //目标是让图片定点放大，这样看起来会有反弹效果
//                setTransX(mid.x - (mid.x - getTransX()) / savedScale * getScale());
//                setTransY(mid.y - (mid.y - getTransY()) / savedScale * getScale());
//
//                index = true;
//            }
//
//            requestChange();
//            try {
//                Thread.sleep(FRAME_INTERVAL);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }


    private float getImgRatioWith() {
        return mRenderer != null ? 1.0f * mRenderer.getScaleWidth() : 1.0f;
    }

    private float getImgRatioHeight() {
        return mRenderer != null ? 1.0f * mRenderer.getScaleHeight() : 1.0f;
    }

    // 触碰两点间距离
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    // 取手势中心点
    private void midPointAndMappingGL(PointF point, MotionEvent event) {
        float x = (event.getX(0) + event.getX(1)) / 2;
        float y = (event.getY(0) + event.getY(1)) / 2;
        point.set(xMappingGL(x), yMappingGL(y));
    }

    // 映射X轴到GL坐标
    private float xMappingGL(float x) {
        return mRenderer != null ? (x / mRenderer.getOutputWidth() * 2 - 1) : 1.0f;
    }

    // 映射Y轴到GL坐标
    private float yMappingGL(float y) {
        return mRenderer != null ? (1 - y / mRenderer.getOutputHeight() * 2) : 1.0f;
    }
}
