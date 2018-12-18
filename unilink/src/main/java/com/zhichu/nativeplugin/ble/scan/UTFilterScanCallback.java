package com.zhichu.nativeplugin.ble.scan;

import android.bluetooth.BluetoothDevice;

import com.ut.unilink.util.Log;

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

        System.out.println(Log.toUnsignedHex(scanRecord, " "));

        byte[] cloudLockRecord = getClockLockRecord(scanRecord);
        if (cloudLockRecord == null) {
            return false;
        }

        //过滤厂商标识
        if (mVendorId != null) {
            byte[] vendorId = new byte[4];
            System.arraycopy(cloudLockRecord, 5, vendorId, 0, 4);

            if (!Arrays.equals(vendorId, mVendorId)) {
                return false;
            }
        }

        //过滤设备类型
        if (mDeviceType != null) {
            byte[] deviceType = new byte[2];
            System.arraycopy(cloudLockRecord, 10, deviceType, 0, 2);

            if (!Arrays.equals(deviceType, mDeviceType)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 获取广播包里的云锁数据包
     * @param scanRecord
     * @return
     */
    public static byte[] getClockLockRecord(byte[] scanRecord) {
        ByteBuffer buffer = ByteBuffer.wrap(scanRecord);
        byte[] msg = null;

        //寻找类型为0xFF的广播包
        while (buffer.hasRemaining()) {
            int length = buffer.get();
            byte[] itemBuffer = new byte[length + 1];
            itemBuffer[0] = (byte) length;
            buffer.get(itemBuffer, 1, length);

            if (length < 17) {
                continue;
            }

            if (itemBuffer[1] == (byte) 0xff && itemBuffer[2] == (byte) 0x55 && itemBuffer[3] == (byte) 0x54) {
                msg = itemBuffer;
                break;
            }
        }

        return msg;
    }
}
