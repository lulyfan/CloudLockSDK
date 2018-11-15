package com.ut.unilink.cloudLock;

import com.ut.unilink.cloudLock.protocol.data.BleLockState;

public interface LockStateListener {
    void onState(BleLockState state);
}
