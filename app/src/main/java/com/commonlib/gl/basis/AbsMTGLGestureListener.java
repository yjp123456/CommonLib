package com.commonlib.gl.basis;

import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * GL手势操作抽象类
 */
public abstract class AbsMTGLGestureListener {

    public abstract void setMTGLRender(GLSurfaceView.Renderer render);

    public interface LongPressCallback {
        void onLongPressed();

        void onUp();
    }


    public void handleActionPointerDown(MotionEvent event) {

    }

    public void handleActionMove(MotionEvent event) {

    }

    public void handleActionUp(MotionEvent event) {

    }

    public void handleActionDown(MotionEvent event) {

    }

    public void handlePointerUp(MotionEvent event) {

    }

    public void handleLongPressDown(MotionEvent event) {

    }

    public void handleLongPressUp(MotionEvent event) {

    }

    public void setAutoScaleMaxValue(float value) {

    }

    public boolean handleDoubleClick(MotionEvent event) {
        return false;
    }
}
