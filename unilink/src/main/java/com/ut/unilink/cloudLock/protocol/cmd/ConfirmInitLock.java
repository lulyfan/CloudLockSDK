package com.ut.unilink.cloudLock.protocol.cmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;

public class ConfirmInitLock extends BleCmdBase<Void>{
    private static final byte CODE = 0x2C;
    private byte[] adminPassword;

    public ConfirmInitLock(byte[] adminPassword) {
        if (adminPassword == null || adminPassword.length != 6) {
            throw new IllegalArgumentException("管理员密码不能为空并且长度必须为6");
        }

        this.adminPassword = adminPassword;
    }
    @Override
    public BleMsg build() {
        BleMsg msg = new BleMsg();
        msg.setCode(CODE);
        msg.setContent(adminPassword);
        return msg;
    }

    @Override
    Void parse(BleMsg msg) {
        return null;
    }
}
