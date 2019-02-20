package com.ut.unilink.cloudLock.protocol.data;

import com.ut.unilink.util.BitUtil;
import com.ut.unilink.util.Log;

public class ProductInfo {
//    public byte versionNum;       //版本号   BCD码表示
//    public byte editionNum;       //版次号   BCD码表示
//    public byte realeaseNum;      //发布号   BCD码表示
//
//    //生产序列号
//    public byte serialId;
//    public byte year;
//    public byte month;
//    public byte pipelineHigh;     //流水线高位
//    public byte pipelineMedian;   //流水线中位
//    public byte pipelineLow;      //流水线低位

    private byte[] softwareVersion = new byte[3];  //软件版本
    private byte[] protocolVersion = new byte[2];  //规约版本
    private byte[] serialNum = new byte[6];
    private byte[] vendorId = new byte[4];       //厂商标识
    private int deviceType;

    @Override
    public String toString() {
        return "softwareVersion:V" + Log.toUnsignedHex(softwareVersion, ".")
                + " protocolVersion:V" + Log.toUnsignedHex(protocolVersion, ".")
                 + " serialNum:" + Log.toUnsignedHex(serialNum, "")
                 + " vendorId:" + Log.toUnsignedHex(vendorId, "");
    }

    public byte[] getVersionBytes() {
        return softwareVersion;
    }

    public String getSoftwareVersion() {
//        String version = "V";
//        version += BitUtil.bits(softwareVersion[0], 0, 4) + ".";
//        version += BitUtil.bits(softwareVersion[1], 4, 4) * 10 + BitUtil.bits(softwareVersion[1], 0, 4) + ".";
//        version += BitUtil.bits(softwareVersion[2], 4, 4) * 10 + BitUtil.bits(softwareVersion[2], 0, 4);
        return "V" + Log.toUnsignedHex(softwareVersion, ".").substring(1);
    }

    public String getProtocolVersion() {
//        String version = "V";
//        version += BitUtil.bits(protocolVersion[0], 0, 4) + ".";
//        version += BitUtil.bits(protocolVersion[1], 4, 4) * 10 + BitUtil.bits(protocolVersion[1], 0, 4);
        return "V" + Log.toUnsignedHex(protocolVersion, ".").substring(1);
    }

    public void setSoftwareVersion(byte[] softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public byte[] getProtocolVersionBytes() {
        return protocolVersion;
    }

    public void setProtocolVersion(byte[] protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public byte[] getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(byte[] serialNum) {
        this.serialNum = serialNum;
    }

    public byte[] getVendorId() {
        return vendorId;
    }

    public void setVendorId(byte[] vendorId) {
        this.vendorId = vendorId;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }
}
