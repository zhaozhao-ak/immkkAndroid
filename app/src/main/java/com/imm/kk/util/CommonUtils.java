package com.imm.kk.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.inputmethod.InputMethodManager;


/**
 * Created by suweiming on 2017/12/28.
 * 一般通用工具
 */

public class CommonUtils {

    // 申请权限
    public static final String[] REQUEST_BASIC_PERMISSIONS = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.CAMERA,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    /**
     * 一般的回调口
     */
    public interface CommonCallBack {
        void callBack(boolean isSuccess, String msg);
    }

    /**
     * 隐藏软键盘
     */
    public static void hideInput(Activity activity) {
        try {

            //获取当屏幕内容的高度
            int screenHeight = activity.getWindow().getDecorView().getHeight();
            //获取View可见区域的bottom
            Rect rect = new Rect();
            //DecorView即为activity的顶级view
            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            //考虑到虚拟导航栏的情况（虚拟导航栏情况下：screenHeight = rect.bottom + 虚拟导航栏高度）
            //选取screenHeight*2/3进行判断
            if ((screenHeight * 2 / 3 > rect.bottom) == false) {
                return;
            }

            InputMethodManager inputmanager = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputmanager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {

        }
    }
}
