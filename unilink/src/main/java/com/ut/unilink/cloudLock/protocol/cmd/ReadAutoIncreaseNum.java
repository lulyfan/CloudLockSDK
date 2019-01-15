package com.ut.unilink.cloudLock.protocol.cmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;
import com.zhichu.nativeplugin.ble.Ble;

import java.nio.ByteBuffer;

public class ReadAutoIncreaseNum extends BleCmdBase<ReadAutoIncreaseNum.Data>{

    private static final byte CODE = 0x2B;

    @Override
    public BleMsg build() {
        BleMsg msg = new BleMsg();
        msg.setCode(CODE);
        msg.setEncryptType(BleMsg.ENCRYPT_TYPE_FIXED);
        return msg;
    }

    @Override
    public Data parse(BleMsg msg) {
        ByteBuffer buffer = ByteBuffer.wrap(msg.getContent());
        Data data = new Data();
        data.autoIncreaseNum = buffer.getShort();
        return data;
    }

    public static class Data {
        public short autoIncreaseNum;
    }
}
