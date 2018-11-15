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
        msg.setEncrypt(false);

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
        data.result = buffer.get();
        data.versionNum = buffer.get();
        data.editionNum = buffer.get();
        data.realeaseNum = buffer.get();
        return data;
    }

    private void init() {
        Random random = new Random();
        random.nextBytes(adminPassword);
        random.nextBytes(openLockPassword);
        random.nextBytes(secretKey);
//        encryptVersion = (byte) random.nextInt(1);
        encryptVersion = 1;
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
        public byte result;           //设置结果，1成功，0失败
        public byte versionNum;       //版本号
        public byte editionNum;       //版次号
        public byte realeaseNum;      //发布号
    }
}
