package com.commonlib.gl.basis;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.support.annotation.NonNull;

import com.commonlib.gl.util.MTGLUtil;

import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_RGBA;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.setIdentityM;


public class MTGLRender implements GLSurfaceView.Renderer {
    private static final String TAG = "MTGLRender";

    /**
     * 保存的操作列表
     */
    private Deque<Integer> mActions = new LinkedBlockingDeque<>();

//    private Deque<Integer> mActionBackup;

    private AbsMTGLFilter mFilter;


    private float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];

    /**
     * 原图纹理
     */
    private int mTextureSrc = MTGLUtil.NO_TEXTURE;

    /**
     * 过程纹理
     */
    private int mTextureDes = MTGLUtil.NO_TEXTURE;

//    /**
//     * 原图纹理
//     */
//    private int mTextureSrc = MTGLUtil.NO_TEXTURE;

//    private int mTextureOut = MTGLUtil.NO_TEXTURE;
//
//    private int mTextureIn = MTGLUtil.NO_TEXTURE;

    /**
     * 原图真实大小
     */
    private int mSrcWidth = 0;
    private int mSrcHeight = 0;

    /**
     * 原图初始化展示大小
     */
    private int mInitWidth = 0;

    /**
     * 输出屏幕大小
     */
    private int mWindowWidth = 0;
    private int mWindowHeight = 0;

    /**
     * 原图缩放比例
     */
    private float mScaleWidth = 1.0f;
    private float mScaleHeight = 1.0f;


    private int mFramebufferID = MTGLUtil.NO_FRAMEBUFFER;
    //    private int mOutFramebufferID = MTGLUtil.NO_FRAMEBUFFER;
    private float[] mAdjustCube;

    /**
     * 图片手势操作后边界的左下角位置
     */
    private float[] mTextureLeftBtm;

    /**
     * 图片手势操作后边界的右上角位置
     */
    private float[] mTextureRightTop;


//    /**
//     * 操作列表里是否包含原图纹理
//     */
//    private boolean isTextureSrcInclude = true;

    /**
     * 是否需要初始化Filter
     */
    private boolean isNeedInitFilter = true;


    /**
     * 原始Bitmap，加载后会释放
     */
    private Bitmap mOriginBitmap;

    boolean mHasLoadBitmap = false;

    private MTGLRenderListener mRenderListener;

    /**
     * 用于显示放大镜
     */
    private boolean isShowMagnifier;
    /**
     * 放大镜展示的区域
     */
    private float[] mVertexData;

    /**
     * 是否可以手势操作
     */
    private boolean mIsOperateEnable;

    private final List<Runnable> mRunOnDraw = new LinkedList<>();

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        //设置GLSurfaceView背景透明第二步
        glClearColor(0.17f, 0.18f, 0.19f, 0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        mWindowWidth = width;
        mWindowHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        glClear(GL_COLOR_BUFFER_BIT);


        if (isNeedInitFilter) {
            isNeedInitFilter = false;
            initFilter();
        }

        if (mOriginBitmap != null) {
            setIdentityM(modelMatrix, 0);
            adjustImageScale();
            glViewport(0, 0, mWindowWidth, mWindowHeight);
            mTextureSrc = mTextureDes = MTGLUtil.loadTexture(mOriginBitmap, false);
            mActions.add(mTextureDes);
            mOriginBitmap = null;
        }

        onRunDraw();

        if (mHasLoadBitmap && mFilter != null) {
            glViewport(0, 0, mWindowWidth, mWindowHeight);
            mFilter.updateVertexData(mScaleWidth, mScaleHeight);
            mFilter.draw(modelMatrix, mTextureDes);
            if (isShowMagnifier) {//这个要放在后面画，不然会被前面的覆盖
                setIdentityM(viewMatrix, 0);
                mFilter.setVertexData(mVertexData);
                mFilter.draw(viewMatrix, mTextureDes);
            }

        }
    }

    /**
     * 根据屏幕大小调整图片大小
     */
    private void adjustImageScale() {
        float ratio1 = (float) mWindowWidth / mSrcWidth;
        float ratio2 = (float) mWindowHeight / mSrcHeight;
        // float ratioMax = Math.max(ratio1, ratio2);
        float ratioMin = Math.min(ratio1, ratio2);
        mInitWidth = Math.round(mSrcWidth * ratioMin);
        int mInitHeight = Math.round(mSrcHeight * ratioMin);

        mScaleWidth = (float) mInitWidth / mWindowWidth;
        mScaleHeight = (float) mInitHeight / mWindowHeight;

        mAdjustCube = new float[]{
                MTGLUtil.VERTEX[0] * mScaleWidth, MTGLUtil.VERTEX[1] * mScaleHeight,
                MTGLUtil.VERTEX[2] * mScaleWidth, MTGLUtil.VERTEX[3] * mScaleHeight,
                MTGLUtil.VERTEX[4] * mScaleWidth, MTGLUtil.VERTEX[5] * mScaleHeight,
                MTGLUtil.VERTEX[6] * mScaleWidth, MTGLUtil.VERTEX[7] * mScaleHeight,
        };
        mTextureLeftBtm = new float[]{mAdjustCube[0], mAdjustCube[1], 0, 1};
        mTextureRightTop = new float[]{mAdjustCube[6], mAdjustCube[7], 0, 1};

    }

    int getImageWidth() {
        return mSrcWidth;
    }

    int getImageHeight() {
        return mSrcHeight;
    }

    /**
     * 将屏幕横坐标转换成纹理坐标
     *
     * @return 返回转换后的纹理坐标
     */
    float getTranslateX(float x) {
        x = (x / mWindowWidth) * 2 - 1;
        float textureWidth = mTextureRightTop[0] - mTextureLeftBtm[0];
        x = (x - mTextureLeftBtm[0]) / textureWidth;
        return x;
    }

    /**
     * 将屏幕纵坐标转换成纹理坐标
     *
     * @return 返回转换后的纹理坐标
     */
    float getTranslateY(float y) {
        y = -((y / mWindowHeight) * 2 - 1);
        float textureHeight = mTextureRightTop[1] - mTextureLeftBtm[1];
        y = (mTextureRightTop[1] - y) / textureHeight;
        return y;
    }

    public float getCurrentBitmapWidth() {
        return modelMatrix[0] * mInitWidth;
    }

    public float getScale() {
        return modelMatrix[0];
    }

    /**
     * 控制是否监听手势操作，自动化时设置为false
     */
    public void setOperateEnable(boolean enable) {
        mIsOperateEnable = enable;
    }

