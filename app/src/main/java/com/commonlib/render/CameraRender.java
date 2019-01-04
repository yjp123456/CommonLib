package com.commonlib.render;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.commonlib.filter.OESFilter;
import com.commonlib.gl.basis.AbsMTGLFilter;
import com.commonlib.gl.basis.CommonFilter;
import com.commonlib.gl.util.MTGLUtil;

import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;

/**
 * 相机gl渲染类
 */

public class CameraRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "CameraRender";
    private final Queue<Runnable> mRunOnDraw = new LinkedList<Runnable>();

    /**
     * 原图的大小
     */
    private int mWidth = 0;
    private int mHeight = 0;

    /**
     * 输出屏幕大小
     */
    private int mOutputWidth = 0;
    private int mOutputHeight = 0;


    /**
     * 原图缩放比例
     */
    private float mScaleWidth = 1.0f;
    private float mScaleHeight = 1.0f;


    private int mFramebufferID = MTGLUtil.NO_FRAMEBUFFER;

    private int mTextureOES = MTGLUtil.NO_TEXTURE;
    private int mTextureDes = MTGLUtil.NO_TEXTURE;

    private OESFilter mOESFilter;
    private CommonFilter mCommonFilter;


    private final float[] viewMatrix = new float[16];


    private OnSurfaceListener mListener;

    /**
     * 接收预览帧数据的载体
     */
    private SurfaceTexture mSurface;

    private GLSurfaceView glSurfaceView;
    private float[] mAdjustCube;
    /**
     * 是否前置摄像头
     */
    private boolean mIsFrontCamera = false;
    /**
     * 滤镜处理类
     */
    private AbsMTGLFilter mFilter;
    private final float[] modelMatrix = new float[16];

    public CameraRender(GLSurfaceView glSurfaceView) {
        this.glSurfaceView = glSurfaceView;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        glClearColor(0.17f, 0.18f, 0.19f, 0f);

        mOESFilter = new OESFilter();
        mOESFilter.init();

        mCommonFilter = new CommonFilter();
        mCommonFilter.init();

        setIdentityM(modelMatrix, 0);
        mTextureOES = MTGLUtil.createOESTextureObject();
        mSurface = new SurfaceTexture(mTextureOES);
        mSurface.setOnFrameAvailableListener(this);
        updateMatrixForCopyTexture();

        if (mListener != null) {
            mListener.surfaceCreated(mSurface);
        }

    }


    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        mOutputWidth = width;
        mOutputHeight = height;
        glDeleteTextures(1, new int[]{mTextureDes}, 0);
        mTextureDes = MTGLUtil.loadTexture(mOutputWidth, mOutputHeight);
        glViewport(0, 0, mOutputWidth, mOutputHeight);
        if (mListener != null) {
            mListener.surfaceChanged(mSurface, width, height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        glClear(GL_COLOR_BUFFER_BIT);
        runAll();

        glViewport(0, 0, mOutputWidth, mOutputHeight);
        if (mFilter != null) {
            mFilter.updateVertexData(1.0f, 1.0f);
            mFilter.draw(modelMatrix, mTextureDes);
        } else {
            mCommonFilter.draw(modelMatrix, mTextureDes);
        }
    }

    public void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.add(runnable);
        }
    }

    private void runAll() {
        synchronized (mRunOnDraw) {
            while (!mRunOnDraw.isEmpty()) {
                mRunOnDraw.poll().run();
            }
        }
    }

    public SurfaceTexture getSurface() {
        return mSurface;
    }

    public void setListener(OnSurfaceListener listener) {
        mListener = listener;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        runOnDraw(() -> {
            mSurface.updateTexImage();
            copyOESToTexture(mTextureOES, mTextureDes, mOutputWidth, mOutputHeight);
        });
        glSurfaceView.requestRender();
    }

    public void release() {
        if (mFramebufferID != MTGLUtil.NO_FRAMEBUFFER) {
            GLES20.glDeleteFramebuffers(1, new int[]{mFramebufferID}, 0);
            mFramebufferID = MTGLUtil.NO_FRAMEBUFFER;
        }
        glDeleteTextures(2, new int[]{mTextureDes, mTextureOES}, 0);
        mTextureDes = MTGLUtil.NO_TEXTURE;
        mTextureOES = MTGLUtil.NO_TEXTURE;
        mCommonFilter.deleteProgram();
        mOESFilter.deleteProgram();
    }

    /**
     * 根据屏幕大小调整图片大小
     */
    private void adjustImageScale() {
        float ratio1 = (float) mOutputWidth / mWidth;
        float ratio2 = (float) mOutputHeight / mHeight;
        // float ratioMax = Math.max(ratio1, ratio2);
        float ratioMin = Math.min(ratio1, ratio2);
        int newWidth = Math.round(mWidth * ratioMin);
        int newHeight = Math.round(mHeight * ratioMin);


        mScaleWidth = (float) newWidth / mOutputWidth;
        mScaleHeight = (float) newHeight / mOutputHeight;

        mAdjustCube = new float[]{
                MTGLUtil.VERTEX[0] * mScaleWidth, MTGLUtil.VERTEX[1] * mScaleHeight,
                MTGLUtil.VERTEX[2] * mScaleWidth, MTGLUtil.VERTEX[3] * mScaleHeight,
                MTGLUtil.VERTEX[4] * mScaleWidth, MTGLUtil.VERTEX[5] * mScaleHeight,
                MTGLUtil.VERTEX[6] * mScaleWidth, MTGLUtil.VERTEX[7] * mScaleHeight,
        };
    }

    /**
     * 更新矩阵，因为bindFBO后的图片是上下颠倒的，所以需要矫正一下
     */
    private void updateMatrixForCopyTexture() {
        setIdentityM(viewMatrix, 0);
        if (!mIsFrontCamera) {
            rotateM(viewMatrix, 0, 180, 1f, 0f, 0f);//将矩阵沿着x轴旋转180度
        }
        rotateM(viewMatrix, 0, -90, 0f, 0f, 1f);//将矩阵沿着z轴旋转90度
    }

    private void copyOESToTexture(int srcTexture, int dstTexture, int width, int height) {
        if (!bindFBO(dstTexture)) {
            return;
        }

        GLES20.glViewport(0, 0, width, height);
        mOESFilter.updateVertexData(1, 1);
        mOESFilter.drawOES(viewMatrix, srcTexture);
        unBindFBO();
    }


    private void unBindFBO() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
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

    public void switchCamera(boolean isFrontCamera) {
        mIsFrontCamera = isFrontCamera;
        updateMatrixForCopyTexture();
    }

    public int getTextureDes() {
        return mTextureDes;
    }

    public void setFilter(AbsMTGLFilter filter) {
        mFilter = filter;
    }


    public interface OnSurfaceListener {
        void surfaceCreated(SurfaceTexture holder);

        void surfaceChanged(SurfaceTexture holder, int width, int height);
    }
}

