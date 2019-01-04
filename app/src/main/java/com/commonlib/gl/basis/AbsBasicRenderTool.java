package com.commonlib.gl.basis;

import android.graphics.Bitmap;


public abstract class AbsBasicRenderTool {
    private static final String TAG = "AbsBasicRenderTool";


    public abstract int getImageWidth();

    public abstract int getImageHeight();


    public abstract float getScale();


    public abstract int getOutputWidth();

    public abstract int getOutputHeight();


    public abstract float getScaleWidth();

    public abstract float getScaleHeight();

    public abstract void showMagnifier(float[] data);

    public abstract void hideMagnifier();

    /**
     * 将屏幕横坐标转换成纹理坐标
     *
     * @return 返回转换后的纹理坐标
     */
    public abstract float[] translateToTexCoord(float x, float y);

    public abstract void handleChangeMatrix(float[] handleChangeMatrix);

    public abstract float[] getAdjustCube();

    public interface SaveListener {
        void onSuccess(Bitmap bitmap);
    }

    public interface MTGLRenderListener {
        /**
         * 更新队列状态到外部
         *
         * @param isCancelable 是否可撤销
         * @param isComparable 是否可对比
         */
        void updateState(boolean isCancelable, boolean isComparable);
    }
}
