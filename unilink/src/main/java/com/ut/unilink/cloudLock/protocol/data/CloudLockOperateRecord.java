package com.ut.unilink.cloudLock.protocol.data;

public class CloudLockOperateRecord {

    private byte[] operateTime = new byte[4];   //操作时间
    private byte motorControl;  //电机控制
    private byte doorState;    //门磁状态

    public byte[] getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(byte[] operateTime) {
        this.operateTime = operateTime;
    }

    public byte getMotorControl() {
        return motorControl;
    }

    public void setMotorControl(byte motorControl) {
        this.motorControl = motorControl;
    }

    public byte getDoorState() {
        return doorState;
    }

    public void setDoorState(byte doorState) {
        this.doorState = doorState;
    }
}
