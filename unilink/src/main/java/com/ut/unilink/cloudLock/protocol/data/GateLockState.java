package com.ut.unilink.cloudLock.protocol.data;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class GateLockState extends LockState{

    private int elect;

    public int getElect() {
        return elect;
    }

    public void setElect(int elect) {
        this.elect = elect;
    }

    @Override
    public void getLockState(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        byte statusNum = buffer.get();
        List<DeviceNodeInfo> deviceNodeInfoList = new ArrayList<>();

        for (int i=0; i<statusNum; i++) {
            byte devNo = buffer.get();

            GateLockNodeInfo deviceNodeInfo = new GateLockNodeInfo(devNo);
            buffer.get(deviceNodeInfo.value);
            deviceNodeInfoList.add(deviceNodeInfo);
        }

        for (DeviceNodeInfo deviceNodeInfo : deviceNodeInfoList) {
            switch (deviceNodeInfo.devNo) {
                case GateLockNodeInfo.NODE_ELECT:
                    setElect(deviceNodeInfo.value[0]);
                    break;

                default:
            }
        }
    }
}
