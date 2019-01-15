package com.ut.unilink.cloudLock.protocol.GateLockCmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;
import com.ut.unilink.cloudLock.protocol.cmd.BleCmdBase;

import java.nio.ByteBuffer;

public class DeleteKey extends BleCmdBase<Void> {

    private static final int CODE = 0x33;
    private int keyId;

    public DeleteKey(int keyId) {
        this.keyId = keyId;
    }

    @Override
    public BleMsg build() {
        BleMsg msg = new BleMsg();
        msg.setCode((byte) CODE);
        ByteBuffer buffer = ByteBuffer.allocate(3);
        buffer.putShort((short) autoIncreaseNum);
        buffer.put((byte) keyId);
        msg.setContent(buffer.array());
        return msg;
    }

    @Override
    public Void parse(BleMsg msg) {
        return null;
    }
}