//    int getTextureIn() {
//        return mTextureIn;
//    }
//
//    int getTextureOut() {
//        return mTextureOut;
//    }
//
//    int getFboIn() {
//        return mFramebufferID;
//    }
//
//    int getFboOut() {
//        return mOutFramebufferID;
//    }

    private void onRunDraw() {
        synchronized (this) {
            while (!mRunOnDraw.isEmpty()) {
                mRunOnDraw.remove(0).run();
            }

        }
    }

    void addDrawRun(Runnable runnable) {
        synchronized (this) {
            mRunOnDraw.add(runnable);
        }
    }

//    void copyTextureIn() {
//        if (mTextureOut == MTGLUtil.NO_TEXTURE) {
//            mTextureOut = MTGLUtil.loadTexture(mSrcWidth, mSrcHeight);
//        }
//
//        if (mTextureIn == MTGLUtil.NO_TEXTURE) {
//            mTextureIn = MTGLUtil.loadTexture(mSrcWidth, mSrcHeight);
//        }
//
//        if (mFramebufferID == MTGLUtil.NO_FRAMEBUFFER) {
//            int[] framebuffer = new int[1];
//            GLES20.glGenFramebuffers(1, framebuffer, 0);
//            mFramebufferID = framebuffer[0];
//        }
//        if (mOutFramebufferID == MTGLUtil.NO_FRAMEBUFFER) {
//            int[] framebuffer = new int[1];
//            GLES20.glGenFramebuffers(1, framebuffer, 0);
//            mOutFramebufferID = framebuffer[0];
//        }
//
//        copyTexture(mActions.getLast(), mTextureIn, mSrcWidth, mSrcHeight);
//        copyTexture(mActions.getLast(), mTextureOut, mSrcWidth, mSrcHeight);
//
//        mTextureDes = mTextureOut;
//
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebufferID);
//        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mTextureIn, 0);
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mOutFramebufferID);
//        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mTextureOut, 0);
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
//    }


    public interface MTGLRenderListener {
        /**
         * 更新队列状态到外部
         *
         * @param isCancelable 是否可撤销
         * @param isComparable 是否可对比
         */
        void updateState(boolean isCancelable, boolean isComparable);
    }

    void setMTGLRenderListener(MTGLRenderListener listener) {
        mRenderListener = listener;
    }

