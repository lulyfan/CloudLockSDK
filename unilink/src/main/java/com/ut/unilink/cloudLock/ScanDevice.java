package com.ut.unilink.cloudLock;

import android.os.Parcel;
import android.os.Parcelable;

import com.zhichu.nativeplugin.ble.BleDevice;
import com.zhichu.nativeplugin.ble.scan.UTFilterScanCallback;

/**
 * <p>表示通过蓝牙搜索出的设备。
 * <p>通过调用{@link com.ut.unilink.UnilinkManager#scan(ScanListener, int)}、{@link com.ut.unilink.UnilinkManager#scan(ScanListener, int, byte[], byte[])}
 * 搜索出相应设备，调用{@link #isActive}得到设备激活状态
 */
public class ScanDevice implements Parcelable {
    private String address;
    private byte[] vendorId = new byte[4];      //厂商标识
    private boolean isActive;
    private byte[] deviceType = new byte[2];
    private BleDevice bleDevice;

    public ScanDevice() {
    }

    protected ScanDevice(Parcel in) {
        address = in.readString();
        vendorId = in.createByteArray();
        isActive = in.readByte() != 0;
        deviceType = in.createByteArray();
        bleDevice = in.readParcelable(BleDevice.class.getClassLoader());
    }

    public static final Creator<ScanDevice> CREATOR = new Creator<ScanDevice>() {
        @Override
        public ScanDevice createFromParcel(Parcel in) {
            return new ScanDevice(in);
        }

        @Override
        public ScanDevice[] newArray(int size) {
            return new ScanDevice[size];
        }
    };

    /**
     * 获取设备mac地址
     *
     * @return
     */
    public String getAddress() {
        return address;
    }

    public String getName() {
        return bleDevice.getName();
    }

    /**
     * 设置设备mac地址
     *
     * @param address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * 获取厂商标识
     *
     * @return
     */
    public byte[] getVendorId() {
        return vendorId;
    }

    /**
     * 设置厂商标识
     *
     * @param vendorId 4字节数组
     */
    public void setVendorId(byte[] vendorId) {
        if (vendorId == null || vendorId.length != 4) {
            return;
        }
        this.vendorId = vendorId;
    }

    /**
     * 设备是否激活
     *
     * @return
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * 设置设备激活状态
     *
     * @param active
     */
    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * 获取设备类型
     *
     * @return
     */
    public byte[] getDeviceType() {
        return deviceType;
    }

    /**
     * 设置设备类型
     *
     * @param deviceType 2字节数组
     */
    public void setDeviceType(byte[] deviceType) {
        if (deviceType == null || deviceType.length != 2) {
            return;
        }
        this.deviceType = deviceType;
    }

    void setBleDevice(BleDevice bleDevice) {
        this.bleDevice = bleDevice;

        byte[] scanRecord = bleDevice.getScanRecord();
        byte[] cloudLockRecord = UTFilterScanCallback.getClockLockRecord(scanRecord);

        System.arraycopy(cloudLockRecord, 5, vendorId, 0, 4);
        isActive = cloudLockRecord[9] == 1 ? true : false;
        System.arraycopy(cloudLockRecord, 10, deviceType, 0, 2);
    }

    public BleDevice getBleDevice() {
        return bleDevice;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(address);
        dest.writeByteArray(vendorId);
        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeByteArray(deviceType);
        dest.writeParcelable(bleDevice, flags);
    }
}
