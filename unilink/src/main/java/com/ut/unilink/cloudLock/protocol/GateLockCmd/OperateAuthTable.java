package com.ut.unilink.cloudLock.protocol.GateLockCmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;
import com.ut.unilink.cloudLock.protocol.cmd.BleCmdBase;
import com.ut.unilink.cloudLock.protocol.data.AuthInfo;
import com.ut.unilink.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class OperateAuthTable extends BleCmdBase<OperateAuthTable.Data> {

    private static final int CODE = 0x36;
    public static final int ADD = 1;
    public static final int UPDATE = 2;
    public static final int DELETE = 3;
    public static final int QUERY = 4;

    private int operateType;
    private AuthInfo authInfo;  //要添加或修改的授权信息
    private int authId = -1;   //要删除或查看的授权编号

    public OperateAuthTable(int operateType) {
        if (!(operateType <= 4 && operateType >= 1)) {
            throw new IllegalArgumentException("操作授权管理表的操作类型的取值范围必须为:1~4");
        }

        this.operateType = operateType;
    }

    public void setAuthInfo(AuthInfo authInfo) {
        this.authInfo = authInfo;
    }

    public void setAuthId(int authId) {
        this.authId = authId;
    }

    @Override
    public BleMsg build() {
        BleMsg msg = new BleMsg();
        msg.setCode((byte) CODE);

        ByteBuffer buffer = ByteBuffer.allocate(3 + 12);
        buffer.putShort((short) autoIncreaseNum);
        buffer.put((byte) operateType);

        switch (operateType) {
            case ADD:
            case UPDATE:
                if (authInfo == null) {
                    Log.i("未设置要添加或修改的授权信息");
                    return null;
                }

                if (operateType == UPDATE) {
                    buffer.put((byte) authInfo.getAuthId());
                }

                buffer.put((byte) authInfo.getKeyId());
                buffer.put((byte) authInfo.getOpenLockCount());
                buffer.put(authInfo.getValidWeekDay());
                buffer.put(authInfo.getStartTimeBytes());
                buffer.put(authInfo.getEndTimeBytes());
                break;

            case DELETE:
                if (authId < 0) {
                    Log.i("未设置要删除的授权信息");
                    return null;
                }
                buffer.put((byte) authId);
                break;

            case QUERY:
                if (authId < 0) {
                    authId = 0xFF;    //如果没设置要查询的指定授权信息，则查询所有的授权信息
                }
                buffer.put((byte) authId);
                break;

            default:
        }

        byte[] content = new byte[buffer.position()];
        buffer.flip();
        buffer.get(content);
        msg.setContent(content);
        return msg;
    }

    @Override
    public Data parse(BleMsg msg) {
        ByteBuffer buffer = ByteBuffer.wrap(msg.getContent());
        Data data = new Data();
        data.operateType = buffer.get();

        if (data.operateType == QUERY) {
            int authInfoCount = buffer.get();
            data.authInfos = new ArrayList<>();

            for (int i=0; i<authInfoCount; i++) {
                AuthInfo authInfo = new AuthInfo();
                authInfo.setAuthId(buffer.get() & 0xFF);
                authInfo.setKeyId(buffer.get() & 0xFF);
                authInfo.setOpenLockCount(buffer.get() & 0xFF);
                authInfo.setValidWeekDay(buffer.get());
                Log.i("ValidWeekDay:" + String.format("%2x", authInfo.getValidWeekDay()));
                buffer.get(authInfo.getStartTimeBytes());
                buffer.get(authInfo.getEndTimeBytes());
                data.authInfos.add(authInfo);
            }
        } else{
            data.authId = buffer.get();
        }
        return data;
    }

    public static class Data {
        public byte operateType;
        public byte authId;
        public List<AuthInfo> authInfos;
    }
}
