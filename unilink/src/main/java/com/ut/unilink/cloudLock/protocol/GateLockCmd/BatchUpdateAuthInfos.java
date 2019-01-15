package com.ut.unilink.cloudLock.protocol.GateLockCmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;
import com.ut.unilink.cloudLock.protocol.cmd.BleCmdBase;
import com.ut.unilink.cloudLock.protocol.data.AuthInfo;
import com.ut.unilink.util.Log;

import java.nio.ByteBuffer;
import java.util.List;

public class BatchUpdateAuthInfos extends BleCmdBase<Void> {

    private static final int CODE = 0x35;
    private List<AuthInfo> authInfos;

    public BatchUpdateAuthInfos(List<AuthInfo> authInfos) {
        this.authInfos = authInfos;
    }

    @Override
    public BleMsg build() {
        if (authInfos == null || authInfos.size() <= 0) {
            Log.i("要批量修改的授权列表为null或没有数据");
            return null;
        }

        BleMsg msg = new BleMsg();
        msg.setCode((byte) CODE);

        int authInfoCount = authInfos.size();
        ByteBuffer buffer = ByteBuffer.allocate(3 + 12 * authInfoCount);
        buffer.putShort((short) autoIncreaseNum);
        buffer.put((byte) authInfoCount);

        for (int i=0; i<authInfoCount; i++) {
            AuthInfo authInfo = authInfos.get(i);
            buffer.put((byte) authInfo.getAuthId());
            buffer.put((byte) authInfo.getKeyId());
            buffer.put((byte) authInfo.getOpenLockCount());
            buffer.put(authInfo.getValidWeekDay());
            buffer.put(authInfo.getStartTimeBytes());
            buffer.put(authInfo.getEndTimeBytes());
        }

        msg.setContent(buffer.array());
        return msg;
    }

    @Override
    public Void parse(BleMsg msg) {
        return null;
    }
}
