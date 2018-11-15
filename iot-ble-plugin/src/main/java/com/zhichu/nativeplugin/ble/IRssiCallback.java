package com.zhichu.nativeplugin.ble;

public interface IRssiCallback {
    /**
     * 获取蓝牙信号值成功
     *
     * @param rssi 蓝牙信号值
     */
    void onRssi(int rssi);

    void onFailure(BleDevice bleDevice, int code, String message);
}
