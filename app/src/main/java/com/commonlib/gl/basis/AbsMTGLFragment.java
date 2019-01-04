package com.commonlib.gl.basis;

import android.app.Activity;
import android.os.Build;
import android.support.v4.app.Fragment;


public abstract class AbsMTGLFragment extends Fragment {


    /**
     * 通过UI上下文安全的activity对象在主线程中执行Runnable
     */
    protected void securelyRunOnUiThread(Runnable runnable) {
        Activity secureContextForUI = getSecureContextForUI();

        if (secureContextForUI != null) {
            secureContextForUI.runOnUiThread(runnable);
        }
    }

    /**
     * 取得UI上下文安全的activity对象
     *
     * @return UI上下文安全的activity对象
     */
    protected Activity getSecureContextForUI() {
        Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (activity.isDestroyed()) {
                return null;
            }
        }
        return activity;
    }
}
