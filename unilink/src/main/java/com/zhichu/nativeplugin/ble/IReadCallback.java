package com.zhichu.nativeplugin.ble;

import java.util.UUID;

public interface IReadCallback {
    /**
     * 读取数据成功
     *
     * @param bleDevice
     * @param data               数据
     * @param serviceUUID        当前服务UUID
     * @param characteristicUUID 当前特征UUID
     */
    void onRead(BleDevice bleDevice, byte[] data, UUID serviceUUID, UUID characteristicUUID);

    void onFailure(BleDevice bleDevice, int code, String message);

}
