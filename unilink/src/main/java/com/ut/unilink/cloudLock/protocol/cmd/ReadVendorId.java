package com.ut.unilink.cloudLock.protocol.cmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;

import java.nio.ByteBuffer;

public class ReadVendorId extends BleCmdBase<ReadVendorId.Data> {

    private static final byte CODE = 0x2A;

    @Override
    public BleMsg build() {
        BleMsg msg = new BleMsg();
        msg.setCode(CODE);
        msg.setEncryptType(BleMsg.ENCRYPT_TYPE_FIXED);
        return msg;
    }

    @Override
    Data parse(BleMsg msg) {
        ByteBuffer buffer = ByteBuffer.wrap(msg.getContent());
        Data data = new Data();
        buffer.get(data.vendorId);
        buffer.get(data.deviceType);
        return data;
    }

    public static class Data {
        public byte[] vendorId = new byte[4];
        public byte[] deviceType = new byte[2];
    }
}
