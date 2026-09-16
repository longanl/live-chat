package com.xuziran.livechat.common.constant;

public class Constant {
    public static final Integer MESSAGE_TYPE_TEXT = 1;
    public static final Integer MESSAGE_TYPE_FILE = 2;
    public static final String GROUP = "group";
    public static final String P2P = "p2pchat";
    public static final String MESSAGES_QUEUE = "/queue/messages";
    public static final String PRESENCE_SNAPSHOT_QUEUE = "/queue/presence";
    /** 业务异常定向提示队列（如非好友私聊），避免抛 ERROR 帧导致客户端断连 */
    public static final String ERROR_QUEUE = "/queue/errors";
}
