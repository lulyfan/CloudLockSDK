package com.ut.unilink.cloudLock;

import android.os.Handler;
import android.os.Looper;

import com.ut.unilink.cloudLock.protocol.BleClient;
import com.ut.unilink.cloudLock.protocol.BleMsg;
import com.ut.unilink.cloudLock.protocol.ClientHelper;
import com.ut.unilink.cloudLock.protocol.data.LockState;
import com.ut.unilink.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloundLockConnectionManager implements IConnectionManager {

    private static final int CMD_DEVICE_STATE = 0x25;

    private Map<String, ClientHelper> mBleHelperMap = new HashMap<>();
    private Map<String, LockStateListener> mLockStateListenerMap = new HashMap<>();
    private UTBleLink mUTBleLink;
    private FrameHandler frameHandler = new FrameHandler();

    public CloundLockConnectionManager(UTBleLink UTBleLink) {
        mUTBleLink = UTBleLink;
    }

    public boolean isConnect(String address) {
        return mBleHelperMap.containsKey(address);
    }

    @Override
    public void onConnect(String address) {
        final String deviceUUID = address;
        ClientHelper clientHelper = mBleHelperMap.get(deviceUUID);

        if (clientHelper == null) {

            BleClient bleClient = new BleClient(deviceUUID, this);
            clientHelper = new ClientHelper(bleClient);
            clientHelper.setReceiveListener(new ClientHelper.ReceiveListener() {
                @Override
                public void onReceive(BleMsg msg) {

                  if (msg.getCode() == CMD_DEVICE_STATE) {

                        final LockState lockState = parseLockState(msg);
                        final LockStateListener lockStateListener = mLockStateListenerMap.get(deviceUUID);

                        if (lockStateListener != null) {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    lockStateListener.onState(lockState);
                                }
                            });
                        }
                    }
                }
            });

            mBleHelperMap.put(deviceUUID, clientHelper);
        }
    }

    @Override
    public void onDisConnect(String address, int code) {
        ClientHelper clientHelper = mBleHelperMap.get(address);
        if (clientHelper != null) {
            clientHelper.close();
            mBleHelperMap.remove(address);
            mLockStateListenerMap.remove(address);

            Log.i("close clientHelper");
        }
    }

    @Override
    public void onReceive(String address, byte[] data) {
        ClientHelper clientHelper = mBleHelperMap.get(address);
        if (clientHelper != null) {
            byte[] wrapData = frameHandler.handleReceive(data);
            if (wrapData != null) {
                clientHelper.getClient().receive(wrapData);
            }
        }
    }

    public void send(String address, byte[] msg) {

        List<byte[]> datas = frameHandler.handleSend(msg);
        for (byte[] data : datas) {
            mUTBleLink.send(address, data);
        }
    }

    public ClientHelper getBleHelper(String address) {

        return mBleHelperMap.get(address);
    }

    private LockState parseLockState(BleMsg msg) {
        return LockState.parseLockState(msg.getContent());
    }

    void addLockStateListener(String address, LockStateListener lockStateListener) {
        mLockStateListenerMap.put(address, lockStateListener);
    }

}
