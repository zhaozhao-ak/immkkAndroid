package com.imm.kk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

/**
 * Created by Administrator on 2019/3/15 0015.
 */

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化base的设定
        initBaseSetting();
    }

    /**
     * 初始化base的设定
     */
    private void initBaseSetting() {
        // 保持屏幕唤醒
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
