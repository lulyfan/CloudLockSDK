package com.zhichu.nativeplugin.ble.scan;

import android.bluetooth.BluetoothDevice;

public class GateLockFilter extends DeviceFilter{

    public GateLockFilter() {
        super(DeviceId.GATE_LOCK);
    }

    @Override
    public boolean onFilter(BluetoothDevice device, int rssi, byte[] scanRecord) {
        String name = device.getName();
        if (name == null || name.length() != 16) {
            return false;
        }

        if (name.charAt(0) != 'U') {
            return false;
        }


        return true;
    }
}
