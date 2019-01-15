package com.ut.unilink.cloudLock.protocol.data;

public class AuthCountInfo {
    private int authId;
    private int authCount;      //授权开锁次数
    private int openLockCount;  //已开锁次数

    public int getAuthId() {
        return authId;
    }

    public void setAuthId(int authId) {
        this.authId = authId;
    }

    public int getAuthCount() {
        return authCount;
    }

    public void setAuthCount(int authCount) {
        this.authCount = authCount;
    }

    public int getOpenLockCount() {
        return openLockCount;
    }

    public void setOpenLockCount(int openLockCount) {
        this.openLockCount = openLockCount;
    }

    @Override
    public String toString() {
        return "授权ID:" + authId +
                "\n开锁限制次数:" + authCount +
                "\n已开锁次数:" + openLockCount;
    }
}
