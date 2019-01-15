package com.ut.unilink.cloudLock;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.ut.unilink.jobluetooth.BlueToothParams;
import com.ut.unilink.jobluetooth.BluetoothLeService;
import com.ut.unilink.jobluetooth.DataAssemble;
import com.ut.unilink.util.Log;

import static android.content.Context.BIND_AUTO_CREATE;

public class JinouxBleLink extends BaseBleLink {

    private BluetoothLeService bluetoothLeService;
    private String address;
    private ConnectListener connectListener;
    private Context context;
    private static final String TAG = "bleTest";
    private Handler handler;
    private boolean isBind;

    public static final int CODE_JINOUX_BLE_DISCONNECTED = -101;
    public static final int CODE_JINOUX_BLE_CONNECT_TIMEOUT = -102;
    public static final int CODE_JINOUX_BLE_CLOSE = -103;

    public JinouxBleLink(Context context) {
        this.context = context;
        handler = new Handler(Looper.getMainLooper());
        Intent intent = new Intent(context, BluetoothLeService.class);
        context.bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        context.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    public void connect(final String address, ConnectListener connectListener) {
        this.address = address;
        this.connectListener = connectListener;
        handler.post(new Runnable() {
            @Override
            public void run() {
                bluetoothLeService.connect(address, 5);
            }
        });
    }

    @Override
    void send(String address, byte[] data) {
        bluetoothLeService.wirte(data);
        Log.i(TAG, "jinoux send:" + Log.toUnsignedHex(data));
    }

    @Override
    void close(String address) {
        handleDisconnect(CODE_JINOUX_BLE_CLOSE);
        if (isBind) {
            isBind = false;
            context.unbindService(serviceConnection);
        }
    }

    private String getCodeMsg(int code) {
        String msg = "unknow error";

        switch (code) {
            case CODE_JINOUX_BLE_DISCONNECTED:
                msg =  "jinoux bluetooth disconnected";
                break;

            case CODE_JINOUX_BLE_CONNECT_TIMEOUT:
                msg =  "jinoux bluetooth connect timeout";
                break;

                default:
        }
        return msg;
    }

    private void handleDisconnect(final int code) {
        bluetoothLeService.disconnect(-1);

        if (mConnectionManager != null) {
            mConnectionManager.onDisConnect(address, code);
        }

        if (connectListener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    connectListener.onDisconnect(code, getCodeMsg(code));
                }
            });
        }
    }

    private void handleConnect() {
        if (mConnectionManager != null) {
            mConnectionManager.onConnect(address);
        }

        if (connectListener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    connectListener.onConnect();
                }
            });
        }

        DataAssemble.get().setReceiveCallback(new DataAssemble.ReceiveCallback() {
            @Override
            public void onReceiveSuccess(byte[] data) {
                Log.i(TAG, "receive data:" + Log.toUnsignedHex(data));
                mConnectionManager.onReceive(address, data);
            }
        });
    }

    private void handleReceiveData(byte[] data) {
        if (mConnectionManager != null) {
            DataAssemble.get().receiveData(data);
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bluetoothLeService = ((BluetoothLeService.LocalBinder)service).getService();
            bluetoothLeService.initialize();
            isBind = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBind = false;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.i(TAG, "====action11 = " + action);
            //连接成功 并读取characteristic成功
            switch (action) {
                case BlueToothParams.ACTION_GATT_READCHARACTERISTICSUCCESS:
                    Log.i(TAG, "ACTION_GATT_READCHARACTERISTICSUCCESS");
                    handleConnect();
                    break;

                case BlueToothParams.ACTION_GATT_DISCONNECTED:
                    Log.i(TAG, "bluetooth disconnected");
                    handleDisconnect(CODE_JINOUX_BLE_DISCONNECTED);
                    break;

                case BlueToothParams.ACTION_GATT_CONNECTTIMEOUT:
                    Log.i(TAG, "bluetooth connect timeout,  blueblooth ");
                    handleDisconnect(CODE_JINOUX_BLE_CONNECT_TIMEOUT);
                    break;

                case BlueToothParams.ACTION_GATT_CONNECTED:
                    Log.i( TAG, "bluetooth connected");
                    break;

                case BlueToothParams.ACTION_GATT_DATARECEIVED:

                    byte[] data = intent.getByteArrayExtra(BluetoothLeService.DATA_NAME);
                    handleReceiveData(data);
                    break;

                case BlueToothParams.ACTION_GATT_READCHARACTERISTICERROR:
                    Log.i(TAG, "ACTION_GATT_READCHARACTERISTICERROR");
                    break;

                case BlueToothParams.ACTION_GATT_HANDLERDATAERROR:
                    Log.i(TAG, "ACTION_GATT_HANDLERDATAERROR");
                    break;

                case BlueToothParams.ACTION_GATT_SENDDATALENGTHEXCEED:
                    Log.i(TAG, "ACTION_GATT_SENDDATALENGTHEXCEED");
                    break;

                case BlueToothParams.ACTION_GATT_SENDDATAERROR:
                    Log.i(TAG, "ACTION_GATT_SENDDATAERROR");
                    break;

                case BlueToothParams.ACTION_GATT_SENDDATAEND:
                    Log.i(TAG, "ACTION_GATT_SENDDATAEND");
                    break;

                case BlueToothParams.ACTION_GATT_SENDDATAB355:
                    Log.i(TAG, "ACTION_GATT_SENDDATAB355");
                    break;

                case BlueToothParams.ACTION_GATT_SENDDATAB358:
                    Log.i(TAG, "ACTION_GATT_SENDDATAB358");
                    break;

                case BlueToothParams.ACTION_GATT_SENDDATASUCCESS:
                    Log.i(TAG, "ACTION_GATT_SENDDATASUCCESS");
                    break;

                default:

                    break;
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BlueToothParams.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BlueToothParams.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BlueToothParams.ACTION_GATT_DATARECEIVED);
        intentFilter.addAction(BlueToothParams.ACTION_GATT_READCHARACTERISTICSUCCESS);
        intentFilter.addAction(BlueToothParams.ACTION_GATT_READCHARACTERISTICERROR);
        intentFilter.addAction(BlueToothParams.ACTION_GATT_HANDLERDATAERROR);
        intentFilter.addAction(BlueToothParams.ACTION_GATT_SENDDATALENGTHEXCEED);
        intentFilter.addAction(BlueToothParams.ACTION_GATT_SENDDATAERROR);
        intentFilter.addAction(BlueToothParams.ACTION_GATT_SENDDATAEND);
        intentFilter.addAction(BlueToothParams.ACTION_GATT_SENDDATAB355);
        intentFilter.addAction(BlueToothParams.ACTION_GATT_SENDDATAB358);
        intentFilter.addAction(BlueToothParams.ACTION_GATT_SENDDATASUCCESS);
        intentFilter.addAction(BlueToothParams.ACTION_GATT_CONNECTTIMEOUT);
        return intentFilter;
    }
}
