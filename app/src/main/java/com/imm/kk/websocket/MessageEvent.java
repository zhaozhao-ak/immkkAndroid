package com.imm.kk.websocket;

/**
 * Created by Administrator on 2019-05-20.
 */

public class MessageEvent {
    public String actionType;
    public String msg;

    public MessageEvent(String actionType, String msg) {
        this.actionType = actionType;
        this.msg = msg;
    }
}
