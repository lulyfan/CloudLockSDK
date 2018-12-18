package com.ut.unilink.cloudLock;

import com.ut.unilink.cloudLock.protocol.data.ProductInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>表示云锁设备。
 * <p>当搜索到未激活的云锁设备时，通过调用{@link com.ut.unilink.UnilinkManager#initLock(ScanDevice, CallBack)}初始化云锁可以得到CloudLock对象，
 *里面保存了云锁设备的相关配置信息，再调用{@link com.ut.unilink.UnilinkManager#confirmInit(CloudLock, CallBack)}确认初始化完成激活
 */
public class CloudLock {

    private byte[] adminPassword;
    private byte[] openLockPassword;
    private byte[] entryptKey;
    private int encryptType = -1;
    private int autuIncreaseNum;
    private ScanDevice bleDevice;
    private byte currentDeviceNum;          //当前想要读取的设备编号，用于读取从设备节点信息命令
    private Map<Byte, byte[]> deviceInfoMap = new HashMap<>();
    private ProductInfo productInfo;

    public CloudLock(String mac) {
        bleDevice = new ScanDevice();
        bleDevice.setAddress(mac);
    }

    /**
     * 获取云锁设备mac地址
     * @return 6字节mac地址
     */
    public String getAddress() {
        if (bleDevice == null) {
            return null;
        }
        return bleDevice.getAddress();
    }

    public void setAddress(String mac) {
        if (bleDevice == null) {
            bleDevice = new ScanDevice();
        }
        bleDevice.setAddress(mac);
    }

    public ScanDevice getBleDevice() {
        return bleDevice;
    }

    public void setBleDevice(ScanDevice bleDevice) {
        this.bleDevice = bleDevice;

        if (bleDevice != null) {
            setVendorId(bleDevice.getVendorId());
        }
    }

    /**
     * 获取云锁激活状态
     * @return 激活状态
     */
    public boolean isActive() {
        return bleDevice.isActive();
    }

    /**
     * 设置云锁激活状态
     * @param active
     */
    public void setActive(boolean active) {
        bleDevice.setActive(active);
    }

    /**
     * 获取管理员密码
     * @return 6字节管理员密码
     */
    public byte[] getAdminPassword() {
        return adminPassword;
    }

    /**
     * 设置管理员密码
     * @param adminPassword 长度为6的字节数组
     */
    public void setAdminPassword(byte[] adminPassword) {
        this.adminPassword = adminPassword;
    }

    public byte[] getOpenLockPassword() {
        return openLockPassword;
    }

    /**
     * 设置开锁密码
     * @param openLockPassword 长度为6的字节数组
     */
    public void setOpenLockPassword(byte[] openLockPassword) {
        this.openLockPassword = openLockPassword;
    }

    public byte[] getEntryptKey() {
        return entryptKey;
    }

    /**
     * 设置加密密钥
     * @param entryptKey 长度为8的字节数组
     */
    public void setEntryptKey(byte[] entryptKey) {
        this.entryptKey = entryptKey;
    }

    public int getEncryptType() {
        return encryptType;
    }

    /**
     * 设置加密方式
     * @param encryptType 0：TEA加密   1：AES加密
     */
    public void setEncryptType(int encryptType) {
        this.encryptType = encryptType;
    }

    public int getAutuIncreaseNum() {
        return autuIncreaseNum;
    }

    public void setAutuIncreaseNum(int autuIncreaseNum) {
        this.autuIncreaseNum = autuIncreaseNum;
    }

    /**
     * 获取生产序列号
     * @return 6字节生产序列号
     */
    public byte[] getSerialNum() {
        if (productInfo == null) {
            return null;
        }
        return productInfo.getSerialNum();
    }

    /**
     * 设置生产序列号
     * @param serialNum 数组长度为6
     */
    public void setSerialNum(byte[] serialNum) {
        if (productInfo == null) {
            productInfo = new ProductInfo();
        }
        productInfo.setSerialNum(serialNum);
    }

    /**
     * 获取产商标识
     * @return 4字节产商标识
     */
    public byte[] getVendorId() {
        if (productInfo == null) {
            return null;
        }
        return productInfo.getVendorId();
    }

    /**
     * 设置厂商标识
     * @param vendorId 数组长度为4
     */
    public void setVendorId(byte[] vendorId) {
        if (productInfo == null) {
            productInfo = new ProductInfo();
        }
        productInfo.setVendorId(vendorId);
    }

    /**
     * 获取设备类型
     * @return 2字节设备类型
     */
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
     * 设置当前要读取的设备节点编号
     * @param deviceNum
     */
    public void setDeviceNum(byte deviceNum) {
        this.currentDeviceNum = deviceNum;
    }

    /**
     * 获取当前要读取的设备节点编号
     * @return 设备节点编号
     */
    public byte getDeviceNum() {
        return currentDeviceNum;
    }

    /**
     * 获取设备的所有节点信息
     * @return 设备节点信息Map
     */
    public Map getDeviceInfoMap() {
        return deviceInfoMap;
    }

    /**
     * 设置设备的所有节点信息
     * @param deviceInfoMap
     */
    public void setDeviceInfoMap(Map<Byte, byte[]> deviceInfoMap) {
        this.deviceInfoMap = deviceInfoMap;
    }

    /**
     * 获取指定设备节点信息
     * @param deviceNum
     * @return 设备节点信息
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

    /**
     * 获取设备的产品信息
     * @return 产品信息
     */
    public ProductInfo getProductInfo() {
        return productInfo;
    }

    /**
     * 设置设备的产品信息
     * @param productInfo
     */
    public void setProductInfo(ProductInfo productInfo) {
        this.productInfo = productInfo;
    }

    /**
     * 获取设备的版本
     * @return
     */
    public byte[] getVersion() {
        if (productInfo == null) {
            return null;
        }
        return productInfo.getVersion();
    }
}
