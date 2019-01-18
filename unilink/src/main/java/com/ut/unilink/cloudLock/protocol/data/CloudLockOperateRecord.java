package com.ut.unilink.cloudLock.protocol.data;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CloudLockOperateRecord {

    private byte[] operateTime = new byte[4];   //操作时间
    private byte motorControl;  //电机控制
    private byte doorState;    //门磁状态

    public byte[] getOperateTimeBytes() {
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

    public long getOperateTime() {
        ByteBuffer buffer = ByteBuffer.wrap(operateTime);
        long time = buffer.getInt() * 1000L;
        return time;
    }

    @Override
    public String toString() {
        long operateTime = getOperateTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String openLockTime = simpleDateFormat.format(new Date(operateTime));
        return "开锁时间:" + openLockTime +
                "\n电机控制:" + String.format("%02x", motorControl) +
                "\n门磁状态:" + String.format("%02x", doorState);
    }
}
