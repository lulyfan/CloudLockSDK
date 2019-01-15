package com.ut.unilink.cloudLock.protocol.data;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public abstract class LockState {

    public abstract int getElect();

    public abstract void getLockState(byte[] data);
}
