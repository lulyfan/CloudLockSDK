package com.ut.unilink.cloudLock.protocol.cmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;

public class WriteProductionSerialNum extends BleCmdBase<WriteProductionSerialNum.Data>{

    private static final byte CODE = 0x27;
    private byte[] prodectionSerialNum;

    public WriteProductionSerialNum(byte[] prodectionSerialNum) {
        if (prodectionSerialNum == null || prodectionSerialNum.length != 6) {
            throw new IllegalArgumentException("生产序列号不能为空，并且长度为6位");
        }

        this.prodectionSerialNum = prodectionSerialNum;
    }

    @Override
    public BleMsg build() {
        BleMsg msg = new BleMsg();
        msg.setCode(CODE);
        msg.setContent(prodectionSerialNum);
        msg.setEncrypt(false);
        return msg;
    }

    @Override
    Data parse(BleMsg msg) {
        byte[] content = msg.getContent();
        Data data = new Data();
        data.result = content[0];
        return data;
    }

    public static class Data {
        public byte result;
    }
}
