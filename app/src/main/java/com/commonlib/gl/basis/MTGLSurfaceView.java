package com.commonlib.gl.basis;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * SurfaceView
 */
public class MTGLSurfaceView extends GLSurfaceView {
    private MTGLRender mRender;
    private List<AbsMTGLGestureListener> mListeners = null;
    private AbsMTGLGestureListener.LongPressCallback mLongPressCallback;
    private static final int LONG_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();
    private MotionEvent mCurrentDownEvent;
    private static final int LONG_PRESS_UP = 1;
    private static final int LONG_PRESS_DOWN = 2;
    private Handler mHandler;
    private boolean isLongPress;

    private float startX;
    private float startY;

    private int mMode = UNDEFINED;
    private static final int UNDEFINED = 0;
    private static final int PINCH_ZOOM = 1;
    private static final int MOVE = 2;


    private static class GestureHandler extends Handler {
        private WeakReference<MTGLSurfaceView> mWeakReference;

        GestureHandler(MTGLSurfaceView glSurfaceView) {
            mWeakReference = new WeakReference<>(glSurfaceView);
        }

        @Override
        public void handleMessage(Message msg) {
            MTGLSurfaceView glSurfaceView = mWeakReference.get();
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


    public MTGLSurfaceView(Context context) {
        super(context);
        init();
    }

    public MTGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        //设备GLSurfaceView背景透明第一步
        /*setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);//把SurfaceView置于Activity显示窗口的最顶层*/


        setEGLContextClientVersion(2);
        mRender = new MTGLRender();
        setRenderer(mRender);

        //这种模式下手动调用 requestRender();才会刷新UI
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        mListeners = new ArrayList<>();
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
            mRender.updateFilter(filter);
            requestRender();
        }
    }


    /**
     * 设置渲染回调
     */
    public void setRenderListener(MTGLRender.MTGLRenderListener listener) {
        if (mRender != null) {
            mRender.setMTGLRenderListener(listener);
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
                    startX = event.getX();
                    startY = event.getY();
                    x = (startX / mRender.getOutputWidth()) * 2 - 1;
                    y = -((startY / mRender.getOutputHeight()) * 2 - 1);

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

                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            mRender.handleActionDown(x, y);
                            requestRender();
                        }
                    });
                    mMode = UNDEFINED;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    //该操作不能异步执行，不然可能执行的时候触点只剩一个导致异常
                    for (AbsMTGLGestureListener listener : mListeners) {
                        listener.handleActionPointerDown(event);
                    }
                    mHandler.removeMessages(LONG_PRESS_DOWN);
                    float oldDist = spacing(event);
                    if (oldDist > 10f) {
                        mMode = PINCH_ZOOM;
                    } else {
                        mMode = UNDEFINED;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mMode == PINCH_ZOOM) {
                        //该操作不能异步执行，不然可能执行的时候触点只剩一个导致异常
                        float newDist = spacing(event);
                        if (newDist > 10f) {
                            for (AbsMTGLGestureListener listener : mListeners) {
                                listener.handleActionMove(event);
                            }
                        }
                    } else {
                        for (AbsMTGLGestureListener listener : mListeners) {
                            listener.handleActionMove(event);
                        }

                        //防止action_down时触发这个
                        x = event.getX() - startX;
                        y = event.getY() - startY;

                        float value = (float) Math.sqrt(x * x + y * y);// 计算两点的距离
                        if (value > 10f) {
                            mMode = MOVE;
                            final float currentX = (event.getX() / mRender.getOutputWidth()) * 2 - 1;
                            final float currentY = -((event.getY() / mRender.getOutputHeight()) * 2 - 1);
                            queueEvent(new Runnable() {
                                @Override
                                public void run() {
                                    mRender.handleActionMove(currentX, currentY);
                                    requestRender();
                                }
                            });
                        }

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

                    if (mMode != PINCH_ZOOM) {
                        x = (event.getX() / mRender.getOutputWidth()) * 2 - 1;
                        y = -((event.getY() / mRender.getOutputHeight()) * 2 - 1);
                        queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                if (mMode == MOVE) {
                                    mRender.handleActionUp(x, y, true);
                                } else {
                                    mRender.handleActionUp(x, y, false);
                                }
                                requestRender();
                            }
                        });
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
     * 撤销动作
     */
    public void undoAction() {
        undoAction(true);
    }

    public void undoAction(final boolean isNeedRequestRender) {
        if (mRender != null) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    mRender.undoAction();
                    if (isNeedRequestRender) {
                        requestRender();
                    }
                }
            });
        }
    }

    public float getCurrentBitmapWidth() {
        if (mRender != null) {
            return mRender.getCurrentBitmapWidth();
        }

        return 1;
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
//
//    /**
//     * 应用效果，外部进行效果操作后需要调用此方法才会处理效果，
//     * 且才会认为是一步有效的操作，存入效果队列
//     */
//    public void apply() {
//        if (mRender != null) {
//            queueEvent(() -> {
//                mRender.apply();
//                requestRender();
//            });
//        }
//    }

    /**
     * 获取结果图片
     */
    public void saveBitmap(final MTGLRender.SaveListener listener) {
        if (mRender != null) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    mRender.getResultTexture(listener);
                }
            });
        }
    }

    public void processBitmap(final boolean isAutoAction, @NonNull final MTGLRender.ProcessListener listener) {
        if (mRender != null) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    mRender.processTexture(isAutoAction, listener);
                    requestRender();
                }
            });
        }
    }

    /**
     * 设置原始图片
     */
    public void setOriginBitmap(Bitmap bitmap) {
        mRender.setOriginTexture(bitmap);
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

//    public int getTextureIn() {
//        return mRender.getTextureIn();
//    }

    public void releaseAutoTexture() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mRender.releaseAutoTexture();
            }
        });

    }

//    public int getFboIn() {
//        return mRender.getFboIn();
//    }
//
//    public int getFboOut() {
//        return mRender.getFboOut();
//    }
//
//    public int getTextureOut() {
//        return mRender.getTextureOut();
//    }

    public void addDrawRun(Runnable runnable) {
        mRender.addDrawRun(runnable);
    }

//    /**
//     * 自动瘦脸开始时需要先复制过程纹理到输出纹理，后面只需要绘制特定某张脸就行了
//     */
//    public void copyTextureIn() {
//        mRender.copyTextureIn();
//
//    }
}
