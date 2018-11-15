package com.ut.unilink.cloudLock.protocol;

import com.ut.unilink.cloudLock.IConnectionManager;

public class BleClient extends ClientBase {

    private String mDestAddress;
    private IConnectionManager mConnectionManager;
    private boolean isConnect;

    public BleClient(String destAddress, IConnectionManager connectionManager) {
        mDestAddress = destAddress;
        mConnectionManager = connectionManager;
    }

    @Override
    public void send(byte[] msg) {
        if (mConnectionManager != null) {
            mConnectionManager.send(mDestAddress, msg);
        }
    }

    @Override
    public void broadcastSend(byte[] msg) {

    }

    @Override
    public void open() {

    }

    @Override
    public boolean isConnect() {
        return isConnect;
    }

    public void setConnect(boolean connect) {
        isConnect = connect;
    }

    @Override
    public void receive(final byte[] data) {
        if (handleExecutor.isShutdown()) {
            return;
        }

        handleExecutor.execute(new Runnable() {

            @Override
            public void run() {
                if (receiveListener != null) {
                    receiveListener.onReceive(data);
                }
            }
        });
    }


}
