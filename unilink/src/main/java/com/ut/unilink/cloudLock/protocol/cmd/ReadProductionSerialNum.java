package com.ut.unilink.cloudLock.protocol.cmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;

public class ReadProductionSerialNum extends BleCmdBase<ReadProductionSerialNum.Data>{

    private static final byte CODE = 0x28;

    @Override
    public BleMsg build() {
        BleMsg msg = new BleMsg();
        msg.setCode(CODE);
        msg.setEncryptType(BleMsg.ENCRYPT_TYPE_FIXED);
        return msg;
    }

    @Override
    public Data parse(BleMsg msg) {
        Data data = new Data();
        data.productionSerialNum = msg.getContent();
        return data;
    }

    public static class Data {
        public byte[] productionSerialNum = new byte[6];
    }
}
