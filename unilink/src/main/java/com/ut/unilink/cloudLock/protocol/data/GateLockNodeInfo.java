package com.ut.unilink.cloudLock.protocol.data;

public class GateLockNodeInfo extends DeviceNodeInfo{

    public static final int NODE_ELECT = 0;                //电量
    public static final int NODE_KEY_EVENT = 1;            //钥匙事件
    public static final int NODE_AUTH_CONTROL = 2;         //授权控制
    public static final int NODE_AUTH_KEY_EVENT = 3;       //授权钥匙事件
    public static final int NODE_AUTH_TEMP_PASSWORD = 4;   //授权临时密码
    public static final int NODE_NORMAL_OPEN_STATE = 5;    //常开状态
    public static final int NODE_DOOR_LOCK_STATE = 6;      //门锁状态
    public static final int NODE_ALARM_STATE = 7;          //报警状态

    public GateLockNodeInfo(byte devNo) {
        super(devNo);
    }

    @Override
    public int getLength(byte devNo) {
        int length = 0;

        switch (devNo) {
            case NODE_ELECT:
            case NODE_KEY_EVENT:
            case NODE_AUTH_CONTROL:
            case NODE_AUTH_KEY_EVENT:
            case NODE_AUTH_TEMP_PASSWORD:
            case NODE_NORMAL_OPEN_STATE:
            case NODE_DOOR_LOCK_STATE:
            case NODE_ALARM_STATE:
                length = 1;
                break;

            default:
        }

        return length;
    }
}
