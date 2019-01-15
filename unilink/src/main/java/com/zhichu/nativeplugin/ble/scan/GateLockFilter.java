package com.zhichu.nativeplugin.ble.scan;

import android.bluetooth.BluetoothDevice;

import java.io.UnsupportedEncodingException;

public class GateLockFilter extends DeviceFilter{

    public GateLockFilter() {
        super(DeviceId.GATE_LOCK);
    }

    @Override
    public boolean onFilter(BluetoothDevice device, int rssi, byte[] scanRecord) {

        int U_pos = -1;
        for (int i=0; i<scanRecord.length; i++) {
            if (scanRecord[i] == 'U') {
                U_pos = i;
                break;
            }
        }

        if (U_pos == -1) {
            return false;
        }

        if (scanRecord.length - U_pos < 16) {
            return false;
        }

        byte[] mac = new byte[12];
        String sMac = "";
        System.arraycopy(scanRecord, U_pos + 4, mac, 0, mac.length);
        try {
            sMac = new String(mac, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (!sMac.equals(device.getAddress().replace(":", ""))) {
            return false;
        }

        return true;
    }

    /**
     * 从扫描应答包中获取设备名
     * @param scanRecord
     * @return
     */
    public static String getName(byte[] scanRecord) {
        int U_pos = -1;
        for (int i=0; i<scanRecord.length; i++) {
            if (scanRecord[i] == 'U') {
                U_pos = i;
            }
        }

        byte[] name = new byte[16];
        String sName = "";
        System.arraycopy(scanRecord, U_pos, name, 0, name.length);
        try {
            sName = new String(name, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return sName;

    }
}
