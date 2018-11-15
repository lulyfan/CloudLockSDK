package com.zhichu.nativeplugin.ble;

public interface IConnectCallback {

    /**
     * 连接成功
     *
     * @param bleDevice 蓝牙对象
     */
    void onConnectSuccess(BleDevice bleDevice);

    /**
     * 连接断开
     *
     * @param bleDevice
     * @param isActive  是否主动断开
     */
    void onDisconnect(BleDevice bleDevice, boolean isActive);

    /**
     * 正在连接
     *
     * @param bleDevice
     */
    void onConnect(BleDevice bleDevice);

    /**
     * 连接失败
     *
     * @param bleDevice 蓝牙设备 ，有可能为空
     * @param code
     * @param message
     */
    void onFailure(BleDevice bleDevice, int code, String message);
}
