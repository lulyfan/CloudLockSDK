package com.zhichu.nativeplugin.ble;

import java.util.UUID;

public interface INotifyCallback {
    /**
     * 通知回调
     *
     * @param serviceUUID        当前服务UUID
     * @param characteristicUUID 当前特征UUID
     */
    void onNotify(UUID serviceUUID, UUID characteristicUUID);

    void onFailure(BleDevice bleDevice, int code, String message);
}
