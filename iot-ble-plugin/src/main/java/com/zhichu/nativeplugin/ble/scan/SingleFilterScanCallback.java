package com.zhichu.nativeplugin.ble.scan;

import android.bluetooth.BluetoothDevice;

import java.util.concurrent.atomic.AtomicBoolean;

public class SingleFilterScanCallback extends ScanCallback {
    private AtomicBoolean atomicBoolean = new AtomicBoolean(false);
    private String deviceUUID;
    private String deviceName;

    public SingleFilterScanCallback(IScanCallback iScanCallback) {
        super(iScanCallback);
    }

    /**
     * 设置搜索设备UUID 或者 MAC地址
     *
     * @param deviceUUID
     * @return
     */
    public SingleFilterScanCallback setDeviceUUID(String deviceUUID) {
        this.deviceUUID = deviceUUID;
        return this;
    }

    /**
     * 设置搜索设备名称
     *
     * @param deviceName
     * @return
     */
    public SingleFilterScanCallback setDeviceName(String deviceName) {
        this.deviceName = deviceName;
        return this;
    }

    @Override
    public boolean onFilter(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (deviceUUID != null && device.getAddress().equalsIgnoreCase(deviceUUID)) {
            if (atomicBoolean.compareAndSet(false, true)) {
                stop();
                return true;
            }
        } else if (deviceName != null && device.getName().equalsIgnoreCase(deviceName)) {
            if (atomicBoolean.compareAndSet(false, true)) {
                stop();
                return true;
            }
        }
        return false;
    }
}
