package com.ut.unilink.util;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class AESTest {

    @Test
    public void encrypt() {
        byte[] data = new byte[] {0x44, (byte) 0x88, 0x65, 0x0d, (byte) 0xc1, (byte) 0xa7, (byte) 0xa4, (byte) 0xc2, 0x38, (byte) 0x8e, 0x52, (byte) 0xa4, 0x35, (byte) 0xf8, 0x79, 0x2f};
        byte[] key = new byte[] {-63, 35, 5, 91, 54, 20, -118, -106};

//        Random random = new Random();
//        random.nextBytes(data);
//        random.nextBytes(key);

        System.out.println(toUnsignedHexString(data));
        System.out.println(toUnsignedHexString(key));

        byte[] encryptData = AES.encrypt(data, key);
        System.out.println(toUnsignedHexString(encryptData));
        System.out.println(toUnsignedHexString(AES.decrypt(encryptData, key)));
    }

    @Test
    public void decrypt() {
    }

    private String toUnsignedHexString(byte[] data) {

        if (data == null) {
            return "null";
        }

        String result = "";
        for(int i=0; i<data.length; i++) {
            result += (String.format("%02x", data[i] & 0xFF) + " ");
        }

        return result;
    }
}