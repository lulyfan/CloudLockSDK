package com.ut.unilink.cloudLock;

import com.ut.unilink.cloudLock.protocol.BleClient;
import com.ut.unilink.cloudLock.protocol.BleMsg;
import com.ut.unilink.cloudLock.protocol.ClientHelper;
import com.ut.unilink.cloudLock.protocol.data.BleLockState;
import com.ut.unilink.cloudLock.protocol.data.DeviceNodeInfo;
import com.ut.unilink.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloundLockConnectionManager implements IConnectionManager {

    private static final int CMD_CLOSE = 0x2C;
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

                    if (msg.getCode() == CMD_CLOSE) {

                        mUTBleLink.close(deviceUUID);

                    } else if (msg.getCode() == CMD_DEVICE_STATE) {

                        BleLockState bleLockState = parseBleStatus(msg);
                        LockStateListener lockStateListener = mLockStateListenerMap.get(deviceUUID);
                        if (lockStateListener != null) {
                            lockStateListener.onState(bleLockState);
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

    private BleLockState parseBleStatus(BleMsg msg) {
        ByteBuffer buffer = ByteBuffer.wrap(msg.getContent());
        byte statusNum = buffer.get();
        List<DeviceNodeInfo> deviceNodeInfoList = new ArrayList<>();

        for (int i=0; i<statusNum; i++) {
            byte devNo = buffer.get();

            DeviceNodeInfo deviceNodeInfo = new DeviceNodeInfo(devNo);
            buffer.get(deviceNodeInfo.value);
            deviceNodeInfoList.add(deviceNodeInfo);
        }

        BleLockState bleLockState = new BleLockState();
        for (DeviceNodeInfo deviceNodeInfo : deviceNodeInfoList) {
            switch (deviceNodeInfo.devNo) {
                case BleLockState.DEV_NUM_ELEC:
                    bleLockState.setElect(deviceNodeInfo.value[0] + "");
                    break;

                case BleLockState.DEV_NUM_LOCK_STATE:
                    bleLockState.setStatus(deviceNodeInfo.value[0] + "");
                    break;

                default:
            }
        }

        return bleLockState;
    }

    void addLockStateListener(String address, LockStateListener lockStateListener) {
        mLockStateListenerMap.put(address, lockStateListener);
    }

}
