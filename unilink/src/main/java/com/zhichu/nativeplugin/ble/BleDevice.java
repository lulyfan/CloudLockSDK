package com.zhichu.nativeplugin.ble;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

public class BleDevice implements Parcelable {
    public static final Creator<BleDevice> CREATOR = new Creator<BleDevice>() {
        @Override
        public BleDevice createFromParcel(Parcel in) {
            return new BleDevice(in);
        }

        @Override
        public BleDevice[] newArray(int size) {
            return new BleDevice[size];
        }
    };
    /**
     * 蓝牙对象
     */
    private BluetoothDevice bluetoothDevice;
    /**
     * 蓝牙信号值
     */
    private int rssi;
    /**
     * 蓝牙广播数据
     */
    private byte[] scanRecord;

    /**
     * 标识设备的硬件类型
     */
    private int deviceId;

    public BleDevice(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
        this.bluetoothDevice = bluetoothDevice;
        this.rssi = rssi;
        this.scanRecord = scanRecord;
    }

    public BleDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    protected BleDevice(Parcel in) {
        bluetoothDevice = in.readParcelable(BluetoothDevice.class.getClassLoader());
        rssi = in.readInt();
        scanRecord = in.createByteArray();
        deviceId = in.readInt();
    }

    /**
     * 蓝牙名称
     *
     * @return
     */
    public String getName() {
        return bluetoothDevice != null ? bluetoothDevice.getName() : null;
    }

    /**
     * 蓝牙设备UUID或者MAC地址
     *
     * @return 蓝牙设备UUID或者MAC地址
     */
    public String getDeviceUUID() {
        return bluetoothDevice != null ? bluetoothDevice.getAddress() : null;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    /**
     * 蓝牙设备信号值
     *
     * @return
     */
    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    /**
     * 蓝牙广播数据
     *
     * @return
     */
    public byte[] getScanRecord() {
        return scanRecord;
    }

    public void setScanRecord(byte[] scanRecord) {
        this.scanRecord = scanRecord;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(bluetoothDevice, flags);
        dest.writeInt(rssi);
        dest.writeByteArray(scanRecord);
        dest.writeInt(deviceId);
    }

    @Override
    public String toString() {
        return "{\"name\": \"" + bluetoothDevice.getName() + "\" , " +
                " \"deviceUUID\": \"" + bluetoothDevice.getAddress() + "\" }";
    }
}
