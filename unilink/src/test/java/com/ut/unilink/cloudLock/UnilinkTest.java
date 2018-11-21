package com.ut.unilink.cloudLock;

import com.ut.unilink.cloudLock.protocol.BleClient;
import com.ut.unilink.cloudLock.protocol.ClientHelper;
import com.ut.unilink.cloudLock.protocol.cmd.BleCallBack;
import com.ut.unilink.cloudLock.protocol.cmd.WriteDeviceInfo;

import org.junit.Test;

import java.util.List;

public class UnilinkTest {

    @Test
    public void test() {
        BleClient client = new BleClient("", connectionManager);
        ClientHelper clientHelper = new ClientHelper(client);
        byte[] key = {1, 2, 3, 4, 5, 6, 7, 8};
//        clientHelper.setEncrypt(new TeaEncrypt(key));

        byte[] openLockPassword = new byte[]{1, 2, 3, 4, 5, 6};
        WriteDeviceInfo openLock = new WriteDeviceInfo(openLockPassword, (byte) 1, new byte[]{1});
        openLock.setClientHelper(clientHelper);
        openLock.sendMsg(new BleCallBack<Void>() {
            @Override
            public void success(Void result) {
            }

            @Override
            public void fail(int errCode, String errMsg) {
            }

            @Override
            public void timeout() {
                System.out.println("send 3 times timeout");
            }
        });

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private IConnectionManager connectionManager = new IConnectionManager() {
        @Override
        public void onConnect(String address) {

        }

        @Override
        public void onDisConnect(String address, int code) {

        }

        @Override
        public void onReceive(String address, byte[] data) {

        }

        @Override
        public void send(String address, byte[] data) {
            System.out.println("原始报文：\n" + toUnsignedHexString(data));
            FrameHandler frameHandler = new FrameHandler();
            List<byte[]> datas = frameHandler.handleSend(data);

            System.out.println("分包：");
            for (byte[] item : datas) {
                System.out.println(toUnsignedHexString(item));
            }

            System.out.println("组包：");
            for (byte[] item : datas) {
                System.out.println(toUnsignedHexString(frameHandler.handleReceive(item)) + "\n");
            }

        }
    };

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
