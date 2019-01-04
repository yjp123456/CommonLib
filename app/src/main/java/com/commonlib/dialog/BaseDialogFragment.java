package com.commonlib.dialog;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

/**
 * 自定义DialogFragment基类
 * 处理{@link DialogFragment#show(FragmentManager, String)}可能导致的异常
 *
 * @author liyaochi on 2017/7/5.
 */

public class BaseDialogFragment extends DialogFragment {

    public void show(FragmentManager manager) {
        this.show(manager, this.getClass().getSimpleName());
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        if (isAdded()) {
            return;
        }
        try {
            super.show(manager, tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int show(FragmentTransaction transaction, String tag) {
        if (isAdded()) {
            return -1;
        }
        try {
            return super.show(transaction, tag);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public void dismiss() {
        try {
            super.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
