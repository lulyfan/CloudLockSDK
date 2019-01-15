package com.ut.unilink.cloudLock.protocol.data;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CloudLockState extends LockState{

    private String status;
    private int elect;

    public CloudLockState() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getElect() {
        return elect;
    }

    public void setElect(int elect) {
        this.elect = elect;
    }

    @Override
    public String toString() {
        return "elect:" + elect + " status:" + status;
    }

    public void getLockState(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        byte statusNum = buffer.get();
        List<DeviceNodeInfo> deviceNodeInfoList = new ArrayList<>();

        for (int i=0; i<statusNum; i++) {
            byte devNo = buffer.get();

            CloudLockNodeInfo deviceNodeInfo = new CloudLockNodeInfo(devNo);
            buffer.get(deviceNodeInfo.value);
            deviceNodeInfoList.add(deviceNodeInfo);
        }

        for (DeviceNodeInfo deviceNodeInfo : deviceNodeInfoList) {
            switch (deviceNodeInfo.devNo) {
                case CloudLockNodeInfo.NODE_ELECT:
                    setElect(deviceNodeInfo.value[0]);
                    break;

                default:
            }
        }
    }
}
