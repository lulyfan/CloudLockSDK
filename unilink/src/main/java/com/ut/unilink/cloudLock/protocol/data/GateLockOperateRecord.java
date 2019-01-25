package com.ut.unilink.cloudLock.protocol.data;

import com.ut.unilink.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GateLockOperateRecord {

    private int keyId;
    private int keyType;
    private byte[] operateTime = new byte[4];

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getKeyType() {
        return keyType;
    }

    public void setKeyType(int keyType) {
        this.keyType = keyType;
    }

    public byte[] getOperateTimeBytes() {
        return operateTime;
    }

    public long getOperateTime() {
        ByteBuffer buffer = ByteBuffer.wrap(operateTime);
        long time = buffer.getInt() * 1000L;
        return time;
    }

    public void setOperateTime(byte[] operateTime) {
        this.operateTime = operateTime;
    }

    @Override
    public String toString() {
        long operateTime = getOperateTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String openLockTime = simpleDateFormat.format(new Date(operateTime));

        String type = "";
        switch (keyType) {
            case GateLockKey.TYPE_FINGERPRINT:
                type = "指纹";
                break;

            case GateLockKey.TYPE_BLE:
                type = "蓝牙";
                break;

            case GateLockKey.TYPE_CARD:
                type = "卡片";
                break;

            case GateLockKey.TYPE_ELECT:
                type = "电子钥匙";
                break;

            case GateLockKey.TYPE_PASSWORD:
                type = "密码";
                break;

            default:
        }

        return "钥匙ID:" + keyId +
                "\n钥匙类型:" + type +
                "\n开锁时间:" + openLockTime;
    }
}
