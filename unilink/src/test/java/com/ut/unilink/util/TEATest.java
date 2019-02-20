package com.ut.unilink.util;

import com.ut.unilink.cloudLock.protocol.TeaEncrypt;

import org.junit.Test;

public class TEATest {

    @Test
    public void test() {



        byte[] data = getBytes("5df7a30900c6f2d0917666a5ecb922837761ce3015d8c8cc988c931fc1587087");


        byte[] key = new byte[]{0x14, 0x18, (byte) 0x82, 0x02, (byte) 0xE9, 0x6B, (byte) 0x88, (byte) 0xAD,
                (byte) 0xFF, 0x0C, 0x11, 0x79, (byte) 0xAF, 0x39, 0x5B, (byte) 0xEE};
        TeaEncrypt teaEncrypt = new TeaEncrypt(key);

        byte[] encryptData = teaEncrypt.encrypt(data);
        System.out.println(Log.toUnsignedHex(encryptData));
        System.out.println(Log.toUnsignedHex(teaEncrypt.decrypt(encryptData)));
    }

    @Test
    public void encrypt() {
        byte[] data = getBytes("22 10 c0 96 e8 6b 4a 3a 00 01 01 00");
        byte[] key = getBytes("78 72 8b b8 69 00 70 80");
        TeaEncrypt teaEncrypt = new TeaEncrypt(key);
        System.out.println(Log.toUnsignedHex(teaEncrypt.encrypt(data), ""));
    }

    @Test
    public void decrypt() {
//        byte[] data = getBytes("5df7a30900c6f2d0917666a5ecb922837761ce3015d8c8cc988c931fc1587087");
        byte[] data = getBytes("412ee6 a0ab2d5a 08");
        byte[] key = getBytes("a3be0ff0 8a1ed026");

//        byte[] key = new byte[]{0x14, 0x18, (byte) 0x82, 0x02, (byte) 0xE9, 0x6B, (byte) 0x88, (byte) 0xAD,
//                (byte) 0xFF, 0x0C, 0x11, 0x79, (byte) 0xAF, 0x39, 0x5B, (byte) 0xEE};
        TeaEncrypt teaEncrypt = new TeaEncrypt(key);
        System.out.println(Log.toUnsignedHex(teaEncrypt.decrypt(data)));
    }

    private byte[] getBytes(String data) {
        data = data.replaceAll(" ", "");

        int length = data.length() / 2;
        byte[] result = new byte[length];

        for (int i=0, j=0; i<length; i++, j+=2) {
            String item = data.substring(j, j+2);
            result[i] = (byte) Integer.parseInt(item, 16);
        }

//        System.out.println(Log.toUnsignedHex(result, ""));
        return result;
    }
}
