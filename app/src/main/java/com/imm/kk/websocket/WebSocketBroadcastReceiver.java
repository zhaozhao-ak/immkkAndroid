package com.imm.kk.websocket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;


/**
 * Created by Administrator on 2018/11/21 0021.
 */

public class WebSocketBroadcastReceiver extends BroadcastReceiver {

    public static final String WEB_SOCKET_ACTION = "rjyx.http.webSocket.connection";
    public static final String RECEIVE_MSG = "receiveMsg";
    public static final String CONNECTION_STATE = "connectionState";

    private ReceiveCallBack mCallBack;
    private Context mContext;


    public interface ReceiveCallBack {
        void onReceiveMsg(String receiveMsg);
        void sendTimeStamp(String receiveMsg);
    }

    public WebSocketBroadcastReceiver() {

    }

    public WebSocketBroadcastReceiver(Context context, ReceiveCallBack callBack) {
        // 传入回调
        this.mCallBack = callBack;
        this.mContext = context;
    }

    // 复写onReceive()方法
    // 接收到广播后，则自动调用该方法
    @Override
    public void onReceive(Context context, Intent intent) {
        //写入接收广播后的操作
        String action = intent.getAction();
        if (WEB_SOCKET_ACTION.equals(action)) {
            // 暂时只取接收的信息，状态不用管
            String receiveMsg = intent.getStringExtra(RECEIVE_MSG);
            System.out.println("web_socket----receiveMsg----"+receiveMsg);
            if (!TextUtils.isEmpty(receiveMsg)) {
                if (receiveMsg.contains("patientRefresh")) {
                    EventBus.getDefault().post(new MessageEvent("patientRefresh",""));

                }
                if (receiveMsg.contains("mobileLongConnection")) {
                    EventBus.getDefault().post(new MessageEvent("mobileLongConnection",""));
                }
            }
        }
    }
}