//    MTGLRender() {
//        mActions = new LinkedBlockingDeque<>();
//        mActionBackup = new LinkedBlockingDeque<>();
//        mRunOnDraw = new LinkedList<>();
//    }

    void updateFilter(AbsMTGLFilter filter) {
        isNeedInitFilter = true;
        mFilter = filter;
    }


    private void initFilter() {
        //所有gl操作必须放在onDraFrame里面
        mFilter.init();

        setIdentityM(modelMatrix, 0);
    }

    /**
     * 最后一步操作是否是自动操作
     */
    private boolean mLastIsAutoAction;

    /**
     * 撤销回上一步
     */
    void undoAction() {
        if (mActions.size() > 1) {
            int currentTexture = mActions.pollLast();
            if (currentTexture != mTextureSrc) {
                deleteTexture(currentTexture);
            }
            mTextureDes = mActions.getLast();
        }

        if (mRenderListener != null) {
            mRenderListener.updateState(isCancelEnable(), isCompareEnable());
        }
    }

    /**
     * 添加一步操作
     *
     * @param bitmap 纹理图
     */
    private void addAction(Bitmap bitmap) {
        mTextureDes = MTGLUtil.loadTexture(bitmap, false);
        if (mActions.size() > mFilter.getActionStep()) {
            int firstTexture = mActions.pollFirst();
            if (firstTexture != mTextureSrc) {
                deleteTexture(firstTexture);
            }
        }
        mActions.addLast(mTextureDes);

        if (mRenderListener != null) {
            mRenderListener.updateState(isCancelEnable(), isCompareEnable());
        }
    }

    void showTextureSrc() {
        mTextureDes = mTextureSrc;
    }

    void showTextureDes() {
        mTextureDes = mActions.size() > 0 ? mActions.getLast() : MTGLUtil.NO_TEXTURE;
    }

    /**
     * 是否可以执行撤销操作
     */
    private boolean isCancelEnable() {
        return mActions.size() > 1;
    }

    /**
     * 是否可以执行对比操作
     */
    private boolean isCompareEnable() {
        return mActions.size() > 1 || (mActions.size() == 1 && mActions.getFirst() != mTextureSrc);

    }

    /**
     * 删除指定纹理
     *
     * @param deleteTexture 纹理列表
     */
    private void deleteTexture(int... deleteTexture) {
        glDeleteTextures(deleteTexture.length, deleteTexture, 0);
    }

