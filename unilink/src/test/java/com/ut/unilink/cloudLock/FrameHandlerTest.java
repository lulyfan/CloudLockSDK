package com.ut.unilink.cloudLock;

import com.ut.unilink.util.Log;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class FrameHandlerTest {

    @Test
    public void test() {
        FrameHandler.setFrameSize(200);
        FrameHandler frameHandler = new FrameHandler();

        byte[] data = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
                                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
                                0, 1, 30};
        List<byte[]> result =  frameHandler.handleSend(data);

        System.out.println("分包：");
        for (byte[] item : result) {
            System.out.println(toUnsignedHexString(item));
        }

        System.out.println("\n组包：");
        for (byte[] item : result) {
            byte[] temp = frameHandler.handleReceive(item);
            System.out.println(toUnsignedHexString(temp));
        }
    }

    @Test
    public void handleSend() {
        FrameHandler frameHandler = new FrameHandler();

        byte[] data = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 30};
        List<byte[]> result =  frameHandler.handleSend(data);

        for (byte[] item : result) {
            System.out.println(toUnsignedHexString(item));
        }
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

    @Test
    public void handleReceive() {
        FrameHandler.setFrameSize(20);
        FrameHandler frameHandler = new FrameHandler();

        byte[] d1 = getBytes("a5 5a 20 80 0a 7d 3d 8f 9f eb e7 e3 4b f9 b3 ef d6 6a d3 d0");
        byte[] d2 = getBytes("a5 5a 20 81 0a 7a 84 7c 76 53 b9 d0 41 9c b4 5c 26 45 a6 35");
        byte[] d3 = getBytes("a5 5a 20 83 0a fa f2 c9 81 6a bd d3 03");
//        byte[] d2 = getBytes("a55a20c1017d1e8dc0cac3139e579806e10dc8d5");
//        byte[] d4 = getBytes("a55a20c2014d69a5848576450c");

        System.out.println(toUnsignedHexString(frameHandler.handleReceive(d1)));
        System.out.println(toUnsignedHexString(frameHandler.handleReceive(d2)));
        System.out.println(toUnsignedHexString(frameHandler.handleReceive(d3)));
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