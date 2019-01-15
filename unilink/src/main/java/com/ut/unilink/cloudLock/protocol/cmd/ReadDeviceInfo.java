package com.ut.unilink.cloudLock.protocol.cmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;

import java.nio.ByteBuffer;

public class ReadDeviceInfo extends BleCmdBase<ReadDeviceInfo.Data>{

    private static final byte CODE = 0x23;
    private byte deviceNum;

    public ReadDeviceInfo(byte deviceNum) {
        this.deviceNum = deviceNum;
    }

    @Override
    public BleMsg build() {
        BleMsg msg = new BleMsg();
        msg.setCode(CODE);
        ByteBuffer content = ByteBuffer.allocate(2 + 1);
        content.putShort((short) autoIncreaseNum);
        content.put(deviceNum);
        msg.setContent(content.array());
        return msg;
    }

    @Override
    public Data parse(BleMsg msg) {
        ByteBuffer buffer = ByteBuffer.wrap(msg.getContent());
        Data data = new Data();
        data.deviceNum = buffer.get();
        data.value = new byte[buffer.remaining()];
        buffer.get(data.value);
        return data;
    }

    public static class Data {
        public byte deviceNum;
        public byte[] value;
    }
}
