package com.ut.unilink.cloudLock.protocol;

import com.ut.unilink.cloudLock.protocol.cmd.InitLock;
import com.ut.unilink.cloudLock.protocol.cmd.ResetLock;
import com.ut.unilink.cloudLock.protocol.cmd.WriteDeviceInfo;

import org.junit.Test;

public class BleMsgTest {

    @Test
    public void testInitLock() {
        InitLock initLock = new InitLock(null);
        BleMsg msg = initLock.build();
        printf(msg);
    }

    @Test
    public void testOpenLock() {
        byte[] openLockPassword = new byte[]{1, 2, 3, 4, 5, 6};
        WriteDeviceInfo writeDeviceInfo = new WriteDeviceInfo(openLockPassword, (byte) 1, new byte[]{1});
        BleMsg msg = writeDeviceInfo.build();
        printf(msg);
    }

    @Test
    public void testResetLock() {
        byte[] adminPassword = new byte[]{1, 2, 3, 4, 5, 6};
        ResetLock resetLock = new ResetLock(adminPassword);
        BleMsg msg = resetLock.build();
        printf(msg);
    }

    private void printf(BleMsg msg) {
        byte[] key = new byte[]{0, 1, 3, 4, 5, 6, 7, 8};

        System.out.println("未加密：" + toUnsignedHexString(msg.encode()));
        msg.setEntrypt(new TeaEncrypt(key));
        System.out.println("加密后：" + toUnsignedHexString(msg.encode()));

        BleMsg result = BleMsg.decode(msg.encode(), new TeaEncrypt(key));
        result.setEntrypt(BleMsg.NO_ENCRYPT);
        System.out.println("解密后：" + toUnsignedHexString(result.encode()));
    }

    @Test
    public void encode() {

        InitLock initLock = new InitLock(null);
        BleMsg msg = initLock.build();
        byte[] key = new byte[]{0, 1, 3, 4, 5, 6, 7, 8};


        System.out.println("random create ------" + "\n"
                + "adminPassword:" + toUnsignedHexString(initLock.getAdminPassword()) + "\n"
                + "openLockPassword" + toUnsignedHexString(initLock.getOpenLockPassword()) + "\n"
                + "secretKey:" + toUnsignedHexString(initLock.getSecretKey()) + "\n"
                + "encryptVersion:" + initLock.getEncryptVersion());

        System.out.println(toUnsignedHexString(msg.encode()));
        msg.setEntrypt(new TeaEncrypt(key));
        System.out.println(toUnsignedHexString(msg.encode()));
    }

    @Test
    public void decode() {
        byte[] key = new byte[]{0, 1, 3, 4, 5, 6, 7, 8};
        byte[] data = new byte[]{(byte) 0xf2, 0x02, 0x34, (byte) 0x9f, (byte) 0xdd, 0x6b, 0x56, (byte) 0x9f, (byte) 0xff, (byte) 0xc6, (byte) 0xfc, 0x7c, 0x4a, (byte) 0x96, (byte) 0xeb, 0x5c, (byte) 0xe7, 0x2c, (byte) 0xc9, 0x4b, 0x1e, 0x56, (byte) 0xf8, (byte) 0xc2, (byte) 0xd5, (byte) 0xb3, (byte) 0xba, (byte) 0x9d, 0x64, (byte) 0xfe, 0x37, 0x55};
        BleMsg msg = BleMsg.decode(data, new TeaEncrypt(key));
        System.out.println(toUnsignedHexString(msg.getContent()));
    }

    private String toUnsignedHexString(byte[] data) {

        if (data == null) {
            return "null";
        }

        String result = "";
        for(int i=0; i<data.length; i++) {
            result += ("0x" + String.format("%02x", data[i] & 0xFF) + ", ");
        }

        return result;
    }
}