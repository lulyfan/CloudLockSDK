package com.ut.unilink.cloudLock;

import java.util.List;

/**
 * 搜索设备监听器
 */
public interface ScanListener {

    /**
     * 搜索到相应设备时调用该方法，当发现新的设备时，会多次调用
     * @param bleDevice
     */
    void onScan(List<UTBleDevice> bleDevice);

    /**
     * 搜索结束时调用
     */
    void onFinish();
}