//    /**
//     * 添加一步操作
//     */
//    private void addAction() {
//        if (mActionBackup.size() > 0) {
//            mActions.add(mActionBackup.pollFirst());
//        } else if (mActions.size() + mActionBackup.size() < mFilter.getActionMaxStep() + 1) {
//            mActions.add(MTGLUtil.loadTexture(mNewWidth, mNewHeight));
//        } else {
//            mActions.addLast(mActions.pollFirst());
//            isTextureSrcInclude = false;
//        }
//        int tempTexture = mActions.getLast();
//        if (copyTexture(mTextureDes, tempTexture, mNewWidth, mNewHeight)) {
//            mTextureDes = tempTexture;
//        }
//
//        if (mRenderListener != null) {
//            mRenderListener.updateState(isCancelEnable(), isCompareEnable());
//        }
//
//    }
//
//    /**
//     * 复制mTextureSrc的纹理数据到mTextureDes里面
//     *
//     * @param textureSrc  被复制的纹理
//     * @param mTextureDes 复制的目标对象
//     */
//    private boolean copyTexture(int textureSrc, int mTextureDes, int width, int height) {
//        if (!bindFBO(mTextureDes)) {
//            return false;
//        }
//        mFilter.updateVertexData(1, 1);
//        GLES20.glViewport(0, 0, width, height);
//        updateMatrixForCopyTexture();
//        mFilter.draw(viewMatrix, textureSrc, modelMatrix[0]);
//        unBindFBO();
//        resetParams();
//        return true;
//    }
//
//    /**
//     * 更新矩阵，因为bindFBO后的图片是上下左右颠倒的，所以需要矫正一下
//     */
//    private void updateMatrixForCopyTexture() {
//        setIdentityM(viewMatrix, 0);
//        rotateM(viewMatrix, 0, 180, 0f, 0f, 1f);//将矩阵沿着z轴旋转180度
//        rotateM(viewMatrix, 0, 180, 0f, 1f, 0f);
//    }
//
//
//    /**
//     * 重置图片大小与屏幕比例，将bindFBO里面的数据输出到屏幕
//     */
//    private void resetParams() {
//        mFilter.resetParams();
//        mFilter.updateVertexData(mScaleWidth, mScaleHeight);
//        glViewport(0, 0, mWindowWidth, mWindowHeight);
//    }

    /**
     * 设置初始纹理
     */
    void setOriginTexture(Bitmap bitmap) {
        mSrcWidth = bitmap.getWidth();
        mSrcHeight = bitmap.getHeight();
        mOriginBitmap = bitmap;
        mHasLoadBitmap = true;
    }

    /**
     * 处理手势中手指滑动操作
     */
    void handleActionMove(float endX, float endY) {
        if (mFilter != null && mIsOperateEnable && mTextureRightTop != null) {
            float textureWidth = mTextureRightTop[0] - mTextureLeftBtm[0];
            float textureHeight = mTextureRightTop[1] - mTextureLeftBtm[1];
            endX = (endX - mTextureLeftBtm[0]) / textureWidth;
            endY = (mTextureRightTop[1] - endY) / textureHeight;
            mFilter.handleActionMove(endX, endY);
        }
    }


    /**
     * 处理手势操作中按下操作
     */
    void handleActionDown(float x, float y) {
        if (mFilter != null && mIsOperateEnable && mTextureRightTop != null) {
            float textureWidth = mTextureRightTop[0] - mTextureLeftBtm[0];
            float textureHeight = mTextureRightTop[1] - mTextureLeftBtm[1];
            x = (x - mTextureLeftBtm[0]) / textureWidth;
            y = (mTextureRightTop[1] - y) / textureHeight;
            mFilter.handleActionDown(x, y);
        }
    }

    /**
     * 处理手势操作中放开操作
     */
    void handleActionUp(float x, float y, boolean isMove) {
        if (mFilter != null && mIsOperateEnable && mTextureRightTop != null) {
            float textureWidth = mTextureRightTop[0] - mTextureLeftBtm[0];
            float textureHeight = mTextureRightTop[1] - mTextureLeftBtm[1];
            x = (x - mTextureLeftBtm[0]) / textureWidth;
            y = (mTextureRightTop[1] - y) / textureHeight;
            mFilter.handleActionUp(x, y, isMove);
        }
    }

//    void apply() {
//        //TODO
//        addAction();
//    }

    /**
     * 更新手势缩放后的矩阵
     */
    void handleChangeMatrix(float[] matrix) {
        // 添加代码容错，防止图片没有加载完有手势操作的行为
        if (mAdjustCube == null) {
            return;
        }
        modelMatrix = matrix;
        float[] leftBtm = {mAdjustCube[0], mAdjustCube[1], 0, 1};
        float[] rightTop = {mAdjustCube[6], mAdjustCube[7], 0, 1};
        Matrix.multiplyMV(mTextureLeftBtm, 0, modelMatrix, 0, leftBtm, 0);
        Matrix.multiplyMV(mTextureRightTop, 0, modelMatrix, 0, rightTop, 0);
    }

    float[] getAdjustCube() {
        return mAdjustCube;
    }

    int getOutputWidth() {
        return mWindowWidth;
    }

    int getOutputHeight() {
        return mWindowHeight;
    }


    public float getScaleWidth() {
        return mScaleWidth;
    }

    public float getScaleHeight() {
        return mScaleHeight;
    }

    void releaseAutoTexture() {
//        if (mOutFramebufferID != MTGLUtil.NO_FRAMEBUFFER) {
//            GLES20.glDeleteFramebuffers(1, new int[]{mOutFramebufferID}, 0);
//            mOutFramebufferID = MTGLUtil.NO_FRAMEBUFFER;
//        }
//        glDeleteTextures(2, new int[]{mTextureIn, mTextureOut}, 0);
//        mTextureIn = MTGLUtil.NO_TEXTURE;
//        mTextureOut = MTGLUtil.NO_TEXTURE;
    }

