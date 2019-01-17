package com.ut.unilink.cloudLock;

public abstract class BaseBleLink {
    protected IConnectionManager mConnectionManager;
    protected ConnectListener connectListener;

    public void setConnectListener(ConnectListener connectListener) {
        this.connectListener = connectListener;
    }

    public void setConnectionManager(IConnectionManager mConnectManager) {
        this.mConnectionManager = mConnectManager;
    }

    abstract void connect(String address, ConnectListener connectListener);
    abstract void send(String address, byte[] data);
    abstract void close(String address);
}
