package com.ut.unilink.cloudLock.protocol.data;

public class BleLockState {

    public static final int DEV_NUM_LOCK_STATE = 6;    //门锁状态的设备编号
    public static final int DEV_NUM_ELEC = 0;          //电量设备编号

    private String status;
    private String elect;

    public BleLockState(String status, String elect, int alarm) {
        this.status = status;
        this.elect = elect;
    }

    public BleLockState() {
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
}
