package com.ut.unilink.cloudLock.protocol.data;

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

    private byte[] version = new byte[3];
    private byte[] serialNum = new byte[6];
    private byte[] vendorId = new byte[4];       //厂商标识

    @Override
    public String toString() {
        return "version:V" + Log.toUnsignedHex(version, ".")
                 + " serialNum:" + Log.toUnsignedHex(serialNum, "")
                 + " vendorId:" + Log.toUnsignedHex(vendorId, "");
    }

    public byte[] getVersion() {
        return version;
    }

    public void setVersion(byte[] version) {
        this.version = version;
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
}
