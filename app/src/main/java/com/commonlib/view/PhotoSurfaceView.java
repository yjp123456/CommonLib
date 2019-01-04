package com.commonlib.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.commonlib.gl.MTListener;
import com.commonlib.gl.basis.AbsMTGLFilter;
import com.commonlib.gl.basis.AbsMTGLGestureListener;
import com.commonlib.render.PhotoRender;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * SurfaceView
 */
public class PhotoSurfaceView extends GLSurfaceView {
    private PhotoRender mRender;
    private List<AbsMTGLGestureListener> mListeners = null;
    private AbsMTGLGestureListener.LongPressCallback mLongPressCallback;
    private static final int LONG_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();
    private MotionEvent mCurrentDownEvent;
    private static final int LONG_PRESS_UP = 1;
    private static final int LONG_PRESS_DOWN = 2;
    private Handler mHandler;
    private boolean isLongPress;

    private static class GestureHandler extends Handler {
        private WeakReference<PhotoSurfaceView> mWeakReference;

        GestureHandler(PhotoSurfaceView glSurfaceView) {
            mWeakReference = new WeakReference<>(glSurfaceView);
        }

        @Override
        public void handleMessage(Message msg) {
            PhotoSurfaceView glSurfaceView = mWeakReference.get();
            if (glSurfaceView == null) {
                return;
            }
            switch (msg.what) {
                case LONG_PRESS_DOWN:
                    glSurfaceView.isLongPress = true;
                    glSurfaceView.dispatchLongPressDown();
                    break;
                case LONG_PRESS_UP:
                    glSurfaceView.isLongPress = false;
                    glSurfaceView.dispatchLongPressUp();
                    break;
                default:
                    throw new RuntimeException("Unknown message " + msg); //never
            }
        }
    }


    public PhotoSurfaceView(Context context) {
        super(context);
        init();
    }

    public PhotoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        //设备GLSurfaceView背景透明第一步
        /*setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);//把SurfaceView置于Activity显示窗口的最顶层*/


        setEGLContextClientVersion(2);
        mRender = new PhotoRender();
        setRenderer(mRender);

        //这种模式下手动调用 requestRender();才会刷新UI
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        mListeners = new ArrayList<>();
        addGestureListener(new MTListener(this));
        mHandler = new GestureHandler(this);

    }

    /**
     * 设置手势操作回调
     */
    public void addGestureListener(AbsMTGLGestureListener listener) {
        listener.setMTGLRender(mRender);
        mListeners.add(listener);
    }

    /**
     * 设置操作类型，比如瘦脸或者眼睛放大
     */
    public void setFilter(AbsMTGLFilter filter) {
        if (mRender != null) {
            mRender.setFilter(filter);
        }
    }


    private void dispatchLongPressDown() {
        for (AbsMTGLGestureListener listener : mListeners) {
            listener.handleLongPressDown(mCurrentDownEvent);
        }
        if (mLongPressCallback != null) {
            mLongPressCallback.onLongPressed();
        }
    }

    private void dispatchLongPressUp() {
        for (AbsMTGLGestureListener listener : mListeners) {
            listener.handleLongPressUp(mCurrentDownEvent);
        }
        if (mLongPressCallback != null) {
            mLongPressCallback.onUp();
        }
    }

    GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        boolean hasAction = false;

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            hasAction = false;
            for (AbsMTGLGestureListener listener : mListeners) {
                if (listener.handleDoubleClick(e)) {
                    hasAction = true;
                }
            }
            return hasAction || super.onDoubleTap(e);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return hasAction || super.onDoubleTapEvent(e);
        }
    });

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        if (event != null) {
            int action = event.getAction() & MotionEvent.ACTION_MASK;
            final float x, y;
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    for (AbsMTGLGestureListener listener : mListeners) {
                        listener.handleActionDown(event);
                    }

                    if (mCurrentDownEvent != null) {
                        mCurrentDownEvent.recycle();
                    }
                    mCurrentDownEvent = MotionEvent.obtain(event);
                    mHandler.removeMessages(LONG_PRESS_DOWN);
                    mHandler.sendEmptyMessageAtTime(LONG_PRESS_DOWN,
                            mCurrentDownEvent.getDownTime() + LONG_PRESS_TIMEOUT);

                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    //该操作不能异步执行，不然可能执行的时候触点只剩一个导致异常
                    for (AbsMTGLGestureListener listener : mListeners) {
                        listener.handleActionPointerDown(event);
                    }
                    mHandler.removeMessages(LONG_PRESS_DOWN);

                    break;
                case MotionEvent.ACTION_MOVE:
                    for (AbsMTGLGestureListener listener : mListeners) {
                        listener.handleActionMove(event);
                    }

                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    for (AbsMTGLGestureListener listener : mListeners) {
                        listener.handlePointerUp(event);
                    }
                case MotionEvent.ACTION_UP:
                    for (AbsMTGLGestureListener listener : mListeners) {
                        listener.handleActionUp(event);
                    }

                    mHandler.removeMessages(LONG_PRESS_DOWN);
                    if (isLongPress) {
                        mHandler.sendEmptyMessage(LONG_PRESS_UP);
                    }

                    break;
            }
            return true;
        }
        return false;
    }

    private float spacing(MotionEvent event) {
        if (event.getPointerCount() >= 2) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) Math.sqrt(x * x + y * y);
        } else {
            return 0;
        }
    }

    /**
     * 对比显示原图
     *
     * @param isOriginal true:展示原图，false:展示效果图
     */
    public void showTexture(boolean isOriginal) {
        if (mRender != null) {
            if (isOriginal) {
                mRender.showTextureSrc();
            } else {
                mRender.showTextureDes();
            }
            requestRender();
        }
    }


    /**
     * 获取结果图片
     */
    public void saveBitmap(final PhotoRender.SaveListener listener) {
        if (mRender != null) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    mRender.getResultTexture(listener);
                }
            });
        }
    }


    /**
     * 设置原始图片
     */
    public void setOriginBitmap(final Bitmap bitmap) {
        mRender.addDrawRun(new Runnable() {
            @Override
            public void run() {
                mRender.setOriginTexture(bitmap);
            }
        });
        requestRender();
    }

    public void setLongPressCallback(AbsMTGLGestureListener.LongPressCallback longPressCallback) {
        mLongPressCallback = longPressCallback;
    }

    /**
     * 设置是否允许手势操作
     */
    public void setOperateEnable(boolean enable) {
        mRender.setOperateEnable(enable);
    }


    public void release() {
        mRender.addDrawRun(new Runnable() {
            @Override
            public void run() {
                mRender.release();
            }
        });
    }


    public void addDrawRun(Runnable runnable) {
        mRender.addDrawRun(runnable);
    }


}

