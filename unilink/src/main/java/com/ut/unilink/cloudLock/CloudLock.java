package com.ut.unilink.cloudLock;

import com.ut.unilink.cloudLock.protocol.data.ProductInfo;

import java.util.HashMap;
import java.util.Map;

public class CloudLock {

    private byte[] adminPassword;
    private byte[] openLockPassword;
    private byte[] entryptKey;
    private int encryptVersion = -1;
    private int autuIncreaseNum;
    private UTBleDevice bleDevice;
    private byte[] serialNum;      //生产序列号
    private byte currentDeviceNum;          //当前想要读取的设备编号，用于读取从设备节点信息命令
    private Map<Byte, byte[]> deviceInfoMap = new HashMap<>();
    private ProductInfo productInfo;

    public CloudLock(String address) {
        bleDevice = new UTBleDevice();
        bleDevice.setAddress(address);
    }

    public String getAddress() {
        if (bleDevice == null) {
            return null;
        }
        return bleDevice.getAddress();
    }

    public UTBleDevice getBleDevice() {
        return bleDevice;
    }

    public void setBleDevice(UTBleDevice bleDevice) {
        this.bleDevice = bleDevice;
    }

    public byte[] getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(byte[] adminPassword) {
        this.adminPassword = adminPassword;
    }

    public byte[] getOpenLockPassword() {
        return openLockPassword;
    }

    public void setOpenLockPassword(byte[] openLockPassword) {
        this.openLockPassword = openLockPassword;
    }

    public byte[] getEntryptKey() {
        return entryptKey;
    }

    public void setEntryptKey(byte[] entryptKey) {
        this.entryptKey = entryptKey;
    }

    public int getEncryptVersion() {
        return encryptVersion;
    }

    public void setEncryptVersion(int encryptVersion) {
        this.encryptVersion = encryptVersion;
    }

    public int getAutuIncreaseNum() {
        return autuIncreaseNum;
    }

    public void setAutuIncreaseNum(int autuIncreaseNum) {
        this.autuIncreaseNum = autuIncreaseNum;
    }

    public byte[] getSerialNum() {
        return serialNum;
    }

    /**
     * 设置生产序列号
     * @param serialNum 数组长度为6
     */
    public void setSerialNum(byte[] serialNum) {
        this.serialNum = serialNum;
    }

    public byte[] getVendorId() {
        if (bleDevice == null) {
            return null;
        }
        return bleDevice.getVendorId();
    }

    /**
     * 设置厂商标识
     * @param vendorId 数组长度为4
     */
    public void setVendorId(byte[] vendorId) {
        if (bleDevice != null) {
            bleDevice.setVendorId(vendorId);
        }
    }

    public byte[] getDeviceType() {
        if (bleDevice == null) {
            return null;
        }
        return bleDevice.getDeviceType();
    }

    /**
     * 设置设备类型
     * @param deviceType 数组长度为2
     */
    public void setDeviceType(byte[] deviceType) {
        if (bleDevice != null) {
            bleDevice.setDeviceType(deviceType);
        }
    }

    /**
     * 设置当前要去读取得设备节点编号
     * @param deviceNum
     */
    public void setDeviceNum(byte deviceNum) {
        this.currentDeviceNum = deviceNum;
    }

    public byte getDeviceNum() {
        return currentDeviceNum;
    }

    public Map getDeviceInfoMap() {
        return deviceInfoMap;
    }

    public void setDeviceInfoMap(Map<Byte, byte[]> deviceInfoMap) {
        this.deviceInfoMap = deviceInfoMap;
    }

    /**
     * 获取指定设备节点信息
     * @param deviceNum
     * @return
     */
    public byte[] getDeviceInfo(byte deviceNum) {
        return deviceInfoMap.get(deviceNum);
    }

    /**
     * 添加指定设备节点信息
     * @param deviceNum
     * @param deviceInfo
     */
    public void addDeviceInfo(byte deviceNum, byte[] deviceInfo) {
        deviceInfoMap.put(deviceNum, deviceInfo);
    }

    public ProductInfo getProductInfo() {
        return productInfo;
    }

    public void setProductInfo(ProductInfo productInfo) {
        this.productInfo = productInfo;
    }
}
