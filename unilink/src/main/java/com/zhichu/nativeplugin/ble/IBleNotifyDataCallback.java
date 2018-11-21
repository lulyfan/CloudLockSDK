package com.zhichu.nativeplugin.ble;

import java.util.UUID;

public interface IBleNotifyDataCallback {
    /**
     * 通知回调
     *
     * @param bleDevice          蓝牙设备
     * @param data               数据
     * @param serviceUUID        当前服务UUID
     * @param characteristicUUID 当前特征UUID
     */
    void onNotify(BleDevice bleDevice, byte[] data, UUID serviceUUID, UUID characteristicUUID);
}
