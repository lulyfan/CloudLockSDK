package com.zhichu.nativeplugin.ble.scan;

import com.zhichu.nativeplugin.ble.BleDevice;

import java.util.List;

public interface IScanCallback {
    /**
     * 发现设备
     *
     * @param bleDevice 发现的蓝牙设备
     * @param result    蓝牙设备集合 ，已排除重复的设备集合
     */
    void onDeviceFound(BleDevice bleDevice, List<BleDevice> result);

    /**
     * 扫描完成
     *
     * @param result 蓝牙设备集合 ， 已排除重复的设备集合
     */
    void onScanFinish(List<BleDevice> result);

    /**
     * 扫描超时
     */
    void onScanTimeout();
}
