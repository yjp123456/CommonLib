package com.commonlib.gl.util;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by czp on 2018/10/30.
 *
 * 离屏渲染器
 */

public class MTGLOffscreenRenderer {

    // reference of self
    private List<Runnable> mRunnable = new LinkedList<Runnable>();

    // reference of self
    private Object mObject = new Object();

    // opengl thread
    private GLThread mGLThread;

    //两个弱引用
    private final WeakReference<List<Runnable>> mRunnableWeakRef = new WeakReference<List<Runnable>>(mRunnable);

    private final WeakReference<Object> mObjectWeakRef = new WeakReference<Object>(mObject);

    public MTGLOffscreenRenderer(){
        beginGLThread();
    }

    /**
     * 开启GL线程
     */
    private void beginGLThread() {
        checkRenderThreadState();
        mGLThread = new GLThread(mRunnableWeakRef, mObjectWeakRef);
        mGLThread.start();
    }

    /**
     * 添加渲染队列
     */
    public void addDrawRun(Runnable runnable) {
        synchronized (mObject) {
            mRunnable.add(runnable);
        }
    }

    /**
     * 通知OpenGL需要刷新一次
     */
    public void requestRender() {
        mGLThread.requestRender();
    }

    /**
     * finish the openGL thread
     */
    public void releaseGL(Runnable runnable) {
        if (mGLThread != null) {
            mGLThread.stopGL(runnable);
        }
    }

    /**
     * GL thread
     */
    static class GLThread extends Thread {

        private static int DEFAULT_SURFACE_WIDTH = 720;
        private static int DEFAULT_SURFACE_HEIGHT = 1280;

        private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
        private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;

        // stop the thread
        private boolean mIsExit = false;

        // GL环境是否合法
        private boolean mIsValid = false;

        // 释放队列
        private Runnable mReleaseRunnable;

        // 渲染队列，内外部公用
        private List<Runnable> mRunOnDraw;

        // 渲染锁，内外部公用
        private Object mLock;

        public GLThread(WeakReference<List<Runnable>> runnable, WeakReference<Object> object) {
            super();
            this.mRunOnDraw = runnable.get();
            this.mLock = object.get();
        }

        /**
         * 刷新
         */
        public void requestRender() {
            synchronized (mLock) {
                mLock.notify();
            }
        }

        /**
         * 停止
         */
        public void stopGL(Runnable runnable) {
            synchronized (mLock) {
                mReleaseRunnable = runnable;
                mIsExit = true;
                mLock.notify();
            }
        }

        @Override
        public void run() {
            setName("GLThread " + getId());
            try {
                guardedRun();
            } catch (InterruptedException e) {
                // fall thru and exit normally
            } finally {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * 哨兵
         *
         * @throws InterruptedException
         */
        private void guardedRun()
                throws InterruptedException {
            while (true) {
                if (!mIsValid) {
                    // TODO create egl context
                    createEGLContext(DEFAULT_SURFACE_WIDTH, DEFAULT_SURFACE_HEIGHT);
                }

                if (mIsExit) {
                    // TODO teminate the egl context
                    if (mIsValid) {
                        if (mReleaseRunnable != null) {
                            mReleaseRunnable.run();
                            mReleaseRunnable = null;
                        }
                        terminateEGL();
                    }
                    break;
                }

                synchronized (mLock) {
                    while (!mRunOnDraw.isEmpty()) {
                        mRunOnDraw.remove(0).run();
                    }
                    mLock.wait();
                }
            }
        }

        /**
         * 创建EGL环境
         * @param width     surface宽
         * @param height    surface高
         */
        private void createEGLContext(int width, int height) {

            mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
                throw new RuntimeException("unable to get EGL14 display");
            }
            int[] version = new int[2];
            if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
                mEGLDisplay = null;
                throw new RuntimeException("unable to initialize EGL14");
            }

            // Configure EGL for pbuffer and OpenGL ES 2.0, 24-bit RGB.
            int[] attribList = {
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                    EGL14.EGL_NONE
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] numConfigs = new int[1];
            if (!EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, configs, 0, configs.length,
                    numConfigs, 0)) {
                throw new RuntimeException("unable to find RGB888+recordable ES2 EGL config");
            }

            // Configure context for OpenGL ES 2.0.
            int[] attrib_list = {
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL14.EGL_NONE
            };
            mEGLContext = EGL14.eglCreateContext(mEGLDisplay, configs[0], EGL14.EGL_NO_CONTEXT,
                    attrib_list, 0);
            if (mEGLContext == EGL14.EGL_NO_CONTEXT) {
                throw new RuntimeException("EGL error " + EGL14.eglGetError());
            }

            // Create a pbuffer surface.
            int[] surfaceAttribs = {
                    EGL14.EGL_WIDTH, width,
                    EGL14.EGL_HEIGHT, height,
                    EGL14.EGL_NONE
            };
            mEGLSurface = EGL14.eglCreatePbufferSurface(mEGLDisplay, configs[0], surfaceAttribs, 0);
            if (mEGLSurface == EGL14.EGL_NO_SURFACE) {
                throw new RuntimeException("surface was null");
            }

            if (!EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)) {
                throw new RuntimeException("eglMakeCurrent failed");
            }
            mIsValid = true;
        }

        /**
         * 销毁EGL环境
         */
        private void terminateEGL() {
            if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
                if (mEGLContext != EGL14.EGL_NO_CONTEXT) {
                    EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
                }
                if (mEGLSurface != EGL14.EGL_NO_SURFACE) {
                    EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
                }
                EGL14.eglReleaseThread();
                EGL14.eglTerminate(mEGLDisplay);
            }
            mEGLDisplay = EGL14.EGL_NO_DISPLAY;
            mEGLContext = EGL14.EGL_NO_CONTEXT;
            mEGLSurface = EGL14.EGL_NO_SURFACE;
        }
    }

    private void checkRenderThreadState() {
        if (mGLThread != null) {
            throw new IllegalStateException("setRenderer has already been called for this instance.");
        }
    }
}
