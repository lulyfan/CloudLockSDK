package com.ut.unilink.cloudLock.protocol.data;

import com.ut.unilink.util.BitUtil;
import com.ut.unilink.util.Log;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class AuthInfo {
    private int authId;  //授权编号
    private int keyId;   //钥匙编号
    private int openLockCount;  //开锁次数
    private byte validWeekDay;      //控制在星期几有效
    private byte[] startTime = new byte[4];  //开始时间
    private byte[] endTime = new byte[4];    //结束时间

    public int getAuthId() {
        return authId;
    }

    public void setAuthId(int authId) {
        this.authId = authId;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getOpenLockCount() {
        return openLockCount;
    }

    public void setOpenLockCount(int openLockCount) {
        this.openLockCount = openLockCount;
    }

    public byte getValidWeekDay() {
        return validWeekDay;
    }

    public void setValidWeekDay(byte validWeekDay) {
        this.validWeekDay = validWeekDay;
    }

    /**
     * 设置星期控制数组, 例如：[0, 1, 5]表示星期日、星期一、星期五为授权有效日期
     * @param validWeekDay
     */
    public void setAuthDay(int[] validWeekDay) {
        if (validWeekDay == null) {
            return;
        }

        for (int day : validWeekDay) {
            if (day <= 6 && day >= 0) {
                this.validWeekDay = (byte) BitUtil.set1(this.validWeekDay, day);
                this.validWeekDay = (byte) BitUtil.set1(this.validWeekDay, 7); //星期时间控制位置1
            }
        }
    }

    /**
     * 返回星期控制数组，例如：[0, 1, 5]表示星期日、星期一、星期五为授权有效日期
     * @return
     */
    public int[] getAuthDay() {
        int enable = BitUtil.bit(validWeekDay, 7);
        if (enable != 1) {
            return null;
        }

        int[] temp = new int[7];
        int pos = 0;

        for (int i=0; i<7; i++) {
            if (BitUtil.bit(validWeekDay, i) == 1) {
                temp[pos] = i;
                pos ++;
            }
        }

        if (pos == 0) {
            return null;
        }

        int[] result = new int[pos];
        System.arraycopy(temp, 0, result, 0, pos);
        return result;
    }

    public byte[] getStartTimeBytes() {
        return startTime;
    }

    /**
     * 返回授权开始时间的时间戳
     * @return
     */
    public long getStartTime() {
        ByteBuffer buffer = ByteBuffer.wrap(this.startTime);
        long startTime = buffer.getInt() * 1000L;
        return startTime;
    }

    public void setStartTime(byte[] startTime) {
        this.startTime = startTime;
    }

    /**
     * 设置授权开始时间
     * @param startTime 时间戳
     */
    public void setStartTime(long startTime) {
        startTime = startTime / 1000;   //转换成以秒为单位的时间戳
        ByteBuffer buffer = ByteBuffer.wrap(this.startTime);
        buffer.putInt((int) startTime);
    }

    public byte[] getEndTimeBytes() {
        return endTime;
    }

    /**
     * 返回授权结束时间的时间戳
     * @return
     */
    public long getEndTime() {
        ByteBuffer buffer = ByteBuffer.wrap(this.endTime);
        long endTime = buffer.getInt() * 1000L;
        return endTime;
    }

    public void setEndTime(byte[] endTime) {
        this.endTime = endTime;
    }

    /**
     * 设置授权结束时间
     * @param endTime 时间戳
     */
    public void setEndTime(long endTime) {
        endTime = endTime / 1000;   //转换成以秒为单位的时间戳
        ByteBuffer buffer = ByteBuffer.wrap(this.endTime);
        buffer.putInt((int) endTime);
    }

    @Override
    public String toString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "授权ID:" + authId +
                "\n钥匙ID:" + keyId +
                "\n授权开锁次数:" + openLockCount +
                "\n授权星期:" + Arrays.toString(getAuthDay()) +
                "\n开始时间:" + simpleDateFormat.format(new Date(getStartTime())) +
                "\n结束时间:" + simpleDateFormat.format(new Date(getEndTime()));
    }
}