//    private void deleteFramebuffer() {
//        if (mFramebufferID != MTGLUtil.NO_FRAMEBUFFER) {
//            GLES20.glDeleteFramebuffers(1, new int[]{mFramebufferID}, 0);
//            mFramebufferID = MTGLUtil.NO_FRAMEBUFFER;
//        }
//
//    }

    public interface ProcessListener {
        Bitmap onProcess(Bitmap bitmap);

        void onFinished();
    }

    void processTexture(boolean isAutoAction, @NonNull ProcessListener processListener) {
        int tempTexture = MTGLUtil.NO_TEXTURE;
        // 如果最后一步是自动操作，并且当前操作也是自动操作，这个纹理是临时的，直接去掉
        if (mActions.size() > 1 && isAutoAction && mLastIsAutoAction) {
            tempTexture = mActions.pollLast();
        }

        if (mActions.size() > 0) {
            mTextureDes = mActions.getLast();
            if (bindFBO(mTextureDes)) {
                ByteBuffer mBuffer = ByteBuffer.allocate(mSrcWidth * mSrcHeight * 4);
                GLES20.glReadPixels(0, 0, mSrcWidth, mSrcHeight, GL_RGBA,
                        GLES20.GL_UNSIGNED_BYTE, mBuffer);
                unBindFBO();

                ByteBuffer bitmapData = ByteBuffer.wrap(mBuffer.array());
                Bitmap bitmap = Bitmap.createBitmap(mSrcWidth, mSrcHeight, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(bitmapData);
                Bitmap result = processListener.onProcess(bitmap);
                if (result != null) {
                    if (tempTexture != MTGLUtil.NO_TEXTURE) {
                        deleteTexture(tempTexture);
                    }

                    mLastIsAutoAction = isAutoAction;
                    addAction(result);
                } else if (tempTexture != MTGLUtil.NO_TEXTURE) {
                    mActions.addLast(tempTexture);
                    mTextureDes = tempTexture;
                }
                bitmapData.clear();
            } else {
                MTGLUtil.d(TAG, "get result bitmap fail");
                processListener.onFinished();
                return;
            }
        } else {
            mTextureDes = MTGLUtil.NO_TEXTURE;
        }

        processListener.onFinished();
    }

    public interface SaveListener {
        void onSuccess(Bitmap bitmap);
    }

    /**
     * 获取结果纹理
     */
    void getResultTexture(@NonNull SaveListener listener) {
        if (mTextureDes == mTextureSrc) {
            listener.onSuccess(null);
            return;
        }

        if (bindFBO(mTextureDes)) {
            ByteBuffer mBuffer = ByteBuffer.allocate(mSrcWidth * mSrcHeight * 4);
            GLES20.glReadPixels(0, 0, mSrcWidth, mSrcHeight, GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, mBuffer);
            unBindFBO();

            ByteBuffer bitmapData = ByteBuffer.wrap(mBuffer.array());
            Bitmap bitmap = Bitmap.createBitmap(mSrcWidth, mSrcHeight, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(bitmapData);
            listener.onSuccess(bitmap);
            bitmapData.clear();
        } else {
            MTGLUtil.d(TAG, "get result bitmap fail");
        }
    }


    void showMagnifier(float[] data) {
        mVertexData = data;
        isShowMagnifier = true;
    }

    void hideMagnifier() {
        isShowMagnifier = false;
    }

    private boolean bindFBO(int texture) {
        if (mFramebufferID == MTGLUtil.NO_FRAMEBUFFER) {
            int[] framebuffer = new int[1];
            GLES20.glGenFramebuffers(1, framebuffer, 0);
            mFramebufferID = framebuffer[0];
        }

        if (texture == MTGLUtil.NO_TEXTURE) {
            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);
            mTextureDes = textures[0];
            if (mTextureDes == MTGLUtil.NO_TEXTURE) {
                return false;
            }
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebufferID);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texture, 0);

        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            MTGLUtil.d(TAG, "frame buffer bind error:" + GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER));
            unBindFBO();
            return false;
        }
        return true;
    }

    private void unBindFBO() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }
}
