package com.zhichu.nativeplugin.ble.scan;

import android.bluetooth.BluetoothDevice;

public interface IScanFilter {

    /**
     * 过滤
     *
     * @param device     蓝牙设备
     * @param rssi       蓝牙信号量
     * @param scanRecord 蓝牙广播数据
     * @return
     */
    boolean onFilter(BluetoothDevice device, int rssi, byte[] scanRecord);
}
