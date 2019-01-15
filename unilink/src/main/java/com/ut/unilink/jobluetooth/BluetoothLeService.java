//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ut.unilink.jobluetooth;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;

import com.ut.unilink.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Vector;

import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;
import static android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;

@SuppressLint("NewApi")
public class BluetoothLeService extends Service {
    public static final int DEFAULT_READ_CHARACTERISTIC_TIME = 100;
    public static final int DEFAULT_CONNECT_TIMEOUT = 5;
    private static final int DEFAULT_DISCOVER_SERVICES_TIMEOUT = 3;
    private static final int DEFAULT_READ_CHARACTERISTIC__TIMEOUT = 2;
    private BluetoothGattCharacteristic FOR_SERIAL_PORT_READ_Characteristic;
    private BluetoothGattCharacteristic SERIAL_PORT_WRITE_Characteristic;
    private BluetoothGattCharacteristic MAX_PACKET_SIZE_Characteristic;
    private BluetoothGattCharacteristic NO_RESPONSE_MAX_PACKET_COUNT_Characteristic;
    private BluetoothGattCharacteristic DEVICE_RECEIVED_PACKET_SEQUENCE_Characteristic;
    private BluetoothGattCharacteristic HOST_RECEIVED_PACKET_SEQUENCE_Characteristic;
    private BluetoothGattCharacteristic PACKET_TIMEOUT_Characteristic;
    private BluetoothGattCharacteristic DEVICE_RECEIVED_ERROR_PACKET_SEQUENCE_Characteristic;
    private BluetoothGattCharacteristic HOST_RECEIVED_ERROR_PACKET_SEQUENCE_Characteristic;
    private BluetoothGattCharacteristic DEVICE_CAN_RECEIVE_PACKET_Characteristic;
    private BluetoothGattCharacteristic HOST_CAN_RECEIVE_PACKET_Characteristic;
    private BluetoothGattCharacteristic RESET_SEQUENCE_Characteristic;
    public static byte[] myId;
    public static BluetoothAdapter mBluetoothAdapter;
    public static BluetoothGatt mBluetoothGatt;
    public static int mConnectionState = 0;
    private int readCharacteristicCount = 0;
    private int stateReadMaxPacketSize = 0;
    private int stateReadNoResponseMaxPacketCount = 0;
    private int stateReadPacketTimeout = 0;
    private int stateReadSuccess = 0;
    private int readMaxPacketSize = 0;
    private int readNoResponseMaxPacketCount = 0;
    private int readPacketTimeout = 0;
    private int sendDataNumber = 'ꀀ';
    public long DEFAULT_SEND_PACKET_INTERVAL = 8L;
    public long S_SEND_PACKET_INTERVAL = 20L;
    public int nMaxPacketSize = 20;
    public int nNoResponseAllowMaxPacketCount = 5;
    public int nNoResponseAllowMaxPacketCounte = 10;
    private int DEFAULT_NO_RESPONSE_MAX_PACKET_COUNT = 10;
    private Vector<byte[]> arrayWaitSendData = null;
    private List<byte[]> arrayWaitResponseData = null;
    private Timer dataTimer = null;
    private Timer timeIncreaseTimer = null;
    private int nNextSendPacketSequence = 0;
    private int nNextSendDataIndex = 0;
    private int dqSendPacketSequence = 0;
    private int dqSendDataIndex = 0;
    private int answerDataPacketSum = 0;
    private int nNextWantRecvPacketSequence = 0;
    private boolean bPeerCanReceive = true;
    private int timeincrease = 0;
    private int senddatatime = 0;
    private int TimeIncreaseMagnitude = 5;

    public static final UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString("0000C004-0000-1000-8000-00805f9b34fb");
    public static final String DATA_NAME = "receivedData";
    private int state = 0;
    private static int idbyteid = 0;
    private static int disableSend = 0;
    private static int idrecvid = 0;
    private static boolean pauseSend = false;
    private static int lastResendindex = 0;

