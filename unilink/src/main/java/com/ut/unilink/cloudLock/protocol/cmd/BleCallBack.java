package com.ut.unilink.cloudLock.protocol.cmd;

/**
 * Created by huangkaifan on 2018/6/12.
 */

public interface BleCallBack<T> {
    void success(T result);
    void fail(int errCode, String errMsg);
    void timeout();
}