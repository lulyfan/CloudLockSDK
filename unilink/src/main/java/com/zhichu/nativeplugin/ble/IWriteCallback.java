package com.zhichu.nativeplugin.ble;

import java.util.UUID;

public interface IWriteCallback {
    /**
     * 读取数据成功
     *
     * @param serviceUUID        当前服务UUID
     * @param characteristicUUID 当前特征UUID
     */
    void onWrite(UUID serviceUUID, UUID characteristicUUID);

    void onFailure(BleDevice bleDevice, int code, String message);

}
