package com.zhichu.nativeplugin.ble.scan;

import android.bluetooth.BluetoothDevice;

public abstract class DeviceFilter implements IScanFilter{

    private int deviceId;

    public DeviceFilter(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getDeviceId() {
        return deviceId;
    }
}
