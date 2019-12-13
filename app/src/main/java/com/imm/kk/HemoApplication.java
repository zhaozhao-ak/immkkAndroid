package com.imm.kk;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.imm.kk.websocket.WebSocketService;

import java.util.List;

/**
 * Created by suweiming on 2018/12/4.
 */

public class HemoApplication extends Application {
    private final static String TAG = "HemoApplication";
    private static HemoApplication mInstance = null;// 单例

    @Override
    public void onCreate() {
        synchronized (this) {
            if (mInstance == null) {
                mInstance = this;
            }
        }
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        // 程序终止的时候一定要销毁websocket（异常情况）
        super.onTerminate();
    }

    /**
     * 单例获取
     */
    public static synchronized HemoApplication getInstance() {
        return mInstance;
    }


    /**
     * 启动webSocket
     */
    public void webSocketStart() {
        // 直接开启服务
        if (!isServiceRunning(HemoApplication.this,"com.rjyx.hdis.fullscreen.websocket.WebSocketService")){
            Intent intent = new Intent(this, WebSocketService.class);
            startService(intent);
        }
    }

    /**
     * 销毁webSocket
     */
    public void webSocketDestroy() {
        if (isServiceRunning(HemoApplication.this,"com.rjyx.hdis.fullscreen.websocket.WebSocketService")){
            WebSocketService.setIsRun(false);
            Intent intent = new Intent(this, WebSocketService.class);
            stopService(intent);
        }
    }

    /**
     * 方法描述：判断某一Service是否正在运行
     *
     * @param context     上下文
     * @param serviceName Service的全路径： 包名 + service的类名
     * @return true 表示正在运行，false 表示没有运行
     */
    private boolean isServiceRunning(Context context, String serviceName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServiceInfos = am.getRunningServices(200);
        if (runningServiceInfos.size() <= 0) {
            return false;
        }
        for (ActivityManager.RunningServiceInfo serviceInfo : runningServiceInfos) {
            if (serviceInfo.service.getClassName().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }


}
