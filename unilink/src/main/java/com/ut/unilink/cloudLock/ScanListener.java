package com.ut.unilink.cloudLock;

import java.util.List;

/**
 * 搜索设备监听器
 */
public interface ScanListener {

    /**
     * 搜索到相应设备时调用该方法，当发现新的设备时，会多次调用
     * @param scanDevice 每次搜索到的设备
     */
    void onScan(ScanDevice scanDevice);

    /**
     * 搜索结束时调用,中途调用停止搜索也会调用
     * @param scanDevices 本次搜索到的所有设备
     */
    void onFinish(List<ScanDevice> scanDevices);

    /**
     * 扫描超时，表示本次扫描没有扫出设备
     */
    void onScanTimeout();
}
