package com.ut.unilink.cloudLock.protocol.GateLockCmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;
import com.ut.unilink.cloudLock.protocol.cmd.BleCmdBase;
import com.ut.unilink.cloudLock.protocol.data.GateLockKey;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ReadKeyInfos extends BleCmdBase<ReadKeyInfos.Data> {

    private static final int CODE = 0x32;

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
        int keyCount = buffer.get();
        Data data = new Data();

        for (int i=0; i<keyCount; i++) {
            GateLockKey key = new GateLockKey();
            key.setKeyId(buffer.get() & 0xFF);
            key.setKeyType(buffer.get());
            key.setAttribute(buffer.get());
            key.setInnerNum(buffer.get() & 0xFF);
            data.keys.add(key);
        }
        return data;
    }

    public static class Data {
        public List<GateLockKey> keys = new ArrayList<>();
    }
}
