package com.zhichu.nativeplugin.ble.scan;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;

import com.zhichu.nativeplugin.ble.BleDevice;
import com.zhichu.nativeplugin.ble.UUIDHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ScanCallback extends android.bluetooth.le.ScanCallback implements IScanFilter {
    private static final int DEFAULT_SCANSECOND = 10;//默认搜索时间
    private Handler handler = new Handler(Looper.getMainLooper());
    private AtomicBoolean atomicBoolean = new AtomicBoolean(false);
    private IScanCallback iScanCallback;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothAdapter.LeScanCallback leScanCallback;
    protected List<ScanFilter> scanFilterList;
    protected ScanSettings.Builder scanSettingsBuilder;

    protected Map<String, BleDevice> bleDeviceFoundMap;
    protected List<BleDevice> bleDeviceList;
    private int scanSecond = DEFAULT_SCANSECOND;

    public ScanCallback(IScanCallback iScanCallback) {
        this.iScanCallback = iScanCallback;
        bleDeviceFoundMap = new LinkedHashMap<>();
        bleDeviceList = new ArrayList<>();
        if (LOLLIPOP()) {
            scanFilterList = new ArrayList<>();
            scanSettingsBuilder = new ScanSettings.Builder();
        }
    }

    public ScanCallback initBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
        return this;
    }


    /**
     * 设置搜索时间
     *
     * @param scanSecond
     * @return
     */
    public ScanCallback scanSecond(int scanSecond) {
        this.scanSecond = scanSecond;
        return this;
    }

    /**
     * UUID过滤，21版本以上支持
     *
     * @param serviceUUIDs
     * @return
     */
    public ScanCallback serviceUUID(String... serviceUUIDs) {
        if (serviceUUIDs != null) {
            for (String serviceUUID : serviceUUIDs) {
                ScanFilter filter = new ScanFilter.Builder()
                        .setServiceUuid(new ParcelUuid(
                                UUIDHelper.uuidFromString(serviceUUID)))
                        .build();
                scanFilterList.add(filter);
            }
        }
        return this;
    }

    /**
     * 配置，21版本以上支持
     *
     * @param settingBundle
     * @return
     */
    public ScanCallback scanSettings(Bundle settingBundle) {
        scanSettingsBuilder.setScanMode((int) settingBundle.getDouble("scanMode"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scanSettingsBuilder.setNumOfMatches((int) settingBundle
                    .getDouble("numberOfMatches", ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT));
            scanSettingsBuilder.setMatchMode((int) settingBundle
                    .getDouble("matchMode", ScanSettings.MATCH_MODE_AGGRESSIVE));
            scanSettingsBuilder.setNumOfMatches((int) settingBundle
                    .getDouble("matchNumMaxAdvertisement", ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT));
        }
        return this;
    }

    /**
     * 是否正在搜索
     *
     * @return
     */
    public boolean isScanning() {
        return atomicBoolean.get();
    }

    /**
     * 搜索
     */
    public final void scan() {
        if (atomicBoolean.compareAndSet(false, true)) {
            if (LOLLIPOP()) {
                this.bluetoothAdapter.getBluetoothLeScanner()
                        .startScan(scanFilterList, scanSettingsBuilder.build(), this);
            } else {
                this.bluetoothAdapter.startLeScan(leScanCallback);
                leScanCallback = new BluetoothAdapter.LeScanCallback() {
                    @Override
                    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                        onScanResultFond(device, rssi, scanRecord);
                    }
                };
            }
            if (this.scanSecond > 0) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (atomicBoolean.compareAndSet(true, false)) {
                            if (iScanCallback != null) {
                                if (!bleDeviceFoundMap.isEmpty()) {
                                    iScanCallback.onScanFinish(bleDeviceList);
                                } else {
                                    iScanCallback.onScanTimeout();
                                }
                            }
                            stopOrTimeoutScan();
                        }
                    }
                }, this.scanSecond * 1000);
            }
        }
    }

    public final void stop() {
        if (atomicBoolean.compareAndSet(true, false)) {
            handler.removeCallbacksAndMessages(null);
            stopOrTimeoutScan();
        }
    }

    public List<BleDevice> getLastScanFinishedDeviceList() {
        return new ArrayList<>(bleDeviceFoundMap.values());
    }

    public Map<String, BleDevice> getLastScanFinishedDeviceMap() {
        return bleDeviceFoundMap;
    }

    private void stopOrTimeoutScan() {
        if (LOLLIPOP()) {
            this.bluetoothAdapter.getBluetoothLeScanner().stopScan(this);
        } else {
            this.bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    private boolean LOLLIPOP() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    @Override
    public final void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        onScanResultFond(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
    }

    final void onScanResultFond(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (onFilter(device, rssi, scanRecord)) {
            BleDevice bleDevice;
            if (bleDeviceFoundMap.containsKey(device.getAddress())) {
                bleDevice = bleDeviceFoundMap.get(device.getAddress());
                bleDevice.setRssi(rssi);
                bleDevice.setScanRecord(scanRecord);
            } else {
                bleDevice = new BleDevice(device, rssi, scanRecord);
                bleDeviceFoundMap.put(device.getAddress(), bleDevice);
            }
            if (iScanCallback != null) {
                bleDeviceList.clear();
                bleDeviceList.addAll(bleDeviceFoundMap.values());
                iScanCallback.onDeviceFound(bleDevice, bleDeviceList);
                if (this instanceof SingleFilterScanCallback) {
                    iScanCallback.onScanFinish(bleDeviceList);
                }
            }
        }
    }

    @Override
    public boolean onFilter(BluetoothDevice device, int rssi, byte[] scanRecord) {
        return true;
    }

}
