package com.commonlib.render;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;

import com.commonlib.gl.basis.AbsBasicRenderTool;
import com.commonlib.gl.basis.AbsMTGLFilter;
import com.commonlib.gl.basis.CommonFilter;
import com.commonlib.gl.util.MTGLUtil;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_RGBA;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.invertM;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;

/**
 * Created by jieping on 2018/11/11.
 */

public class PhotoRender extends AbsBasicRenderTool implements GLSurfaceView.Renderer {
    private static final String TAG = "PhotoRender";

    private AbsMTGLFilter mFilter;


    private float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] modelInvertMatrix = new float[16];

    /**
     * 原图纹理
     */
    private int mTextureSrc = MTGLUtil.NO_TEXTURE;

    /**
     * 过程纹理
     */
    private int mTextureDes = MTGLUtil.NO_TEXTURE;


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

    private float[] mAdjustCube;

    boolean mHasLoadBitmap = false;


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
    private CommonFilter mCommonFilter;
    private boolean mIsShowOrigin = false;

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        //设置GLSurfaceView背景透明第二步
        glClearColor(0.17f, 0.18f, 0.19f, 0f);

        mCommonFilter = new CommonFilter();
        mCommonFilter.init();

        setIdentityM(modelMatrix, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        mWindowWidth = width;
        mWindowHeight = height;
        glViewport(0, 0, mWindowWidth, mWindowHeight);

    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        glClear(GL_COLOR_BUFFER_BIT);

        onRunDraw();
        if (!mHasLoadBitmap) {
            return;
        }
        if (mFilter != null) {
            copyEffectToTexture(mTextureSrc, mTextureDes, mSrcWidth, mSrcHeight);
            glViewport(0, 0, mWindowWidth, mWindowHeight);
            mCommonFilter.updateVertexData(mScaleWidth, mScaleHeight);
            mCommonFilter.draw(modelMatrix, mTextureDes);
        } else {
            glViewport(0, 0, mWindowWidth, mWindowHeight);
            mCommonFilter.updateVertexData(mScaleWidth, mScaleHeight);
            mCommonFilter.draw(modelMatrix, mTextureSrc);
        }

    }

    private void copyEffectToTexture(int textureSrc, int mTextureDes, int width, int height) {
        if (!bindFBO(mTextureDes)) {
            return;
        }
        mFilter.updateVertexData(1, 1);
        GLES20.glViewport(0, 0, width, height);
        updateMatrixForCopyTexture();
        mFilter.draw(viewMatrix, textureSrc);
        unBindFBO();
    }

    private void updateMatrixForCopyTexture() {
        setIdentityM(viewMatrix, 0);
        rotateM(viewMatrix, 0, 180, 1f, 0f, 0f);//将矩阵沿着x轴旋转180度

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
    }


    /**
     * 控制是否监听手势操作，自动化时设置为false
     */
    public void setOperateEnable(boolean enable) {
        mIsOperateEnable = enable;
    }

    private void onRunDraw() {
        synchronized (this) {
            while (!mRunOnDraw.isEmpty()) {
                mRunOnDraw.remove(0).run();
            }

        }
    }

    public void addDrawRun(Runnable runnable) {
        synchronized (this) {
            mRunOnDraw.add(runnable);
        }
    }


    public void showTextureSrc() {
        mIsShowOrigin = true;
    }

    public void showTextureDes() {
        mIsShowOrigin = false;
    }


    /**
     * 删除指定纹理
     *
     * @param deleteTexture 纹理列表
     */
    private void deleteTexture(int... deleteTexture) {
        glDeleteTextures(deleteTexture.length, deleteTexture, 0);
    }

    /**
     * 设置初始纹理
     */
    public void setOriginTexture(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        mSrcWidth = bitmap.getWidth();
        mSrcHeight = bitmap.getHeight();
        if (mTextureSrc != MTGLUtil.NO_TEXTURE) {
            glDeleteTextures(2, new int[]{mTextureDes, mTextureSrc}, 0);
        }
        mTextureSrc = MTGLUtil.loadTexture(bitmap, true);
        mTextureDes = MTGLUtil.loadTexture(mSrcWidth, mSrcHeight);
        mHasLoadBitmap = true;
        adjustImageScale();
    }


    /**
     * 更新手势缩放后的矩阵
     */
    @Override
    public void handleChangeMatrix(float[] matrix) {
        // 添加代码容错，防止图片没有加载完有手势操作的行为
        if (mAdjustCube == null) {
            return;
        }
        modelMatrix = matrix;
        invertM(modelInvertMatrix, 0, modelMatrix, 0);
    }

    @Override
    public float getScale() {
        return modelMatrix[0];
    }

    @Override
    public int getImageWidth() {
        return mSrcWidth;
    }

    @Override
    public int getImageHeight() {
        return mSrcHeight;
    }

    @Override
    public float[] getAdjustCube() {
        return mAdjustCube;
    }

    @Override
    public int getOutputWidth() {
        return mWindowWidth;
    }

    @Override
    public int getOutputHeight() {
        return mWindowHeight;
    }

    @Override
    public float getScaleWidth() {
        return mScaleWidth;
    }

    @Override
    public float getScaleHeight() {
        return mScaleHeight;
    }

    public void setFilter(AbsMTGLFilter filter) {
        mFilter = filter;
    }


    public interface SaveListener {
        void onSuccess(Bitmap bitmap);
    }

    /**
     * 获取结果纹理
     */
    public void getResultTexture(@NonNull SaveListener listener) {
        if (mTextureDes == mTextureSrc) {
            listener.onSuccess(null);
            return;
        }
        int resultTexture = mFilter == null ? mTextureSrc : mTextureDes;

        if (bindFBO(resultTexture)) {
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


    public void showMagnifier(float[] data) {
        mVertexData = data;
        isShowMagnifier = true;
    }

    public void hideMagnifier() {
        isShowMagnifier = false;
    }

    /**
     * 将屏幕坐标转换成相对纹理的纹理坐标
     */
    @Override
    public float[] translateToTexCoord(float x, float y) {
        x = (x / getOutputWidth()) * 2 - 1;
        y = -((y / getOutputHeight()) * 2 - 1);
        float[] temp = new float[]{x, y, 0.0f, 1.0f};
        multiplyMV(temp, 0, modelInvertMatrix, 0, temp, 0);
        float dx = (temp[0] + mScaleWidth) / (2 * mScaleWidth);
        float dy = (mScaleHeight - temp[1]) / (2 * mScaleHeight);
        return new float[]{dx, dy};
    }

    public void release() {
        if (mFramebufferID != MTGLUtil.NO_FRAMEBUFFER) {
            GLES20.glDeleteFramebuffers(1, new int[]{mFramebufferID}, 0);
            mFramebufferID = MTGLUtil.NO_FRAMEBUFFER;
        }
        glDeleteTextures(2, new int[]{mTextureDes, mTextureSrc}, 0);
        mTextureDes = MTGLUtil.NO_TEXTURE;
        mCommonFilter.deleteProgram();
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
