package com.ut.unilink.cloudLock.protocol.data;

public class CloudLockNodeInfo extends DeviceNodeInfo{

    //设备节点编号
    public static final int NODE_ELECT = 0;
    public static final int NODE_LOCK_CONTROL = 1;
    public static final int NODE_LOCK_STATE = 2;

    public CloudLockNodeInfo(byte devNo) {
        super(devNo);
    }

    //根据type得到设备状态value的数组长度
    public int getLength(byte devNo) {

        int length = 0;

        switch (devNo) {
            case NODE_ELECT:
            case NODE_LOCK_CONTROL:
            case NODE_LOCK_STATE:
                length = 1;
                break;

            default:
        }

        return length;
    }
}
