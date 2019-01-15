package com.ut.unilink.cloudLock.protocol.data;

public abstract class DeviceNodeInfo {

    public byte devNo;
    public byte[] value;

    public DeviceNodeInfo(byte devNo) {
        this.devNo = devNo;
        value = new byte[getLength(devNo)];
    }

    //根据type得到设备状态value的数组长度
    public abstract int getLength(byte devNo);
}
