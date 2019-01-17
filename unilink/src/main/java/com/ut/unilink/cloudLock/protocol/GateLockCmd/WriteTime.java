package com.ut.unilink.cloudLock.protocol.GateLockCmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;
import com.ut.unilink.cloudLock.protocol.cmd.BleCmdBase;

import java.nio.ByteBuffer;
import java.util.Calendar;

public class WriteTime extends BleCmdBase<Void> {

    private static final int CODE = 0x2E;
    private long time;

    public WriteTime(long time) {
        this.time = time;
    }

    @Override
    public BleMsg build() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int year = calendar.get(Calendar.YEAR) - 2000;
        int mouth = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int week = calendar.get(Calendar.DAY_OF_WEEK);

        week = week - 1;
        if (week == 0) {
            week = 7;
        }

        BleMsg msg = new BleMsg();
        msg.setCode((byte) CODE);
        ByteBuffer buffer = ByteBuffer.allocate(9);
        buffer.putShort((short) autoIncreaseNum);
        buffer.put((byte) year);
        buffer.put((byte) mouth);
        buffer.put((byte) day);
        buffer.put((byte) hour);
        buffer.put((byte) minute);
        buffer.put((byte) second);
        buffer.put((byte) week);

        msg.setContent(buffer.array());
        return msg;
    }

    @Override
    public Void parse(BleMsg msg) {
        return null;
    }


}
