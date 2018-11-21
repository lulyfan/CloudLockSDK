package com.ut.unilink.util;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class AESTest {

    @Test
    public void encrypt() {
        byte[] key = new byte[] {-80, -39, -80, 14, -21, -111, -31, 102};
        byte[] data = new byte[] {0x2f, (byte) 0xce, (byte) 0x91, (byte) 0xc5, 0x10, (byte) 0xcd, (byte) 0xc6, 0x3c, (byte) 0xd1, (byte) 0xdd, (byte) 0xe6, (byte) 0xee, (byte) 0x9f, (byte) 0x8f, (byte) 0xc9, (byte) 0x92};

//        Random random = new Random();
//        random.nextBytes(data);
//        random.nextBytes(key);

        System.out.println(toUnsignedHexString(data));
        System.out.println(toUnsignedHexString(key));

        byte[] encryptData = data;
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