    private static final String TAG = "bleTest";
    @SuppressLint("NewApi")
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(TAG, "=====status:" + status + "====newState:" + newState + " gatt==mBluetoothGatt:" + (gatt == mBluetoothGatt));
            synchronized (BluetoothLeService.class) {
                String intentAction;
                if (newState == STATE_CONNECTED) {
                    if (mConnectionState == 1) {
                        mServiceHnadler.removeCallbacks(mConnectTimeoutRunnable);
                        mConnectionState = 2;
                        lastSendTime = 0;
                        boolean bo = mBluetoothGatt.discoverServices();
                        Log.i(TAG, "Attempting to start service discovery:" + bo);
                        intentAction = BlueToothParams.ACTION_GATT_CONNECTED;
                        broadcastUpdate(intentAction);
                        Log.i(TAG, "Connected to GATT server.");
                        mServiceHnadler.postDelayed(mDiscoverServicesTimeoutRunnable, 3000L);
                    }
                } else if (newState == STATE_DISCONNECTED) {
                    disconnect(2);
                    intentAction = BlueToothParams.ACTION_GATT_DISCONNECTED;
                    Log.i(TAG, "Disconnected from GATT server.intentAction: " + intentAction);
                    broadcastUpdate(intentAction);
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            synchronized (BluetoothLeService.class) {
                if (status == 0) {
                    mServiceHnadler.removeCallbacks(mDiscoverServicesTimeoutRunnable);
                    mConnectionState = 4;
                    getGattServices(getSupportedGattServices());
                } else {
                    Log.i(TAG, "onServicesDiscovered received: " + status);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status != 0) {
                return;
            }
            Log.i(TAG, "onCharacteristicRead " + characteristic.getUuid().toString() + " --> " + Tools.bytesToHexString(characteristic.getValue()));
            if (characteristic.getValue() != null) {
                if (stateReadSuccess != 5) {
                    if (characteristic.getUuid().toString().equals("0000b353-0000-1000-8000-00805f9b34fb")) {
                        readMaxPacketSize = characteristic.getValue()[0];
                        stateReadMaxPacketSize = 1;
                    } else if (characteristic.getUuid().toString().equals("0000b354-0000-1000-8000-00805f9b34fb")) {
                        readNoResponseMaxPacketCount = characteristic.getValue()[0];
                        stateReadNoResponseMaxPacketCount = 1;
                    } else if (characteristic.getUuid().toString().equals("0000b357-0000-1000-8000-00805f9b34fb")) {
                        readPacketTimeout = characteristic.getValue()[0];
                        stateReadPacketTimeout = 1;
                    }
                    checkReadCharacteristic();
                } else {
                    String UUIDStr = characteristic.getUuid().toString();
                    if (UUIDStr.equals("0000b351-0000-1000-8000-00805f9b34fb")) {
                        whichChanged(characteristic);
                    }
                }
            }

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic != null) {
                whichChanged(characteristic);
            } else {
                Log.i(TAG, "characteristic = null");
            }

        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.i(TAG, "rssi = " + rssi);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "====ble uuid:" + characteristic.getUuid() + " write data success:" + HexUtils.getFormatHex(characteristic.getValue()));

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (characteristic.getUuid().toString().equals(SERIAL_PORT_WRITE_Characteristic.getUuid().toString())) {
                    Log.i(TAG,  "write data success:" + HexUtils.getFormatHex(characteristic.getValue()));
                    mSuccessSendSequence ++;
                    writeDataPacket();
                }
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }
    };

    private final IBinder mBinder = new BluetoothLeService.LocalBinder();

    Runnable mConnectTimeoutRunnable = new Runnable() {
        public void run() {
            synchronized (BluetoothLeService.class) {
                if (BluetoothLeService.mConnectionState == 1) {
                    Log.i(TAG, "conect timeout --> disconnect");
                    disconnect(11);
                    refreshGatt();//刷新一下，三星的蓝牙在连接过程中断开，下次连接会直接返回133，加这个好一点
                }
                mServiceHnadler.removeCallbacks(mConnectTimeoutRunnable);
            }
        }
    };

    Runnable mDiscoverServicesTimeoutRunnable = new Runnable() {
        public void run() {
            synchronized (BluetoothLeService.class) {
                if (BluetoothLeService.mConnectionState != 4) {
                    Log.i(TAG, "discoverServices timeout --> disconnect");
                    disconnect(11);
                }
            }
        }
    };
    Runnable mReadCharacteristicTimeoutRunnable = new Runnable() {
        public void run() {
            synchronized (BluetoothLeService.class) {
                if (BluetoothLeService.mConnectionState == 4) {
                    if (stateReadSuccess != 5) {
                        if (readCharacteristicCount < 1) {
                            Log.i(TAG, "Try read characteristic one more time!");
                            startReadCharacteristic();
                            mServiceHnadler.postDelayed(mReadCharacteristicTimeoutRunnable, 3000L);//超时重读
                            readCharacteristicCount = readCharacteristicCount + 1;
                        } else {
                            Log.i(TAG, "Read characteristic timeout, disconnect!");
                            disconnect(11);
                        }
                    }
                } else {
                    Log.i(TAG, "Device not connected or service not discovered,can not read characteristic, disconnect!");
                    disconnect(11);
                }
            }

        }
    };
    //    private BluetoothLeService.SendPacketTimer sendtimer;
    private Runnable mSendRunnable = new Runnable() {
        public void run() {
            if (arrayWaitSendData != null && arrayWaitSendData.size() > 0) {
                if (BluetoothLeService.disableSend == 0) {
                    writeDataPacket();
                    mServiceHnadler.postDelayed(mSendRunnable, 5L);
                } else {
                    mServiceHnadler.postDelayed(mSendRunnable, 1L);
                }
            }

        }
    };
    Handler readCharacteristicHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    readCharacteristic(MAX_PACKET_SIZE_Characteristic);
                    break;
                case 1:
                    readCharacteristic(NO_RESPONSE_MAX_PACKET_COUNT_Characteristic);
                    break;
                case 2:
                    readCharacteristic(PACKET_TIMEOUT_Characteristic);
            }

        }
    };
    private BluetoothLeService.IncreaseTimer tit;
    private BluetoothLeService.SendDataTimer sdt;
    private Handler mServiceHnadler = new Handler() {
        @SuppressLint("NewApi")
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    HOST_CAN_RECEIVE_PACKET_Characteristic.setValue((byte[]) msg.obj);
                    wirteCharacteristic(HOST_CAN_RECEIVE_PACKET_Characteristic);
                    break;
                case 1:
                    if (RESET_SEQUENCE_Characteristic != null) {
                        RESET_SEQUENCE_Characteristic.setValue((byte[]) msg.obj);
                        wirteCharacteristic(RESET_SEQUENCE_Characteristic);
                    }
                    break;
                case 2:
                    Log.i(TAG, "接收 发送 应答模块与设备数据");
                    disconnect(1);
                    break;
                case 3:
                    broadcastUpdate(BlueToothParams.ACTION_GATT_DATARECEIVED, (byte[]) msg.obj);
                    Log.i(TAG, "msg:" + HexUtils.getFormatHex((byte[]) msg.obj));
                    break;
                case 4:
                    if (BluetoothLeService.mConnectionState == 4 && stateReadSuccess == 5) {
                        byte[] data = (byte[]) msg.obj;
                        SERIAL_PORT_WRITE_Characteristic.setValue(data);
                        wirteCharacteristic(SERIAL_PORT_WRITE_Characteristic);
                    }
                    break;
                case 5:
                    if (msg.arg1 == 0) {
                        HOST_RECEIVED_ERROR_PACKET_SEQUENCE_Characteristic.setValue((byte[]) msg.obj);
                        wirteCharacteristic(HOST_RECEIVED_ERROR_PACKET_SEQUENCE_Characteristic);
                    } else if (msg.arg1 == 1) {
                        HOST_RECEIVED_PACKET_SEQUENCE_Characteristic.setValue((byte[]) msg.obj);
                        wirteCharacteristic(HOST_RECEIVED_PACKET_SEQUENCE_Characteristic);
                    }
                    break;
                case 6:
                    broadcastUpdate("com.android.jinoux.ble.ACTION_GATT_SENDDATAEND");
            }

        }
    };
    private int mSuccessSendSequence;

    public BluetoothLeService() {
    }

    private void checkReadCharacteristic() {
        synchronized (BluetoothLeService.class) {
            if (this.stateReadMaxPacketSize == 1 && this.stateReadNoResponseMaxPacketCount == 1
                    && this.stateReadPacketTimeout == 1 && mConnectionState == 4) {
                this.mServiceHnadler.removeCallbacks(this.mReadCharacteristicTimeoutRunnable);
                this.stateReadSuccess = 5;
                this.nNextSendPacketSequence = 0;

                mSuccessSendSequence = 0;
                Log.i(TAG,  "checkReadCharacteristic");

                this.nNextWantRecvPacketSequence = 0;
                Log.i(TAG, "stateReadSuccess --> " + this.stateReadSuccess);
                this.broadcastUpdate(BlueToothParams.ACTION_GATT_READCHARACTERISTICSUCCESS);
            }
        }
    }

    public void resetReadCharacteristic() {
        this.stateReadMaxPacketSize = 0;
        this.stateReadNoResponseMaxPacketCount = 0;
        this.stateReadPacketTimeout = 0;
        this.stateReadSuccess = 0;
        this.readCharacteristicCount = 0;
    }


    private void whichChanged(BluetoothGattCharacteristic characteristic) {
        byte[] value = characteristic.getValue();
        String UUIDStr = characteristic.getUuid().toString();
        if (UUIDStr.equals("0000b351-0000-1000-8000-00805f9b34fb")) {
            if (value != null) {
                this.recvDataFromPeer(value);
            } else {
                Log.i(TAG, "characteristic.getValue()== null");
            }
        } else if (UUIDStr.equals("0000b353-0000-1000-8000-00805f9b34fb")) {
            if (value != null) {
                this.nMaxPacketSize = value[0];
            } else {
                Log.i(TAG, "characteristic.getValue()== null");
            }
        } else if (UUIDStr.equals("0000b354-0000-1000-8000-00805f9b34fb")) {
            if (value != null) {
                this.nNoResponseAllowMaxPacketCount = value[0];
            }
        } else if (UUIDStr.equals("0000b357-0000-1000-8000-00805f9b34fb")) {
            if (value != null) {
                Log.i(TAG, "数据包应答超时时间==" + value[0]);
            } else {
                Log.i(TAG, "characteristic.getValue()== null");
            }
        } else if (UUIDStr.equals("0000b355-0000-1000-8000-00805f9b34fb")) {
            if (value != null) {
                if (this.state == 1) {
                    this.recvDataPackeSequenced(value[0], 5);
                }
                this.broadcastUpdate(BlueToothParams.ACTION_GATT_SENDDATAB355, value);
            } else {
                Log.i(TAG, "characteristic.getValue()== null");
            }
        } else if (UUIDStr.equals("0000b358-0000-1000-8000-00805f9b34fb")) {
            if (value != null) {
                if (this.state == 1) {
                    this.recvDataPackeSequenced(value[0], 8);
                }

                this.broadcastUpdate(BlueToothParams.ACTION_GATT_SENDDATAB358, value);
            } else {
                Log.i(TAG, "characteristic.getValue()== null");
            }
        } else if (UUIDStr.equals("0000b35A-0000-1000-8000-00805f9b34fb")) {
            if (value != null) {
                byte flag = value[0];
                if (flag == 0) {
                    this.bPeerCanReceive = false;
                    Log.i(TAG, "------Device Can not Receive------");
                } else {
                    this.bPeerCanReceive = true;
                }
            } else {
                this.bPeerCanReceive = true;
            }
        } else if (UUIDStr.equals("0000b35C-0000-1000-8000-00805f9b34fb")) {
            Log.i(TAG, "RESET_SEQUENCE --> 0000b35C-0000-1000-8000-00805f9b34fb");
        }

    }

    public void broadcastUpdate(String action) {
        Intent intent = new Intent(action);
        this.sendBroadcast(intent);
    }

    private void broadcastUpdate(String action, byte[] data) {
        Intent intent = new Intent(action);
        intent.putExtra("receivedData", data);
        this.sendBroadcast(intent);
    }

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public boolean onUnbind(Intent intent) {
        this.BluetoothGattClose();
        return super.onUnbind(intent);
    }

    public boolean initialize() {
        BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        String addressb = mBluetoothAdapter.getAddress();
        String[] address = addressb.split(":");
        myId = Tools.hexStrToStr(address[0] + address[1] + address[2] + address[3] + address[4] + address[5]);
        return true;
    }

    public boolean connect(String address, int timeout) {
        synchronized (BluetoothLeService.class) {
            mServiceHnadler.removeCallbacks(mConnectTimeoutRunnable);//连接之前先去掉之前的超时判断
            if (mBluetoothAdapter == null) {
                Log.i(TAG, "BluetoothAdapter未初始化");
                return false;
            }
            if (TextUtils.isEmpty(address)) {
                Log.i(TAG, "要连接的设备地址为空");
                return false;
            }

            if (mConnectionState != 0) {//已在连接
                return false;
            }
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            if (device == null) {
                return false;
            }
            mBluetoothGatt = device.connectGatt(this, false, this.mGattCallback);
            if (mBluetoothGatt == null) {
                disconnect(3);
                return false;
            }
            mConnectionState = 1;
            this.mServiceHnadler.postDelayed(mConnectTimeoutRunnable, (long) (timeout * 1000));
            return true;
        }
    }

    public void disconnect(int type) {
        synchronized (BluetoothLeService.class) {
            if (mConnectionState != 0) {
                BluetoothGattClose();
                Log.i(TAG, "====ble BluetoothGattClose type:" + type);
                switch (type) {
                    case 0:
                        broadcastUpdate(BlueToothParams.ACTION_GATT_READCHARACTERISTICERROR);
                        break;
                    case 1:
                        broadcastUpdate(BlueToothParams.ACTION_GATT_HANDLERDATAERROR);
                        break;
                    case 11:
                        broadcastUpdate(BlueToothParams.ACTION_GATT_CONNECTTIMEOUT);//连接超时
                        break;
                }
                resetReadCharacteristic();
            }
        }
    }

    public void BluetoothGattClose() {
        mConnectionState = 0;
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        this.closetimer();
        this.closeIncreaseTimer();
        removeAllCallback();
    }

    private void refreshGatt() {
        try {
            Method refreshMethod = mBluetoothGatt.getClass().getDeclaredMethod("refresh");
            Object o = refreshMethod.invoke(mBluetoothGatt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeAllCallback() {
        for (int i = 1; i <= 3; i++) {
            if (readCharacteristicHandler.hasMessages(i)) {
                readCharacteristicHandler.removeMessages(i);
            }
        }
        mServiceHnadler.removeCallbacks(mConnectTimeoutRunnable);
        mServiceHnadler.removeCallbacks(mReadCharacteristicTimeoutRunnable);
        mServiceHnadler.removeCallbacks(mDiscoverServicesTimeoutRunnable);
        mServiceHnadler.removeCallbacks(mSendRunnable);
    }

    public void wirteCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter != null && mBluetoothGatt != null) {
            mBluetoothGatt.writeCharacteristic(characteristic);
        } else {
            Log.i(TAG, "send data but BluetoothAdapter not initialized");
        }
    }

    public boolean readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.i(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        if (characteristic != null) {
            Log.i(TAG, "readCharacteristic for -->" + characteristic.getUuid().toString());
            return mBluetoothGatt.readCharacteristic(characteristic);
        } else {
            return false;
        }
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.i(TAG, "BluetoothAdapter not initialized");
            return;
        }
        boolean ba = mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        Log.i(TAG, "setCharacteristicNotification for --> " + characteristic.getUuid().toString() + " to --> " + enabled + " result --> " + ba);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(characteristic.getUuid());
        if (descriptor != null) {
            Log.i(TAG, "write descriptor uuid -->" + characteristic.getUuid().toString());
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }

    }

    public List<BluetoothGattService> getSupportedGattServices() {
        return mBluetoothGatt == null ? null : mBluetoothGatt.getServices();
    }

    private void getGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices != null) {
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList();
            String uuid = null;
            Iterator var6 = gattServices.iterator();

            while (var6.hasNext()) {
                BluetoothGattService gattService = (BluetoothGattService) var6.next();
                uuid = gattService.getUuid().toString();
                Log.d("Service--->" + uuid);
                List gattCharacteristics = gattService.getCharacteristics();

                Iterator var11 = gattCharacteristics.iterator();

                while (var11.hasNext()) {
                    BluetoothGattCharacteristic gattCharacteristic = (BluetoothGattCharacteristic) var11.next();
                    Log.d("gattCharacteristic--->" + gattCharacteristic.getUuid().toString());
                    charas.add(gattCharacteristic);
                }
            }

            this.setCharacteristicNotificationAndReadValue(charas);
        }
    }

    private void setCharacteristicNotificationAndReadValue(ArrayList<BluetoothGattCharacteristic> mGattCharacteristics2) {
        for (int i = 0; i < mGattCharacteristics2.size(); ++i) {
            BluetoothGattCharacteristic characteristic = mGattCharacteristics2.get(i);
            String uuid = characteristic.getUuid().toString();
            if (uuid.equals("0000b351-0000-1000-8000-00805f9b34fb")) {
                this.FOR_SERIAL_PORT_READ_Characteristic = characteristic;
                this.setCharacteristicNotification(characteristic, true);
            } else if (uuid.equals("0000b352-0000-1000-8000-00805f9b34fb")) {
                this.SERIAL_PORT_WRITE_Characteristic = characteristic;
            } else if (uuid.equals("0000b353-0000-1000-8000-00805f9b34fb")) {
                this.MAX_PACKET_SIZE_Characteristic = characteristic;
            } else if (uuid.equals("0000b354-0000-1000-8000-00805f9b34fb")) {
                this.NO_RESPONSE_MAX_PACKET_COUNT_Characteristic = characteristic;
            } else if (uuid.equals("0000b355-0000-1000-8000-00805f9b34fb")) {
                this.DEVICE_RECEIVED_PACKET_SEQUENCE_Characteristic = characteristic;
                this.setCharacteristicNotification(characteristic, true);
            } else if (uuid.equals("0000b356-0000-1000-8000-00805f9b34fb")) {
                this.HOST_RECEIVED_PACKET_SEQUENCE_Characteristic = characteristic;
            } else if (uuid.equals("0000b357-0000-1000-8000-00805f9b34fb")) {
                this.PACKET_TIMEOUT_Characteristic = characteristic;
            } else if (uuid.equals("0000b358-0000-1000-8000-00805f9b34fb")) {
                this.DEVICE_RECEIVED_ERROR_PACKET_SEQUENCE_Characteristic = characteristic;
                this.setCharacteristicNotification(characteristic, true);
            } else if (uuid.equals("0000b359-0000-1000-8000-00805f9b34fb")) {
                this.HOST_RECEIVED_ERROR_PACKET_SEQUENCE_Characteristic = characteristic;
            } else if (uuid.equals("0000b35A-0000-1000-8000-00805f9b34fb")) {
                this.DEVICE_CAN_RECEIVE_PACKET_Characteristic = characteristic;
                this.setCharacteristicNotification(characteristic, true);
            } else if (uuid.equals("0000b35B-0000-1000-8000-00805f9b34fb")) {
                this.HOST_CAN_RECEIVE_PACKET_Characteristic = characteristic;
            } else if (uuid.equals("0000b35C-0000-1000-8000-00805f9b34fb")) {
                this.RESET_SEQUENCE_Characteristic = characteristic;
            }
        }

        startReadCharacteristic();
        mServiceHnadler.postDelayed(this.mReadCharacteristicTimeoutRunnable, 3000L);
    }

    private long lastSendTime = 0;

    public synchronized void wirte(byte[] data) {
        if (data.length > this.sendDataNumber) {
            String intentAction = BlueToothParams.ACTION_GATT_SENDDATALENGTHEXCEED;
            Log.i(TAG, "intentAction: " + intentAction);
            this.broadcastUpdate(intentAction);
        } else {
            long nowTime = System.currentTimeMillis();
            if (nowTime - lastSendTime < S_SEND_PACKET_INTERVAL) {
                return;
            }
            this.state = 1;
            if (arrayWaitSendData == null) {
                this.arrayWaitSendData = new Vector<byte[]>();
            } else {
                arrayWaitSendData.clear();
            }
            this.arrayWaitResponseData = new ArrayList();
            Log.i(TAG, "发送开始序号 " + Tools.byteToHexString((byte) this.nNextSendPacketSequence));
            Log.i(TAG, "====SelectBlue sendData");
            this.initSendDataToPeer(data);
            writeDataPacket();
            lastSendTime = nowTime;
        }

    }

    public void closeSendtimer() {
        if (this.dataTimer != null) {
            this.dataTimer.cancel();
            this.dataTimer = null;
        }

//        if (this.sendtimer != null) {
//            this.sendtimer.cancel();
//            this.sendtimer = null;
//        }

    }

    public synchronized boolean initSendDataToPeer(byte[] data) {
        Log.i(TAG, "====ble send data:" + HexUtils.getFormatHex(data));
        int nIndex = 0;
        boolean nLength = true;

        int nLength1;
        for (; nIndex < data.length; nIndex += nLength1) {
            nLength1 = this.nMaxPacketSize - 2;
            if (data.length - nIndex < nLength1) {
                nLength1 = data.length - nIndex;
            }

            byte[] newData = new byte[nLength1 + 2];
            System.arraycopy(data, nIndex, newData, 1, nLength1);
            this.arrayWaitSendData.add(newData);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("====ble arrayWaitSendData:");
        for (byte[] bytes : arrayWaitSendData) {
            sb.append(HexUtils.getFormatHex(bytes));
            sb.append(" ");
        }
        Log.i(TAG, sb.toString());
        this.nNextSendDataIndex = 0;
        this.timeincrease = 0;
        this.answerDataPacketSum = 0;
//        if (this.sendtimer != null) {
//            this.sendtimer.cancel();
//            this.sendtimer = null;
//        }

        if (this.dataTimer == null) {
            this.dataTimer = new Timer();
        }
//        this.sendtimer = new BluetoothLeService.SendPacketTimer();
//        this.dataTimer.schedule(this.sendtimer, 0L, this.DEFAULT_SEND_PACKET_INTERVAL);
        return true;
    }

    private synchronized void writeDataPacket() {
        if (this.bPeerCanReceive && !pauseSend && this.arrayWaitSendData.size() > 0 && this.arrayWaitResponseData.size() < this.nNoResponseAllowMaxPacketCount) {
//            byte[] buffer = (byte[]) this.arrayWaitSendData.get(0);
            byte[] buffer = this.arrayWaitSendData.remove(0);
            Log.i(TAG, "====ble send data 0:" + HexUtils.getFormatHex(buffer));

            if (nNextSendPacketSequence != mSuccessSendSequence) {
                Log.i(TAG,  "nNextSendPacketSequence:" + nNextSendPacketSequence);
                Log.i(TAG,  "mSuccessSendSequence" + mSuccessSendSequence);
                nNextSendPacketSequence = mSuccessSendSequence;
            }

            buffer[0] = (byte) this.nNextSendPacketSequence;
            buffer[buffer.length - 1] = Tools.checksum(buffer, buffer.length - 1);
            this.dqSendPacketSequence = this.nNextSendPacketSequence;
            idbyteid = this.nNextSendPacketSequence++;
            if (this.nNextSendPacketSequence >= 255) {
                this.nNextSendPacketSequence = 0;
            }

            if (mConnectionState == 4 && this.stateReadSuccess == 5) {
                this.senddatatime = this.timeincrease;
                this.SERIAL_PORT_WRITE_Characteristic.setValue(buffer);
                this.SERIAL_PORT_WRITE_Characteristic.setWriteType(1);
                this.wirteCharacteristic(this.SERIAL_PORT_WRITE_Characteristic);
//                Log.i(TAG, "====ble send buffer:" + HexUtils.getFormatHex(buffer));
//                this.arrayWaitResponseData.add(buffer);
                Log.i(TAG, "====ble send data 1:" + HexUtils.getFormatHex(buffer));
                try {
                    Thread.sleep(3);
                } catch (InterruptedException e) {
                }
            }
        }

    }

    public void velocityWirte(byte[] data) {
        if (this.SERIAL_PORT_WRITE_Characteristic != null) {
            this.state = 0;
            Log.i(TAG, "发送数据包序号" + Tools.byteToHexString(data[0]));
            data[data.length - 1] = Tools.checksum(data, data.length - 1);
            this.SERIAL_PORT_WRITE_Characteristic.setValue(data);
            this.SERIAL_PORT_WRITE_Characteristic.setWriteType(1);
            this.wirteCharacteristic(this.SERIAL_PORT_WRITE_Characteristic);
        }

    }

    private void testdatanr(byte[] buffer) {
        String sll = "";

        for (int i = 0; i < buffer.length; ++i) {
            byte b = buffer[i];
            sll = sll + Tools.byteToHexString(b);
        }

    }

    private void startReadCharacteristic() {
        for (int e = 0, i = 100; e < 3; ++e, i += 50) {
            readCharacteristicHandler.sendEmptyMessageDelayed(e, (e + 1) * i);
        }
    }

    public void closeIncreaseTimer() {
        if (this.timeIncreaseTimer != null) {
            this.timeIncreaseTimer.cancel();
            this.timeIncreaseTimer = null;
        }

        if (this.tit != null) {
            this.tit.cancel();
            this.tit = null;
        }

    }

    public void closetimer() {
        if (this.dataTimer != null) {
            this.dataTimer.cancel();
            this.dataTimer = null;
        }

        if (this.sdt != null) {
            this.sdt.cancel();
            this.sdt = null;
        }

    }

    public void recvDataPackeSequenced(byte nSeq, int uuid) {
        Object sendDataIndex = null;
        Object sequenceint = null;
        byte[] buffer = null;
        Object start = null;
        Object end = null;
        this.senddatatime = this.timeincrease;
        if (this.arrayWaitResponseData != null) {
            int nDataIndex;
            for (nDataIndex = 0; nDataIndex < this.arrayWaitResponseData.size(); ++nDataIndex) {
                buffer = this.arrayWaitResponseData.get(nDataIndex);
                if (buffer.length == 0) {
                    return;
                }

                byte nDataSeq = buffer[0];
                if (nDataSeq == nSeq) {
                    break;
                }
            }

            int i;
            if (nDataIndex >= this.arrayWaitResponseData.size()) {
                if (this.arrayWaitResponseData.size() > 0) {
                    byte[] var11 = this.arrayWaitResponseData.get(0);
                    byte[] var12 = this.arrayWaitResponseData.get(this.arrayWaitResponseData.size() - 1);
                    Log.i(TAG, "应答序号不合法,应该在" + Tools.byteToHexString(var11[0]) + "和" + Tools.byteToHexString(var12[0]));
                    if (this.arrayWaitResponseData.size() == 1) {
                        for (i = this.arrayWaitResponseData.size() - 1; i >= 0; --i) {
                            buffer = this.arrayWaitResponseData.get(i);
                            this.arrayWaitSendData.add(lastResendindex, buffer);
                        }

                        this.arrayWaitResponseData = new ArrayList();
                    }
                }

                return;
            }

            if (uuid == 5) {
                for (i = 0; i <= nDataIndex; ++i) {
                    this.arrayWaitResponseData.remove(0);
                }

                if (this.arrayWaitSendData.size() == 0 && this.arrayWaitResponseData.size() == 0) {
                    this.arrayWaitSendData = new Vector();
                    this.broadcastUpdate(BlueToothParams.ACTION_GATT_SENDDATAEND);
                    this.arrayWaitResponseData = new ArrayList();
                }

                this.nNoResponseAllowMaxPacketCount = 10;
            } else {
                this.closeSendtimer();
                pauseSend = true;
                lastResendindex = 0;

                for (i = this.arrayWaitResponseData.size() - 1; i >= nDataIndex; --i) {
                    buffer = this.arrayWaitResponseData.get(i);
                    Log.i(TAG, "====ble arrayWaitSendData.add");
                    this.arrayWaitSendData.add(0, buffer);
                    ++lastResendindex;
                }

                this.arrayWaitResponseData = new ArrayList();
                if (buffer != null && buffer.length > 0) {
                    this.nNextSendPacketSequence = buffer[0];
                    if (this.nNextSendPacketSequence < 0) {
                        this.nNextSendPacketSequence += 256;
                    }
                }

                this.nNoResponseAllowMaxPacketCount = 1;
                pauseSend = false;
//                if (this.sendtimer != null) {
//                    this.sendtimer.cancel();
//                    this.sendtimer = null;
//                }

                if (this.dataTimer == null) {
                    this.dataTimer = new Timer();
                }
                writeDataPacket();
//                this.sendtimer = new BluetoothLeService.SendPacketTimer();
//                this.dataTimer.schedule(this.sendtimer, 500L);
            }
        }

    }

    public boolean  recvDataFromPeer(byte[] data) {

        if (data.length >= 2) {
            byte nCurrChecksum = Tools.checksum(data, data.length - 1);
            byte[] respPacket;
            if (data[data.length - 1] == nCurrChecksum) {

                Log.i(TAG, "Checksum success");
                Log.i(TAG,  "response sequence:" + data[0]);
                Log.i(TAG,  "nNextWantRecvPacketSequence:" + nNextWantRecvPacketSequence);

                if (data[0] == (byte) this.nNextWantRecvPacketSequence) {

                    this.didDataReceived(data, data.length - 2);
                    ++this.nNextWantRecvPacketSequence;
                    if (this.nNextWantRecvPacketSequence >= 255) {
                        this.nNextWantRecvPacketSequence = 0;
                    }

                    respPacket = new byte[]{data[0]};
                    this.writeResponsePacket(respPacket, false);
                } else if (this.IsHistoryPacketSeq(data[0])) {
                    respPacket = new byte[]{data[0]};
                    Log.i(TAG, "====ble IsHistoryPacketSeq");
                    this.writeResponsePacket(respPacket, false);
                } else {
                    respPacket = new byte[]{(byte) this.nNextWantRecvPacketSequence};
                    this.writeResponsePacket(respPacket, true);
                }
            } else {

                Log.i(TAG, "Checksum failed");

                respPacket = new byte[]{(byte) this.nNextWantRecvPacketSequence};
                this.writeResponsePacket(respPacket, true);
            }

            return true;
        } else {
            return false;
        }
    }

    public void didDataReceived(byte[] buffer, int length) {

        byte[] data = new byte[length];
        System.arraycopy(buffer, 1, data, 0, length);
        this.broadcastUpdate(BlueToothParams.ACTION_GATT_DATARECEIVED, data);

//        i("bleBug", "buffer:" + HexUtils.getFormatHex(buffer) + " length:" + length);
        Log.i(TAG,  "didDataReceived:" + HexUtils.getFormatHex(data));
    }

    public void writeResponsePacket(byte[] respPacket, boolean error) {
        if (error) {
            this.HOST_RECEIVED_ERROR_PACKET_SEQUENCE_Characteristic.setValue(respPacket);
            this.wirteCharacteristic(this.HOST_RECEIVED_ERROR_PACKET_SEQUENCE_Characteristic);
        } else if (!error) {
            this.HOST_RECEIVED_PACKET_SEQUENCE_Characteristic.setValue(respPacket);
            this.wirteCharacteristic(this.HOST_RECEIVED_PACKET_SEQUENCE_Characteristic);
        }
    }

    boolean IsHistoryPacketSeq(byte nRecvedSeq) {
        return (this.nNextWantRecvPacketSequence + 255 - nRecvedSeq) % 255 < this.nNoResponseAllowMaxPacketCounte;
    }

    private class IncreaseTimer extends TimerTask {
        private IncreaseTimer() {
        }

        public void run() {
            timeincrease = timeincrease + 1;
            if (timeIncreaseTimer != null) {
                if (tit != null) {
                    tit.cancel();
                    tit = null;
                }

                tit = new IncreaseTimer();
                timeIncreaseTimer.schedule(tit, (long) TimeIncreaseMagnitude);
            }

        }
    }

    public class LocalBinder extends Binder {
        public LocalBinder() {
        }

        public BluetoothLeService getService() {
            mConnectionState = 0;
            return BluetoothLeService.this;
        }
    }

    private class SendDataTimer extends TimerTask {
        private SendDataTimer() {
        }

        public void run() {
            if (arrayWaitSendData != null && arrayWaitSendData.size() > 0) {
                int time = (timeincrease - senddatatime) * TimeIncreaseMagnitude;
                if ((long) time >= S_SEND_PACKET_INTERVAL) {
                    nNextSendPacketSequence = dqSendPacketSequence;
                    nNextSendDataIndex = dqSendDataIndex;
                    writeDataPacket();
                }

                if (sdt != null) {
                    sdt.cancel();
                    sdt = null;
                }

                if (dataTimer == null) {
                    dataTimer = new Timer();
                }

                sdt = new SendDataTimer();
                dataTimer.schedule(sdt, DEFAULT_SEND_PACKET_INTERVAL);
            } else {
                closetimer();
            }

        }
    }

//    private class SendPacketTimer extends TimerTask {
//        private SendPacketTimer() {
//        }
//
//        public void run() {
//            if (arrayWaitSendData != null && arrayWaitSendData.size() > 0) {
//                writeDataPacket();
//                if (sendtimer != null) {
//                    sendtimer.cancel();
//                    sendtimer = null;
//                }
//
//                if (dataTimer == null) {
//                    dataTimer = new Timer();
//                }
//
//                sendtimer = new SendPacketTimer();
//                dataTimer.schedule(sendtimer, 5L);
//            } else {
//                closeSendtimer();
//            }
//
//        }
//    }

}
