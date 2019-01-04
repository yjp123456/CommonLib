package com.commonlib.gl.basis;

import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * Created by jieping on 2018/2/24.
 */

public class MTGLShowGestureListener extends AbsMTGLGestureListener {
    private AbsMTGLShowGestureView mAnimationView;

    public MTGLShowGestureListener(AbsMTGLShowGestureView view) {
        mAnimationView = view;
    }


    @Override
    public void setMTGLRender(GLSurfaceView.Renderer render) {

    }

    @Override
    public void handleActionPointerDown(MotionEvent event) {
        super.handleActionPointerDown(event);
        mAnimationView.isShowDrawPoint = false;
    }

    @Override
    public void handleActionMove(MotionEvent event) {
        super.handleActionMove(event);
        if (event.getPointerCount() < 2) {
            mAnimationView.setCurrentPoint(event.getX(), event.getY());
            mAnimationView.isShowDrawPoint = true;
        } else {
            mAnimationView.isShowDrawPoint = false;
        }
        mAnimationView.invalidate();
    }

    @Override
    public void handleActionUp(MotionEvent event) {
        super.handleActionUp(event);
        mAnimationView.isShowDrawPoint = false;
        mAnimationView.invalidate();
    }

    @Override
    public void handleActionDown(MotionEvent event) {
        super.handleActionDown(event);
        mAnimationView.setCurrentPoint(event.getX(), event.getY());
        mAnimationView.isShowDrawPoint = true;
        mAnimationView.invalidate();
    }
}
