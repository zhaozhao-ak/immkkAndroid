package com.imm.kk.websocket;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.imm.kk.HemoApplication;
import com.imm.kk.util.SharedPreferencesUtils;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;


/**
 * Created by Administrator on 2018/11/21 0021.
 */

public class WebSocketService extends Service {
    private static final String TAG = "WebSocketService";

    private static final long HEART_BEAT_RATE = 8 * 1000;//每隔5秒进行一次对长连接的心跳检测
    private WebSocket mWebSocket;
    private long sendTime = 0L;
    //通过binder实现调用者client与Service之间的通信
    private MyBinder mBinder = new MyBinder();
    //client 可以通过Binder获取Service实例


    private static boolean isRun = true;
    public static void setIsRun(boolean isRun) {
        WebSocketService.isRun = isRun;
    }



    //广播
    private WebSocketBroadcastReceiver webSocketReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        isRun = true;
        new InitSocketThread().start();
        webSocketReceiver = new WebSocketBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WebSocketBroadcastReceiver.WEB_SOCKET_ACTION);
        registerReceiver(webSocketReceiver, filter);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public void onDestroy() {
        System.out.println("-----WebSocketService---------onDestroy-----");
        super.onDestroy();
        isRun = false;
        if (mWebSocket != null) {
            mWebSocket.close(1000, null);
            if (mHandler != null && heartBeatRunnable != null) {
                mHandler.removeCallbacks(heartBeatRunnable);
            }
        }
        unregisterReceiver(webSocketReceiver);
    }

    public class MyBinder extends Binder {
        public WebSocketService getService() {
            return WebSocketService.this;
        }
    }


    /**
     * 发送消息
     */
    public void sendMsg(String msg) {
        mWebSocket.send(msg);
    }


    // 发送心跳包
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String message = (String) msg.obj;
                    Intent intent = new Intent();
                    intent.putExtra(WebSocketBroadcastReceiver.RECEIVE_MSG, message);
                    //设置intent的动作为com.example.broadcast，可以任意定义
                    intent.setAction(WebSocketBroadcastReceiver.WEB_SOCKET_ACTION);
                    //发送无序广播
                    sendBroadcast(intent);
                    break;
                case 2:
                    String connectionState = (String) msg.obj;
                    Intent connectionIntent = new Intent();
                    connectionIntent.putExtra(WebSocketBroadcastReceiver.CONNECTION_STATE, connectionState);
                    //设置intent的动作为com.example.broadcast，可以任意定义
                    connectionIntent.setAction(WebSocketBroadcastReceiver.WEB_SOCKET_ACTION);
                    //发送无序广播
                    sendBroadcast(connectionIntent);
                    break;
                case 3:
                    String connectionError = (String) msg.obj;
                    Intent connectionErrorIntent = new Intent();
                    connectionErrorIntent.putExtra(WebSocketBroadcastReceiver.CONNECTION_STATE, connectionError);
                    //设置intent的动作为com.example.broadcast，可以任意定义
                    connectionErrorIntent.setAction(WebSocketBroadcastReceiver.WEB_SOCKET_ACTION);
                    //发送无序广播
                    sendBroadcast(connectionErrorIntent);
                    mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);//每隔一定的时间，对长连接进行一次心跳检测
                    break;
                default:
                    break;
            }
        }
    };
    private Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (System.currentTimeMillis() - sendTime >= HEART_BEAT_RATE) {
                if (mWebSocket == null && isRun ==true) {
                    new InitSocketThread().start();//创建一个新的连接
                } else {
                    boolean isSuccess = mWebSocket.send("mobileLongConnection");//发送一个空消息给服务器，通过发送消息的成功失败来判断长连接的连接状态
                    if (!isSuccess) {//长连接已断开
                        mWebSocket.close(1000, null);
                        mWebSocket.cancel();//取消掉以前的长连接
                        mWebSocket = null;
                        mHandler.removeCallbacks(heartBeatRunnable);
                        new InitSocketThread().start();//创建一个新的连接
                    } else {//长连接处于连接状态
//                        if (WebHemoApplication.getInstance().isWebSocketConnected()) {
                        Message msg = new Message();
                        msg.what = 3;
                        msg.obj = "长连接处于连接状态";
                        mHandler.sendMessage(msg);
                        Log.e("WebSocket-----", "长连接处于连接状态");
//                        }
                    }
                    sendTime = System.currentTimeMillis();
                }
            }
        }
    };

    class InitSocketThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                System.out.println("---Thread------run------------");
                initSocket();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 心跳检测时间
     */

    // 初始化socket
    private void initSocket() throws UnknownHostException, IOException {
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build();

        // 获取url地址 "ws://192.168.8.241:8083/hdis/websocket/581476892ea3c04dfdc8bfabf52d2a74";
//        String ip = PrefUtils.getWebApiServerIp(this);
//        String port = PrefUtils.getWebApiServerPort(this);
//        String url = "ws://" + ip + ":" + port + "/hdis/websocket/"
//                + WebHemoApplication.getInstance().getAccountId();// 当前登录用户ID
        String url = "";
        String mHomeUrl = SharedPreferencesUtils.getHttpUrl(HemoApplication.getInstance().getApplicationContext());
        if (!TextUtils.isEmpty(mHomeUrl) && mHomeUrl.contains("hdis")){
            url = mHomeUrl.substring(0, mHomeUrl.indexOf("hdis"));//截取hdis之前的字符串
            url = url+"/hdis/websocket/00000";
        }

        Request request = new Request.Builder().url(url).build();
        client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {//开启长连接成功的回调
                super.onOpen(webSocket, response);
                mWebSocket = webSocket;
                Log.e("WebSocket----", "连接成功");
                Message msg = new Message();
                msg.what = 2;
                msg.obj = "连接成功";
                mHandler.sendMessage(msg);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {//接收消息的回调
                super.onMessage(webSocket, text);
                if (!TextUtils.isEmpty(text)) {
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = text;
                    mHandler.sendMessage(msg);
                    Log.e("WebSocket-接收到消息---", text);
                }
                //收到服务器端传过来的消息text
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                super.onMessage(webSocket, bytes);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                super.onClosing(webSocket, code, reason);
                Log.e("WebSocket-onClosed", reason);

            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                Log.e("WebSocket-onClosed", reason);

            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {//长连接连接失败的回调
                super.onFailure(webSocket, t, response);
                Message msg = new Message();
                msg.what = 2;
                msg.obj = "长连接连接失败";
                mHandler.sendMessage(msg);
                Log.e("WebSocket---长连接连接失败----", t.toString());
            }
        });
        client.dispatcher().executorService().shutdown();
        mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);//每隔一定的时间，对长连接进行一次心跳检测

    }

}
