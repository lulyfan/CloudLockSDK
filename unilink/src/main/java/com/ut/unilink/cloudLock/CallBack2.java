package com.ut.unilink.cloudLock;

public interface CallBack2<T> {
    /**
     * 命令执行成功
     * @param data 返回的数据
     */
    void onSuccess(T data);

    /**
     * 命令执行失败
     * @param errCode 错误码
     * @param errMsg 错误信息
     */
    void onFailed(int errCode, String errMsg);
}
