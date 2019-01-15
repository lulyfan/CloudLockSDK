package com.ut.unilink.cloudLock.protocol.GateLockCmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;
import com.ut.unilink.cloudLock.protocol.cmd.BleCmdBase;

import java.nio.ByteBuffer;
import java.util.Calendar;

public class ReadTime extends BleCmdBase<ReadTime.Data> {

    private static final int CODE = 0x2F;

    @Override
    public BleMsg build() {
        BleMsg msg = new BleMsg();
        msg.setCode((byte) CODE);
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort((short) autoIncreaseNum);
        msg.setContent(buffer.array());
        return msg;
    }

    @Override
    public Data parse(BleMsg msg) {
        ByteBuffer buffer = ByteBuffer.wrap(msg.getContent());
        int year = buffer.get() + 2000;
        int mouth = buffer.get() - 1;
        int day = buffer.get();
        int hour = buffer.get();
        int minute = buffer.get();
        int second = buffer.get();
        int week = buffer.get();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, mouth, day, hour, minute, second);

        Data data = new Data();
        data.time = calendar.getTimeInMillis();
        return data;
    }

    public static class Data {
        public long time;
    }
}
