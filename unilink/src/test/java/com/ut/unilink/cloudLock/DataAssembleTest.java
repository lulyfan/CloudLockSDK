package com.ut.unilink.cloudLock;

import com.ut.unilink.jobluetooth.DataAssemble;
import com.ut.unilink.util.Log;

import org.junit.Test;

public class DataAssembleTest {

    @Test
    public void test() {
        DataAssemble.get().setReceiveCallback(new DataAssemble.ReceiveCallback() {
            @Override
            public void onReceiveSuccess(byte[] data) {
                System.out.println(Log.toUnsignedHex(data));
            }
        });

        byte[] data = getBytes("a5 5a 20 c0 03 57 90 2a a2 bf e8 b5 dd 79 68 9b 1c d7 d9 38 18 a8 0a fd ec 3a 68 56 a9 02 41 c4 04 d7 ee cf 30 d1 e0"
         + "a5 5a 20 c0 03 57 90 2a a2 bf e8 b5 dd 79" + " 68 9b 1c d7 d9 38 18 a8 0a fd ec 3a 68 56 a9 02 41 c4 04 d7 ee cf 30 d1 e0");
        System.out.println(Log.toUnsignedHex(data));
        DataAssemble.get().receiveData(data);
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
