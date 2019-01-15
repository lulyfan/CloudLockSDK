package com.ut.unilink.cloudLock;

import com.ut.unilink.cloudLock.protocol.data.CloudLockState;
import com.ut.unilink.cloudLock.protocol.data.LockState;

/**
 * <p>云锁状态监听器.
 * <p>在连接时设置云锁状态监听器
 * 连接成功后即可以获取云锁状态信息
 */
public interface LockStateListener {
    /**
     * 收到云锁设备的状态信息
     * @param state 云锁设备上传的状态信息，调用{@link LockState#getElect()}可以获取电量
     */
    void onState(LockState state);
}
