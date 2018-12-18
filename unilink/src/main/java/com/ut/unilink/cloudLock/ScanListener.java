package com.ut.unilink.cloudLock;

import java.util.List;

/**
 * 搜索设备监听器
 */
public interface ScanListener {

    /**
     * 搜索到相应设备时调用该方法，当发现新的设备时，会多次调用
     * @param scanDevices 每次搜索到的设备集合
     */
    void onScan(ScanDevice scanDevice, List<ScanDevice> scanDevices);

    /**
     * 搜索结束时调用
     */
    void onFinish();
}
