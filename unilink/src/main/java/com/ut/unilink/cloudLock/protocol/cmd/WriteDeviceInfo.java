package com.ut.unilink.cloudLock.protocol.cmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;
import com.ut.unilink.util.Log;

import java.nio.ByteBuffer;

public class WriteDeviceInfo extends BleCmdBase<Void>{

    private static final byte CODE = 0x22;
    private byte[] openLockPassword;
    private byte deviceNum;          //设备编号
    private byte[] value;              //要写入云锁的设备数据值

    public WriteDeviceInfo(byte[] openLockPassword, byte deviceNum, byte[] value) {

        if (openLockPassword == null || openLockPassword.length != 6) {
            throw new IllegalArgumentException("开锁密码不能为null，并且长度必须为6位");
        }

        if (value == null) {
            throw new IllegalArgumentException("写入设备数据不能为空");
        }

        this.openLockPassword = openLockPassword;
        this.deviceNum = deviceNum;
        this.value = value;
    }

    @Override
    public BleMsg build() {
        BleMsg msg = new BleMsg();
        msg.setCode(CODE);
        ByteBuffer buffer = ByteBuffer.allocate(openLockPassword.length + 2 + 2);
        buffer.put(openLockPassword);
        buffer.putShort((short) autoIncreaseNum);
        buffer.put(deviceNum);
        buffer.put(value);
        msg.setContent(buffer.array());
        return msg;
    }

    @Override
    Void parse(BleMsg msg) {
        return null;
    }
}
