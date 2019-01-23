package com.ut.unilink.cloudLock;

import android.os.Handler;
import android.os.Looper;

import com.ut.unilink.cloudLock.protocol.BleClient;
import com.ut.unilink.cloudLock.protocol.BleMsg;
import com.ut.unilink.cloudLock.protocol.ClientBase;
import com.ut.unilink.cloudLock.protocol.ClientHelper;
import com.ut.unilink.cloudLock.protocol.cmd.BleCallBack;
import com.ut.unilink.cloudLock.protocol.cmd.StateResponse;
import com.ut.unilink.cloudLock.protocol.data.CloudLockState;
import com.ut.unilink.cloudLock.protocol.data.GateLockState;
import com.ut.unilink.cloudLock.protocol.data.LockState;
import com.ut.unilink.util.Log;
import com.zhichu.nativeplugin.ble.scan.DeviceId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LockConnectionManager implements IConnectionManager {

    private static final int CMD_DEVICE_STATE = 0x25;  //从设备上传状态信息
    private static final int CMD_DEVICE_CLOSE = 0x30;  //从设备断开连接

    private Map<String, ClientHelper> mBleHelperMap = new HashMap<>();
    private Map<String, LockStateListener> mLockStateListenerMap = new HashMap<>();
    private BaseBleLink bleLink;
    private FrameHandler frameHandler = new FrameHandler();
    private Map<String, Integer> deviceIdMap = new HashMap<>();
    private ConnectListener connectListener;

    public LockConnectionManager() {
    }

    public void setBleLink(BaseBleLink bleLink) {
        this.bleLink = bleLink;
    }

    public boolean isConnect(String address) {
        return mBleHelperMap.containsKey(address);
    }

    @Override
    public void onConnect(final String address) {
        final String deviceUUID = address;
        ClientHelper clientHelper = mBleHelperMap.get(deviceUUID);

        if (clientHelper == null) {

            BleClient bleClient = new BleClient(deviceUUID, this);
            clientHelper = new ClientHelper(bleClient);
            final ClientHelper finalClientHelper = clientHelper;
            clientHelper.setReceiveListener(new ClientHelper.ReceiveListener() {
                @Override
                public void onReceive(BleMsg msg) {

                    if (msg.getCode() == CMD_DEVICE_STATE) {

                        final LockState lockState = parseLockState(address, msg);
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

//                        handleState(finalClientHelper);

                    } else if (msg.getCode() == CMD_DEVICE_CLOSE) {
                        Log.i("从设备断开连接");
                        bleLink.close(deviceUUID);
                    }
                }
            });

            mBleHelperMap.put(deviceUUID, clientHelper);
        }

        if (connectListener != null) {
            connectListener.onConnect(address);
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
            ClientBase client = clientHelper.getClient();
            if (wrapData != null && client != null) {
                client.receive(wrapData);
            }
        }
    }

    public void send(String address, byte[] msg) {

        int deviceId = getDeviceId(address);

        if (deviceId == DeviceId.GATE_LOCK) {
            FrameHandler.setFrameSize(512);
        } else {
            FrameHandler.setFrameSize(20);
        }

        List<byte[]> datas = frameHandler.handleSend(msg);
        for (byte[] data : datas) {
            if (bleLink != null) {
                bleLink.send(address, data);
            }
        }
    }

    private int getDeviceId(String address) {
        int deviceId = 0;
        try {
            deviceId = deviceIdMap.get(address);
        } catch (Exception e) {

        }
        return deviceId;
    }

    public ClientHelper getBleHelper(String address) {

        return mBleHelperMap.get(address);
    }

    private LockState parseLockState(String address, BleMsg msg) {

        int deviceId = getDeviceId(address);
        LockState lockState = null;
        if (deviceId == DeviceId.GATE_LOCK) {
            lockState = new GateLockState();
        } else {
            lockState = new CloudLockState();
        }

        lockState.getLockState(msg.getContent());
        return lockState;
    }

    private void handleState(ClientHelper clientHelper) {
        StateResponse stateResponse = new StateResponse();
        stateResponse.setClientHelper(clientHelper);
        stateResponse.sendMsg();
    }

    void addLockStateListener(String address, LockStateListener lockStateListener) {
        mLockStateListenerMap.put(address, lockStateListener);
    }

    public void setDeviceId(String address, int deviceId) {
        deviceIdMap.put(address, deviceId);
    }

    public void setConnectListener(ConnectListener connectListener) {
        this.connectListener = connectListener;
    }
}
