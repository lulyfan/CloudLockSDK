package com.ut.unilink.util;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Tea每次加密8字节数据，不足8字节在后面填充0x00, 密钥为16字节，加密轮数建议32轮
 * 加密参数均为无符号数，采用小端存储模式（为了与硬件兼容）
 */
public class MyTea {

    public static byte[] encrypt(String data, byte[] password) {
        try {
            return encrypt(data.getBytes("utf-8"), password);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] encrypt(String data, String password) {
        try {
            return encrypt(data.getBytes("utf-8"), password);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] encrypt(byte[] data, String password) {
        try {
            return encrypt(data, password.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] encrypt(byte[] data, byte[] password) {
        if (data == null || password == null) {
            return null;
        }

        byte[] key = new byte[16];
        int length = Math.min(password.length, 16);
        System.arraycopy(password, 0, key, 0, length);

        int n = 8 - data.length % 8;//若data的字节数不足8的倍数,需要填充的位数
        if (n == 8) {
            n = 0;
        }

        ByteBuffer src = ByteBuffer.allocate(n + data.length);
        ByteBuffer encryptResult = ByteBuffer.allocate(n + data.length);
        src.put(data);
        if (n > 0) {
            byte[] fillArray = new byte[n];
            src.put(fillArray);
        }

        src.flip();
        for (int i = 0; i < src.capacity() / 8; i++) {
            byte[] temp = new byte[8];
            src.get(temp);

            byte[] result = encrypt(temp, key, 32);
            encryptResult.put(result);
        }

        return encryptResult.array();
    }

    public static byte[] decrypt(byte[] data, byte[] password) {
        if (data == null || data.length % 8 != 0) {
            return null;
        }

        if (password == null) {
            return null;
        }

        byte[] key = new byte[16];
        int length = Math.min(password.length, 16);
        System.arraycopy(password, 0, key, 0, length);

        ByteBuffer src = ByteBuffer.wrap(data);
        ByteBuffer decryptResult = ByteBuffer.allocate(data.length);

        for (int i = 0; i < data.length / 8; i++) {
            byte[] temp = new byte[8];
            src.get(temp);

            byte[] result = decrypt(temp, key, 32);
            decryptResult.put(result);
        }

        return decryptResult.array();
    }

    //参数为8字节的明文输入和16字节的密钥，输出8字节密文
    public static byte[] encrypt(byte[] data, byte[] key, int encryCount) {

        if (data == null || data.length != 8) {
            return null;
        }

        if (key == null || key.length != 16) {
            return null;
        }

        long[] datas = transform(data);
        long left = datas[0];
        long right = datas[1];

        long[] keys = transform(key);
        long a = keys[0];
        long b = keys[1];
        long c = keys[2];
        long d = keys[3];

        long sum = 0, delta = 0x9E3779B9;

        // 明文输入被分为左右两部分，密钥分为四部分存入寄存器，n表示加密轮数推荐32。Delta为一常数。

        int y = (int) left;
        int z = (int) right;

//        Log.i("tea", "left:" + left + " right:" + right);
//        Log.i("tea", "a:" + a + " b:" + b + " c:" + c + " d:" + d);

        for (int i = 0; i < encryCount; i++) {
            sum += delta;
            y += ((z << 4) + a) ^ (z + sum) ^ ((z >>> 5) + b);
            z += ((y << 4) + c) ^ (y + sum) ^ ((y >>> 5) + d);
        }

        return reverseTransform(y, z);
    }

    private static long[] transform(byte[] data) {

        int n = data.length / 4;
        long[] keys = new long[n];

        for (int i=0; i<n; i++) {
            byte[] temp = new byte[4];
            System.arraycopy(data, i * 4, temp, 0, 4);

            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.put(temp);

            buffer.flip();
            keys[i] = buffer.getInt() & 0xFFFFFFFFL;
        }

        return keys;
    }

    private static byte[] reverseTransform(int... data) {
        ByteBuffer buffer = ByteBuffer.allocate(4 * data.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        for (int item : data) {
            buffer.putInt(item);
        }
        return buffer.array();
    }

    public static byte[] decrypt(byte[] data, byte[] key, int encryptCount) {

        if (data == null || data.length != 8) {
            return null;
        }

        if (key == null || key.length != 16) {
            return null;
        }

        long[] datas = transform(data);
        long left = datas[0];
        long right = datas[1];

        long[] keys = transform(key);
        long a = keys[0];
        long b = keys[1];
        long c = keys[2];
        long d = keys[3];

        long sum = 0xC6EF3720, i; /* set up */
        long delta = 0x9e3779b9; /* a key schedule constant */

        int y = (int) left;
        int z = (int) right;

        for (i = 0; i < encryptCount; i++) { /* basic cycle start */
            z -= ((y << 4) + c) ^ (y + sum) ^ ((y >>> 5) + d);
            y -= ((z << 4) + a) ^ (z + sum) ^ ((z >>> 5) + b);
            sum -= delta; /* end cycle */
        }

        return reverseTransform(y, z);
    }
}
