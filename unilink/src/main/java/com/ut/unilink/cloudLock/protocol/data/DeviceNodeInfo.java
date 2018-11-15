package com.ut.unilink.cloudLock.protocol.data;

public class DeviceNodeInfo {

    //设备节点编号
    public static final int ELECT = 0;
    public static final int LOCK_CONTROL = 1;
    public static final int LOCK_STATE = 2;

    public byte devNo;
    public byte[] value;

    public DeviceNodeInfo(byte devNo) {
        this.devNo = devNo;
        value = new byte[getLength(devNo)];
    }

    //根据type得到设备状态value的数组长度
    public static int getLength(byte devNo) {

        int length = 0;

        switch (devNo) {
            case ELECT:
            case LOCK_CONTROL:
            case LOCK_STATE:
                length = 1;
                break;

                default:
        }

        return length;
    }
}
