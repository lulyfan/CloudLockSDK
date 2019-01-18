package com.ut.unilink.cloudLock.protocol.cmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;
import com.ut.unilink.cloudLock.protocol.data.CloudLockOperateRecord;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ReadOpenLockRecord extends BleCmdBase<ReadOpenLockRecord.Data> {

    private static final byte CODE = 0x2D;
    private int readSerialNum; //读取序号

    public ReadOpenLockRecord(int readSerialNum) {
        this.readSerialNum = readSerialNum;
    }

    @Override
    public BleMsg build() {
        BleMsg msg = new BleMsg();
        msg.setCode(CODE);
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
            CloudLockOperateRecord record = new CloudLockOperateRecord();
            buffer.get(record.getOperateTimeBytes());
            record.setMotorControl(buffer.get());
            record.setDoorState(buffer.get());
            data.operateRecords.add(record);
        }

        return data;
    }

    public static class Data {
        public List<CloudLockOperateRecord> operateRecords = new ArrayList<>();

    }
}
