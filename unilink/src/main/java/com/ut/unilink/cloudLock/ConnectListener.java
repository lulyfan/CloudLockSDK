package com.ut.unilink.cloudLock;

/**
 * 连接监听器
 */
public interface ConnectListener {
    /**
     * 表示与设备连接成功
     */
    void onConnect();

    /**
     * 表示与设备连接失败或者断开连接
     * @param code 失连状态码
     * @param message 失连原因
     */
    void onDisconnect(int code, String message);
}
