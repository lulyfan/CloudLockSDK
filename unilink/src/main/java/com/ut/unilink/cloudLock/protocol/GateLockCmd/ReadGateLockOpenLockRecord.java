package com.ut.unilink.cloudLock.protocol.GateLockCmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;
import com.ut.unilink.cloudLock.protocol.cmd.BleCmdBase;
import com.ut.unilink.cloudLock.protocol.data.GateLockOperateRecord;
import com.ut.unilink.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ReadGateLockOpenLockRecord extends BleCmdBase<ReadGateLockOpenLockRecord.Data> {

    private static final int CODE = 0x34;
    private int readSerialNum;

    public ReadGateLockOpenLockRecord(int readSerialNum) {
        this.readSerialNum = readSerialNum;
    }

    @Override
    public BleMsg build() {
        BleMsg msg = new BleMsg();
        msg.setCode((byte) CODE);
        ByteBuffer buffer = ByteBuffer.allocate(3);
        buffer.putShort((short) autoIncreaseNum);
        buffer.put((byte) readSerialNum);
        msg.setContent(buffer.array());
        return msg;
    }

    @Override
    public Data parse(BleMsg msg) {
        ByteBuffer buffer = ByteBuffer.wrap(msg.getContent());
        int recordCount = buffer.get();
        Data data = new Data();

        for (int i=0; i<recordCount; i++) {
            GateLockOperateRecord record = new GateLockOperateRecord();
            record.setKeyId(buffer.get() & 0xFF);
            record.setKeyType(buffer.get() & 0xFF);
            buffer.get(record.getOperateTimeBytes());
            data.records.add(record);
        }
        return data;
    }

    public static class Data {
        public List<GateLockOperateRecord> records = new ArrayList<>();
    }
}
