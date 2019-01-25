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

        DataAssemble.get().assembleData(getBytes("a5"));
        DataAssemble.get().assembleData(getBytes("5a"));
        DataAssemble.get().assembleData(getBytes("10 80"));
        DataAssemble.get().assembleData(getBytes("00"));
        DataAssemble.get().assembleData(getBytes("83"));
        DataAssemble.get().assembleData(getBytes("9d a3 88 8e c3 ae 2e 34 da ce 46 0a a1 8c 82 ed 26 a5"));
        DataAssemble.get().assembleData(getBytes("5a 10 80 00 83 9d a3 88 8e c3 ae 2e 34 da ce 46 0a a1"));
        DataAssemble.get().assembleData(getBytes("8c 82 ed 26 a5 5a 10 80 00 83 9d a3 88 8e c3 ae 2e 34"));
        DataAssemble.get().assembleData(getBytes("da ce 46 0a a1 8c 82 ed 26 a5 5a 10 80 00 83 9d a3 88"));
        DataAssemble.get().assembleData(getBytes("8e c3 ae 2e 34 da ce 46 0a a1 8c 82 ed 26"));
        DataAssemble.get().assembleData(getBytes("a5"));
        DataAssemble.get().assembleData(getBytes("5a"));
        DataAssemble.get().assembleData(getBytes("10 80"));
        DataAssemble.get().assembleData(getBytes("00"));
        DataAssemble.get().assembleData(getBytes("df"));
        DataAssemble.get().assembleData(getBytes("b7 b5 b1 f7 73 ae ee 0f e2 95 ec 34 ee 9e a6 ad 32"));

        byte[] data = getBytes("a5 a5 5a 20 c0 03 57 90 2a a2 bf e8 b5 dd 79" + " 68 9b 1c d7 d9 38 18 a8 0a fd ec 3a 68 56 a9 02 41 c4 04 d7 ee cf 30 d1 e0"
         + "a5 a5 5a 20 c0 03 57 90 2a a2 bf e8 b5 dd 79" + " 68 9b 1c d7 d9 38 18 a8 0a fd ec 3a 68 56 a9 02 41 c4 04 d7 ee cf 30 d1 e0"
                + "a5 5a 20 c0 03 57 90 2a a2 bf e8 b5 dd 79 68 9b 1c d7 d9 38 18 a8 0a fd ec 3a 68 56 a9 02 41 c4 04 d7 ee cf 30 d1 e0"
                + "a5 5a 20 c0 03 57 90 2a a2 bf e8 b5 dd 79 68 9b 1c d7 d9 38 18 a8 0a fd ec 3a 68 56 a9 02 41 c4 04 d7 ee cf 30 d1 e0"
                + "a5 5a 20 c0 03 57 90 2a a2 bf e8 b5 dd 79 68 9b 1c d7 d9 38 18 a8 0a fd ec 3a 68 56 a9 02 41 c4 04 d7 ee cf 30 d1 e0"
                + "a5 5a 20 c0 03 57 90 2a a2 bf e8 b5 dd 79 68 9b 1c d7 d9 38 18 a8 0a fd ec 3a 68 56 a9 02 41 c4 04 d7 ee cf 30 d1 e0"
                + "a5 5a 20 c0 03 57 90 2a a2 bf e8 b5 dd 79 68 9b 1c d7 d9 38 18 a8 0a fd ec 3a 68 56 a9 02 41 c4 04 d7 ee cf 30 d1 e0"
                + "a5 5a 20 c0 03 57 90 2a a2 bf e8 b5 dd 79 68 9b 1c d7 d9 38 18 a8 0a fd ec 3a 68 56 a9 02 41 c4 04 d7 ee cf 30 d1 e0"
                + "a5 5a 20 c0 03 57 90 2a a2 bf e8 b5 dd 79 68 9b 1c d7 d9 38 18 a8 0a fd ec 3a 68 56 a9 02 41 c4 04 d7 ee cf 30 d1 e0"
                + "a5 5a 20 c0 03 57 90 2a a2 bf e8 b5 dd 79 68 9b 1c d7 d9 38 18 a8 0a fd ec 3a 68 56 a9 02 41 c4 04 d7 ee cf 30 d1 e0"
                + "a5 5a 20 c0 03 57 90 2a a2 bf e8 b5 dd 79 68 9b 1c d7 d9 38 18 a8 0a fd ec 3a 68 56 a9 02 41 c4 04 d7 ee cf 30 d1 e0"
                + "a5 5a 20 c0 03 57 90 2a a2 bf e8 b5 dd 79 68 9b 1c d7 d9 38 18 a8 0a fd ec 3a 68 56 a9 02 41 c4 04 d7 ee cf 30 d1 e0"
                + "a5 5a 20 c0 03 57 90 2a a2 bf e8 b5 dd 79 68 9b 1c d7 d9 38 18 a8 0a fd ec 3a 68 56 a9 02 41 c4 04 d7 ee cf 30 d1 e0"
                + "a5 5a 20 c0 03 57 90 2a a2 bf e8 b5 dd 79 68 9b 1c d7 d9 38 18 a8 0a fd ec 3a 68 56 a9 02 41 c4 04 d7 ee cf 30 d1 e0"

        );
        DataAssemble.get().assembleData(data);
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
