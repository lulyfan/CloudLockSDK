package com.ut.unilink.cloudLock;

import android.os.Parcel;
import android.os.Parcelable;

import com.zhichu.nativeplugin.ble.BleDevice;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

public class UTBleDevice implements Parcelable{
    private String address;
    private byte[] vendorId = new byte[4];      //厂商标识
    private boolean isActive;
    private byte[] deviceType = new byte[2];
    private BleDevice bleDevice;

    public UTBleDevice() {

    }

    protected UTBleDevice(Parcel in) {
        address = in.readString();
        vendorId = in.createByteArray();
        isActive = in.readByte() != 0;
        deviceType = in.createByteArray();
        bleDevice = in.readParcelable(BleDevice.class.getClassLoader());
    }

    public static final Creator<UTBleDevice> CREATOR = new Creator<UTBleDevice>() {
        @Override
        public UTBleDevice createFromParcel(Parcel in) {
            return new UTBleDevice(in);
        }

        @Override
        public UTBleDevice[] newArray(int size) {
            return new UTBleDevice[size];
        }
    };

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public byte[] getVendorId() {
        return vendorId;
    }

    public void setVendorId(byte[] vendorId) {
        if (vendorId == null || vendorId.length != 4) {
            return;
        }
        this.vendorId = vendorId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public byte[] getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(byte[] deviceType) {
        if (deviceType == null || deviceType.length != 2) {
            return;
        }
        this.deviceType = deviceType;
    }

    void setBleDevice(BleDevice bleDevice) {
        this.bleDevice = bleDevice;

        byte[] scanRecord = bleDevice.getScanRecord();
        System.arraycopy(scanRecord, 5, vendorId, 0,4);
        isActive = scanRecord[9] == 1 ? true : false;
        System.arraycopy(scanRecord, 10, deviceType, 0, 2);
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
