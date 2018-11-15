package com.ut.unilink.cloudLock;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class FrameHandlerTest {

    @Test
    public void test() {
        FrameHandler frameHandler = new FrameHandler();
        byte[] data = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
                1, 2, 3,
                30};
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
        FrameHandler frameHandler = new FrameHandler();

        byte[] d4 = new byte[]{(byte) 0xa5, 0x5a, 0x08, (byte) 0x80, 0x07, (byte) 0xb3, (byte) 0x97, 0x35, (byte) 0x91, (byte) 0xa9, (byte) 0x96, 0x6a, (byte) 0xa3, 0x53, 0x59};
        System.out.println(toUnsignedHexString(frameHandler.handleReceive(d4)));

//        byte[] d1 = new byte[]{(byte) 0xa5, 0x5a, 0x0e, 0x00, 0x1e, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x26, 0x64};
//        byte[] d2 = new byte[]{(byte) 0xa5, 0x5a, 0x0e, 0x01, 0x1e, 0x0e, 0x54, (byte) 0xd8};
//
//        System.out.println(toUnsignedHexString(frameHandler.handleReceive(d1)));
//        System.out.println(toUnsignedHexString(frameHandler.handleReceive(d2)));


    }
}