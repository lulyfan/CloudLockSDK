package com.ut.unilink.cloudLock.protocol.data;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class LockState {

    private static final int DEV_NUM_LOCK_STATE = 6;    //门锁状态的设备编号
    private static final int DEV_NUM_ELEC = 0;          //电量设备编号

    private String status;
    private String elect;

    public LockState() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getElect() {
        return elect;
    }

    public void setElect(String elect) {
        this.elect = elect;
    }

    @Override
    public String toString() {
        return "elect:" + elect + " status:" + status;
    }

    public static LockState parseLockState(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        byte statusNum = buffer.get();
        List<DeviceNodeInfo> deviceNodeInfoList = new ArrayList<>();

        for (int i=0; i<statusNum; i++) {
            byte devNo = buffer.get();

            DeviceNodeInfo deviceNodeInfo = new DeviceNodeInfo(devNo);
            buffer.get(deviceNodeInfo.value);
            deviceNodeInfoList.add(deviceNodeInfo);
        }

        LockState lockState = new LockState();
        for (DeviceNodeInfo deviceNodeInfo : deviceNodeInfoList) {
            switch (deviceNodeInfo.devNo) {
                case LockState.DEV_NUM_ELEC:
                    lockState.setElect(deviceNodeInfo.value[0] + "");
                    break;

                case LockState.DEV_NUM_LOCK_STATE:
                    lockState.setStatus(deviceNodeInfo.value[0] + "");
                    break;

                default:
            }
        }

        return lockState;
    }
}
