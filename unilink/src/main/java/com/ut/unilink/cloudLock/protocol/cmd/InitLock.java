package com.ut.unilink.cloudLock.protocol.cmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;
import com.ut.unilink.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

public class InitLock extends BleCmdBase<InitLock.Data>{

    private static final byte CODE = 0x20;
    private byte[] adminPassword = new byte[6];
    private byte[] openLockPassword = new byte[6];
    private byte[] secretKey = new byte[8];     //密钥
    private byte encryptVersion;         //加密版本

    @Override
    public BleMsg build() {
        init();
        BleMsg msg = new BleMsg();
        msg.setCode(CODE);
        msg.setEncryptType(BleMsg.ENCRYPT_TYPE_FIXED);

        int contentLength = adminPassword.length + openLockPassword.length + secretKey.length + 1 + 2;
        ByteBuffer buffer = ByteBuffer.allocate(contentLength);
        buffer.put(adminPassword);
        buffer.put(openLockPassword);
        buffer.put(encryptVersion);
        buffer.put(secretKey);
        buffer.putShort((short) autoIncreaseNum);
        msg.setContent(buffer.array());

        return msg;
    }

    @Override
    Data parse(BleMsg msg) {
        byte[] content = msg.getContent();
        ByteBuffer buffer = ByteBuffer.wrap(content);
        Data data = new Data();
        buffer.get(data.version);
        return data;
    }

    private void init() {
        Random random = new Random();
        random.nextBytes(adminPassword);
        random.nextBytes(openLockPassword);
        random.nextBytes(secretKey);
        encryptVersion = (byte) random.nextInt(1);
    }

    public byte[] getAdminPassword() {
        return adminPassword;
    }

    public byte[] getOpenLockPassword() {
        return openLockPassword;
    }

    public byte[] getSecretKey() {
        return secretKey;
    }

    public byte getEncryptVersion() {
        return encryptVersion;
    }

    public static class Data {
       public byte[] version = new byte[3];
    }
}
