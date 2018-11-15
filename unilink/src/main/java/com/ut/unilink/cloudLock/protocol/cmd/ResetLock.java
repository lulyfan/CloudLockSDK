package com.ut.unilink.cloudLock.protocol.cmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;

import java.nio.ByteBuffer;

public class ResetLock extends BleCmdBase<ResetLock.Data>{

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
    Data parse(BleMsg msg) {
        ByteBuffer buffer = ByteBuffer.wrap(msg.getContent());
        Data data = new Data();
        data.result = buffer.get();
        data.errCode = buffer.get();
        return data;
    }

    public static class Data {
        public byte result;
        public byte errCode;

        public boolean isSuccess() {
            return result == 1 ? true : false;
        }
    }
}
