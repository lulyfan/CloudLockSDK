package com.ut.unilink.util;

import com.ut.unilink.cloudLock.protocol.TeaEncrypt;

import org.junit.Test;

public class TEATest {

    @Test
    public void test() {
        byte[] data = new byte[]{(byte) 0x8a, 0x0c, (byte) 0xcc, 0x0e, 0x4f, 0x63, 0x15, 0x4f, 0x43, 0x0f,
                (byte) 0x97, (byte) 0xb0, (byte) 0xe5, (byte) 0xef, 0x52, (byte) 0x99};
        byte[] key = new byte[]{0x33, 0x75, 0x7f, 0x39, (byte) 0xb6, (byte) 0xba, (byte) 0xd6, (byte) 0xe6};
        TeaEncrypt teaEncrypt = new TeaEncrypt(key);
        System.out.println(Log.toUnsignedHex(teaEncrypt.decrypt(data)));
    }
}
