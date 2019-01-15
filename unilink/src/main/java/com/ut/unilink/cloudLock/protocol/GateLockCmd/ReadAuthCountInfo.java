package com.ut.unilink.cloudLock.protocol.GateLockCmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;
import com.ut.unilink.cloudLock.protocol.cmd.BleCmdBase;
import com.ut.unilink.cloudLock.protocol.data.AuthCountInfo;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ReadAuthCountInfo extends BleCmdBase<ReadAuthCountInfo.Data> {

    private static final int CODE = 0x37;

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
        int count = buffer.get();
        Data data = new Data();

        for (int i=0; i<count; i++) {
            AuthCountInfo authCountInfo = new AuthCountInfo();
            authCountInfo.setAuthId(buffer.get());
            authCountInfo.setAuthCount(buffer.get());
            authCountInfo.setOpenLockCount(buffer.get());
            data.authCountInfos.add(authCountInfo);
        }
        return data;
    }

    public static class Data {
        public List<AuthCountInfo> authCountInfos = new ArrayList<>();
    }
}
