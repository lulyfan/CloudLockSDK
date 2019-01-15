package com.ut.unilink.cloudLock.protocol.GateLockCmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;
import com.ut.unilink.cloudLock.protocol.cmd.BleCmdBase;
import com.ut.unilink.cloudLock.protocol.data.GateLockKey;

import java.nio.ByteBuffer;
import java.util.List;

public class WriteKeyInfos extends BleCmdBase<Void> {

    private static final byte CODE = 0x31;
    private List<GateLockKey> keys;

    public WriteKeyInfos(List<GateLockKey> keys) {
        if (keys == null) {
            throw new IllegalArgumentException("写入的钥匙列表不能为null");
        }

        this.keys = keys;
    }

    @Override
    public BleMsg build() {
        BleMsg msg = new BleMsg();
        msg.setCode(CODE);

        int keyCount = keys.size();
        ByteBuffer buffer = ByteBuffer.allocate(3 + keyCount * 4);
        buffer.putShort((short) autoIncreaseNum);
        buffer.put((byte) keyCount);

        for (int i=0; i<keys.size(); i++) {

            GateLockKey key = keys.get(i);
            buffer.put((byte) key.getKeyId());
            buffer.put(key.getKeyType());
            buffer.put(key.getAttribute());
            buffer.put((byte) key.getInnerNum());
        }
        msg.setContent(buffer.array());
        return msg;
    }

    @Override
    public Void parse(BleMsg msg) {
        return null;
    }
}
