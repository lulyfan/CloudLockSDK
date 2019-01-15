package com.ut.unilink.cloudLock.protocol.cmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;

import java.nio.ByteBuffer;

public class ReadDeviceMutilInfo extends BleCmdBase<ReadDeviceMutilInfo.Data>{

    private static final byte CODE = 0x24;

    @Override
    public BleMsg build() {
        BleMsg msg = new BleMsg();
        msg.setCode(CODE);
        ByteBuffer content = ByteBuffer.allocate(2);
        content.putShort((short) autoIncreaseNum);
        msg.setContent(content.array());
        return msg;
    }

    @Override
    public Data parse(BleMsg msg) {
        ByteBuffer buffer = ByteBuffer.wrap(msg.getContent());
        Data data = new Data();
        data.deviceNodeCount = buffer.get();
        data.deviceValues = new byte[buffer.remaining()];
        buffer.get(data.deviceValues);
        return data;
    }

    public static class Data {
        public byte deviceNodeCount;
        public byte[] deviceValues;
    }


}
