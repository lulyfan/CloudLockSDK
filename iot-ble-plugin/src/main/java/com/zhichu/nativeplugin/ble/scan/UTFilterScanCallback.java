package com.zhichu.nativeplugin.ble.scan;

import android.bluetooth.BluetoothDevice;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * author : zhouyubin (change by huangkaifan)
 * time   : 2018/08/30
 * desc   :过滤优特蓝牙设备
 * version: 1.0
 */
public class UTFilterScanCallback extends ScanCallback {

    private byte[] mVendorId;
    private byte[] mDeviceType;

    public UTFilterScanCallback(IScanCallback iScanCallback) {
        super(iScanCallback);
        super.serviceUUID("0001");
    }

    public UTFilterScanCallback resetCallback(IScanCallback iScanCallback) {
        super.bleDeviceFoundMap.clear();
        super.bleDeviceList.clear();
        return this;
    }

    public void setVendorId(byte[] vendorId) {
        this.mVendorId = vendorId;
    }

    public void setDeviceType(byte[] deviceType) {
        this.mDeviceType = deviceType;
    }

    @Override
    public boolean onFilter(BluetoothDevice device, int rssi, byte[] scanRecord) {

        if (scanRecord.length < 18) {
            return false;
        }

        if (scanRecord[1] != (byte) 0xff || scanRecord[2] != 0x55 || scanRecord[3] != 0x54) {
            return false;
        }

        //过滤厂商标识
        if (mVendorId != null) {
            byte[] vendorId = new byte[4];
            System.arraycopy(scanRecord, 5, vendorId, 0, 4);

            if (!Arrays.equals(vendorId, mVendorId)) {
                return false;
            }
        }

        //过滤设备类型
        if (mDeviceType != null) {
            byte[] deviceType = new byte[2];
            System.arraycopy(scanRecord, 10, deviceType, 0, 2);

            if (!Arrays.equals(deviceType, mDeviceType)) {
                return false;
            }
        }

        return true;
    }
}
