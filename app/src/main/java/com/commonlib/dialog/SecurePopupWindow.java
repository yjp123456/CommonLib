package com.commonlib.dialog;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.PopupWindow;



/**
 * Created by jieping on 2018/9/3.
 */

public class SecurePopupWindow extends PopupWindow implements LifecycleObserver {
    private static final String TAG = "SecurePopupWindow";

    private Context mContext;


    public SecurePopupWindow(@NonNull Context context) {
        super(context);
        init(context);
    }

    public SecurePopupWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SecurePopupWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, defStyleAttr);
        init(context);
    }

    public SecurePopupWindow(View contentView, int width, int height) {
        super(contentView, width, height);
        if (contentView != null) {
            init(contentView.getContext());
        }
    }

    private void init(Context context) {
        mContext = context;
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        if (isEnableShow()) {
            super.showAtLocation(parent, gravity, x, y);
        }
    }

    @Override
    public void showAsDropDown(View anchor) {
        if (isEnableShow()) {
            super.showAsDropDown(anchor);
        }
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        if (isEnableShow()) {
            super.showAsDropDown(anchor, xoff, yoff);
        }
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        if (isEnableShow()) {
            super.showAsDropDown(anchor, xoff, yoff, gravity);
        }
    }


    private boolean isEnableShow() {
        try {
            Activity activity = (Activity) mContext;
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                Log.d(TAG, "show is invalid, activity is abnormal");
                return false;
            }
            if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) {
                Log.d(TAG, "show is invalid, no in UI thread");
               /* if (ApplicationConfigure.isForTesters()) {
                    throw new IllegalStateException("Cannot show popupWindow on Non UI Thread:");
                }*/
                return false;
            }
            if (activity instanceof AppCompatActivity) {
                ((AppCompatActivity) activity).getLifecycle().addObserver(this);
            }
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public void dismiss() {
        try {
            Activity activity = (Activity) mContext;

            if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                return;
            }
            if (isShowing()) {
                super.dismiss();
            }
            if (activity instanceof AppCompatActivity) {
                ((AppCompatActivity) activity).getLifecycle().removeObserver(this);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void onDestroy() {
        try {
            if (isShowing()) {
                super.dismiss();
            }
            Activity activity = (Activity) mContext;
            if (activity instanceof AppCompatActivity) {
                ((AppCompatActivity) activity).getLifecycle().removeObserver(this);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}