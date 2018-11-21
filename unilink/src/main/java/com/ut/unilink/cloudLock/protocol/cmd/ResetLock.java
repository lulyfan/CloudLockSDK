package com.ut.unilink.cloudLock.protocol.cmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;

import java.nio.ByteBuffer;

public class ResetLock extends BleCmdBase<Void>{

    private static final byte CODE = 0x21;
    private byte[] adminPassword;

    public ResetLock(byte[] adminPassword) {

        if (adminPassword == null || adminPassword.length != 6) {
            throw new IllegalArgumentException("管理员密码不能为空, 并且长度必须为6位");
        }

        this.adminPassword = adminPassword;
    }

    @Override
    public BleMsg build() {
        BleMsg msg = new BleMsg();
        msg.setCode(CODE);
        ByteBuffer buffer = ByteBuffer.allocate(adminPassword.length + 2);
        buffer.put(adminPassword);
        buffer.putShort((short) autoIncreaseNum);
        msg.setContent(buffer.array());
        return msg;
    }

    @Override
    Void parse(BleMsg msg) {
        return null;
    }

}
