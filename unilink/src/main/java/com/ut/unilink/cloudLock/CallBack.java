package com.ut.unilink.cloudLock;

/**
 * 对云锁设备进行命令控制的回调接口
 */
public interface CallBack {
    /**
     * 命令执行成功
     * @param cloudLock 执行命令的云锁设备
     */
    void onSuccess(CloudLock cloudLock);

    /**
     * 命令执行失败
     * @param errCode 错误码
     * @param errMsg 错误信息
     */
    void onFailed(int errCode, String errMsg);
}
