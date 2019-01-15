package com.ut.unilink.cloudLock;

import android.os.Parcel;
import android.os.Parcelable;

import com.zhichu.nativeplugin.ble.BleDevice;
import com.zhichu.nativeplugin.ble.scan.CloudLockFilter;
import com.zhichu.nativeplugin.ble.scan.DeviceId;
import com.zhichu.nativeplugin.ble.scan.GateLockFilter;

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
    private int deviceId;
    private int version;
    private String name;

    public ScanDevice() {
    }

    protected ScanDevice(Parcel in) {
        address = in.readString();
        vendorId = in.createByteArray();
        isActive = in.readByte() != 0;
        deviceType = in.createByteArray();
        bleDevice = in.readParcelable(BleDevice.class.getClassLoader());
        deviceId = in.readInt();
        version = in.readInt();
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
        if (name == null) {
            name = bleDevice.getName();
        }
        return name;
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
     * 获取设备硬件标识
     * @return
     */
    public int getDeviceId() {
        return deviceId;
    }

    /**
     * 设置设备硬件标识
     * @param deviceId
     */
    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * 获取版本号
     * @return
     */
    public int getVersion() {
        return version;
    }

    /**
     * 设置版本号
     * @param version
     */
    public void setVersion(int version) {
        this.version = version;
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
        deviceId = bleDevice.getDeviceId();

        switch (bleDevice.getDeviceId()) {
            case DeviceId.CLOUD_LOCK:           //云锁
                byte[] scanRecord = bleDevice.getScanRecord();
                byte[] cloudLockRecord = CloudLockFilter.getClockLockRecord(scanRecord);
                version = cloudLockRecord[4];
                System.arraycopy(cloudLockRecord, 5, vendorId, 0, 4);
                isActive = cloudLockRecord[9] == 1 ? true : false;
                System.arraycopy(cloudLockRecord, 10, deviceType, 0, 2);
                break;

            case DeviceId.GATE_LOCK:            //门锁
                byte[] scanRecord2 = bleDevice.getScanRecord();
                String name = GateLockFilter.getName(scanRecord2);
                this.name = name;
                version = Integer.parseInt(name.substring(1, 3));
                isActive = name.charAt(3) == '1' ? true : false;
                break;

            default:

        }
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
        dest.writeInt(deviceId);
        dest.writeInt(version);
    }
}
