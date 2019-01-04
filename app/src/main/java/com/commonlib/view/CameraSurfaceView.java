package com.commonlib.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.commonlib.gl.basis.AbsMTGLFilter;
import com.commonlib.render.CameraRender;

/**
 * 相机预览图
 */

public class CameraSurfaceView extends GLSurfaceView {
    private CameraRender mRender;


    public CameraSurfaceView(Context context) {
        super(context);
        init();
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        mRender = new CameraRender(this);
        setRenderer(mRender);

        //这种模式下手动调用 requestRender();才会刷新UI
        setRenderMode(RENDERMODE_WHEN_DIRTY);

    }


    public void setSurfaceListener(CameraRender.OnSurfaceListener listener) {
        mRender.setListener(listener);
    }

    public void addRunOnDraw(Runnable runnable) {
        mRender.runOnDraw(runnable);
    }

    public SurfaceTexture getSurface() {
        return mRender.getSurface();
    }

    public void switchCamera(boolean isFrontCamera) {
        mRender.switchCamera(isFrontCamera);
    }

    public int getTextureDes() {
        return mRender.getTextureDes();
    }

    public void setFilter(final AbsMTGLFilter filter) {
        mRender.runOnDraw(new Runnable() {
            @Override
            public void run() {
                mRender.setFilter(filter);
            }
        });
    }

    public void release() {
        mRender.runOnDraw(new Runnable() {
            @Override
            public void run() {
                mRender.release();
            }
        });
        requestRender();
    }
}