package com.zhichu.nativeplugin.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;

import com.zhichu.nativeplugin.ble.scan.IScanCallback;
import com.zhichu.nativeplugin.ble.scan.ScanCallback;
import com.zhichu.nativeplugin.ble.scan.SingleFilterScanCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Ble {
    private static final String TAG = "Ble";
    private Context context;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver broadcastReceiver;
    private IBleStateChangedCallback bleStateChangedCallback;
    private ScanCallback scanCallback;
    private PeripheralDevicePool peripheralDevicePool;

    private static class BleHolder {
        private static final Ble instance = new Ble();
    }

    private Ble() {
        peripheralDevicePool = new PeripheralDevicePool();
    }

    public static Ble get() {
        return BleHolder.instance;
    }

    /**
     * 设置蓝牙状态监听器
     *
     * @param callback
     */
    public void setBleStateChangedCallback(IBleStateChangedCallback callback) {
        this.bleStateChangedCallback = callback;
    }

    /**
     * 初始化蓝牙
     *
     * @param context
     */
    public void init(Context context) {
        if (this.context == null && context != null) {
            this.context = context.getApplicationContext();
            bluetoothManager = (BluetoothManager) this.context
                    .getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null) {
                pringLog("BluetoothAdapter NULL");
                return;
            }
            registerReceiver();
            pringLog("Ble Init Successful");
        }
    }

    /**
     * 销毁
     */
    public void uninit() {
        if (this.context != null) {
            if (bluetoothAdapter != null) {
                unregisterReceiver();
                this.scanCallback = null;
                this.bleStateChangedCallback = null;
                disconnect();
            }
            this.context = null;
            pringLog("Ble Uninited Successful");
        }
    }

    private void unregisterReceiver() {
        if (broadcastReceiver != null) {
            this.context.unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
    }

    private void registerReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    if (bleStateChangedCallback == null) {
                        return;
                    }
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR);
                    bleStateChangedCallback.onState(state);
                }
            }
        };
        this.context.registerReceiver(broadcastReceiver,
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    /**
     * 是否支持BLE
     *
     * @return true 支持
     */
    public boolean isSupportBle() {
        return this.context.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * 是否支持蓝牙
     *
     * @return true 支持
     */
    public boolean isSupportBluetooth() {
        return bluetoothAdapter != null;
    }

    /**
     * 蓝牙是否打开
     *
     * @return
     */
    public boolean isEnabled() {
        return bluetoothAdapter != null ? bluetoothAdapter.isEnabled() : false;
    }

    /**
     * 打开蓝牙
     *
     * @param activity    上下文
     * @param requestCode
     * @return -1 蓝牙不支持  10 蓝牙没有打开 12 蓝牙已经打开 0 正在打开
     */
    public int enableBluetooth(Activity activity, int requestCode) {
        if (bluetoothAdapter == null) {
            return -1;
        }
        if (!bluetoothAdapter.isEnabled()) {
            if (activity != null) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(intent, requestCode);
                return 0;
            } else {
                throw new NullPointerException("Activity Cannot NULL");
            }
        } else {
            pringLog("Bluetooth Is Enabled");
            return 12;
        }
    }

    /**
     * 搜索
     *
     * @param scanCallback 搜索方式
     * @return -1 蓝牙不支持  10 蓝牙没有打开  0 搜索执行成功
     */
    public int scan(ScanCallback scanCallback) {
        if (bluetoothAdapter == null) {
            return -1;
        }
        if (!bluetoothAdapter.isEnabled()) {
            return 10;
        }
        if (this.scanCallback != null && this.scanCallback.isScanning()) {
            this.scanCallback.stop();
        }
        if (scanCallback != null) {
            this.scanCallback = scanCallback;
            this.scanCallback.initBluetoothAdapter(bluetoothAdapter);
            this.scanCallback.scan();
        }
        return 0;
    }

    /**
     * 停止搜索
     *
     * @return -1 蓝牙不支持  10 蓝牙没有打开  0 停止搜索执行成功
     */
    public int stopScan() {
        if (bluetoothAdapter == null) {
            return -1;
        }
        if (!bluetoothAdapter.isEnabled()) {
            return 10;
        }
        if (scanCallback != null && scanCallback.isScanning()) {
            scanCallback.stop();
        }
        return 0;
    }

    /**
     * 通过蓝牙设备连接
     *
     * @param bleDevice       蓝牙对象 {@link IScanCallback#onDeviceFound(BleDevice, List)}
     * @param connectCallback
     */
    public void connect(BleDevice bleDevice, IConnectCallback connectCallback) {
        PeripheralDevice peripheralDevice;
        if (peripheralDevicePool.contains(bleDevice.getDeviceUUID())) {
            peripheralDevice = peripheralDevicePool.getPeripheralDevice(bleDevice.getDeviceUUID());
            if (!peripheralDevice.isConnected()) {
                pringLog("BleDevice "
                        + bleDevice.getDeviceUUID() + " Existed But Not Connected");
            }
        } else {
            peripheralDevice = new PeripheralDevice(context, bleDevice);
            peripheralDevicePool.addPeripheralDevice(peripheralDevice);
        }
        if (peripheralDevice.isConnected()) {
            if (connectCallback != null) {
                connectCallback.onConnectSuccess(bleDevice);
            }
            return;
        }
        peripheralDevice.connect(connectCallback);
    }

    /**
     * 通过蓝牙UUID或者MAC地址直接连接
     *
     * @param deviceUUID      蓝牙UUID或者MAC地址
     * @param connectCallback
     */
    public void connect(String deviceUUID, IConnectCallback connectCallback) {
        PeripheralDevice peripheralDevice = null;
        if (peripheralDevicePool.contains(deviceUUID)) {
            peripheralDevice = peripheralDevicePool.getPeripheralDevice(deviceUUID);
            if (!peripheralDevice.isConnected()) {
                pringLog("BleDevice " + deviceUUID + " Existed But Not Connected");
            }
        } else {
            if (bluetoothAdapter.checkBluetoothAddress(deviceUUID)) {
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceUUID);
                BleDevice bleDevice = null;
                Map<String, BleDevice> maps = getLastScanFinishedDeviceMap();
                if (maps != null && !maps.isEmpty()) {
                    if (maps.containsKey(deviceUUID)) {
                        bleDevice = maps.get(deviceUUID);
                        bleDevice.setBluetoothDevice(device);
                    }
                }
                if (bleDevice == null) {
                    bleDevice = new BleDevice(device);
                }
                peripheralDevice = new PeripheralDevice(context, bleDevice);
                peripheralDevicePool.addPeripheralDevice(peripheralDevice);
            }
        }
        if (peripheralDevice == null) {
            connectCallback.onFailure(null, -1, "BluetoothDevice NULL");
            return;
        }

        peripheralDevice.connect(connectCallback);
    }

    /**
     * 通过蓝牙UUID或者MAC地址连接，先搜索后连接
     *
     * @param deviceUUID      蓝牙设备UUID或者MAC地址
     * @param connectCallback
     * @return -1 蓝牙不支持  10 蓝牙没有打开  12 搜索执行成功
     */
    public int scanConnect(String deviceUUID, final IConnectCallback connectCallback) {
        return scan(new SingleFilterScanCallback(new IScanCallback() {
            @Override
            public void onDeviceFound(BleDevice bleDevice, List<BleDevice> result) {
                connect(bleDevice, connectCallback);
            }

            @Override
            public void onScanFinish(List<BleDevice> result) {
            }

            @Override
            public void onScanTimeout() {
                if (connectCallback != null) {
                    connectCallback.onFailure(null, -1, "Invalid deviceUUID");
                }
            }
        }).setDeviceUUID(deviceUUID));
    }

    /**
     * 根据蓝牙UUID或者MAC地址断开当前连接
     *
     * @param deviceUUID 蓝牙设备UUID或者MAC地址
     * @return false 蓝牙设备未连接
     */
    public boolean disconnect(String deviceUUID) {
        if (peripheralDevicePool.contains(deviceUUID)) {
            peripheralDevicePool.getPeripheralDevice(deviceUUID).disconnect();
            peripheralDevicePool.removePeripheralDevice(deviceUUID);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 断开所有连接
     */
    public void disconnect() {
        for (PeripheralDevice peripheralDevice : peripheralDevicePool.values()) {
            if (peripheralDevice.isConnected()) {
                peripheralDevice.disconnect();
            }
        }
        peripheralDevicePool.clear();
    }

    /**
     * 读取数据
     *
     * @param deviceUUID         蓝牙设备UUID或者MAC地址
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 读取特征UUID
     * @param readCallback
     */
    public void read(String deviceUUID, String serviceUUID, String characteristicUUID,
                     IReadCallback readCallback) {
        if (peripheralDevicePool.contains(deviceUUID)) {
            peripheralDevicePool.getPeripheralDevice(deviceUUID).read(UUIDHelper.uuidFromString(serviceUUID),
                    UUIDHelper.uuidFromString(characteristicUUID), readCallback);
        } else {
            if (readCallback != null) {
                readCallback.onFailure(null, -1, "BleDevice not connected");
            }
        }
    }

    /**
     * 写数据 ，需要回复
     *
     * @param deviceUUID         蓝牙设备UUID或者MAC地址
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 写特征UUID
     * @param data               数据
     * @param writeCallback
     */
    public synchronized void write(String deviceUUID, String serviceUUID, String characteristicUUID,
                                   byte[] data, IWriteCallback writeCallback) {
        if (peripheralDevicePool.contains(deviceUUID)) {
            peripheralDevicePool.getPeripheralDevice(deviceUUID).write(UUIDHelper.uuidFromString(serviceUUID),
                    UUIDHelper.uuidFromString(characteristicUUID), data,
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT, writeCallback);
        } else {
            if (writeCallback != null) {
                writeCallback.onFailure(null, -1, "BleDevice not connected");
            }
        }
    }

    /**
     * 写数据 ，不需要回复
     *
     * @param deviceUUID         蓝牙设备UUID或者MAC地址
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 写特征UUID
     * @param data               数据
     */
    public synchronized void writeNoResp(String deviceUUID, String serviceUUID, String characteristicUUID,
                                         byte[] data, IWriteCallback writeCallback) {
        if (peripheralDevicePool.contains(deviceUUID)) {
            peripheralDevicePool.getPeripheralDevice(deviceUUID).write(UUIDHelper.uuidFromString(serviceUUID),
                    UUIDHelper.uuidFromString(characteristicUUID), data,
                    BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE, writeCallback);
        } else {
            if (writeCallback != null) {
                writeCallback.onFailure(null, -1, "BleDevice not connected");
            }
        }
    }

    /**
     * 注册通知
     *
     * @param deviceUUID         蓝牙设备UUID或者MAC地址
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 通知特征UUID
     * @param notifyCallback
     */
    public synchronized void registerNotify(String deviceUUID, String serviceUUID, String characteristicUUID,
                                            INotifyCallback notifyCallback) {
        if (peripheralDevicePool.contains(deviceUUID)) {
            peripheralDevicePool.getPeripheralDevice(deviceUUID).registerNotify(UUIDHelper.uuidFromString(serviceUUID),
                    UUIDHelper.uuidFromString(characteristicUUID), notifyCallback);
        } else {
            if (notifyCallback != null) {
                notifyCallback.onFailure(null, -1, "BleDevice not connected");
            }
        }
    }

    /**
     * 取消通知注册
     *
     * @param deviceUUID         蓝牙设备UUID或者MAC地址
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 通知特征UUID
     * @param notifyCallback
     */
    public synchronized void unregisterNotify(String deviceUUID, String serviceUUID, String characteristicUUID,
                                              INotifyCallback notifyCallback) {
        if (peripheralDevicePool.contains(deviceUUID)) {
            peripheralDevicePool.getPeripheralDevice(deviceUUID).unregisterNotify(UUIDHelper.uuidFromString(serviceUUID),
                    UUIDHelper.uuidFromString(characteristicUUID), notifyCallback);
        } else {
            if (notifyCallback != null) {
                notifyCallback.onFailure(null, -1, "BleDevice not connected");
            }
        }
    }

    /**
     * 读取RSSI
     *
     * @param deviceUUID   蓝牙设备UUID或者MAC地址
     * @param rssiCallback
     */
    public void readRssi(String deviceUUID, IRssiCallback rssiCallback) {
        if (peripheralDevicePool.contains(deviceUUID)) {
            peripheralDevicePool.getPeripheralDevice(deviceUUID).readRssi(rssiCallback);
        } else {
            if (rssiCallback != null) {
                rssiCallback.onFailure(null, -1, "BleDevice not connected");
            }
        }
    }

    /**
     * 获取最后一次扫描的列表
     *
     * @return 蓝牙设备集合
     */
    public List<BleDevice> getLastScanFinishedDeviceList() {
        if (this.scanCallback != null) {
            this.scanCallback.getLastScanFinishedDeviceList();
        }
        return Collections.emptyList();
    }


    Map<String, BleDevice> getLastScanFinishedDeviceMap() {
        if (this.scanCallback != null) {
            return this.scanCallback.getLastScanFinishedDeviceMap();
        }
        return null;
    }

    /***
     * 获取已连接的设备列表
     *
     * @return 蓝牙设备集合
     */
    public List<BleDevice> getConnectedPeripheralDeviceList() {
        List<BleDevice> bleDeviceList = new ArrayList<>();
        for (PeripheralDevice peripheralDevice : peripheralDevicePool.values()) {
            if (peripheralDevice.isConnected()) {
                bleDeviceList.add(peripheralDevice.getBleDevice());
            }
        }
        return bleDeviceList;
    }

    /**
     * 添加被动通知回调
     *
     * @param deviceUUID            蓝牙设备UUID或者MAC地址
     * @param bleNotifyDataCallback
     * @return false 设备未连接
     */
    public boolean addBleNotifyDataCallback(String deviceUUID, IBleNotifyDataCallback bleNotifyDataCallback) {
        if (peripheralDevicePool.contains(deviceUUID)) {
            PeripheralDevice peripheralDevice = peripheralDevicePool.getPeripheralDevice(deviceUUID);
            peripheralDevice.addBleNotifyDataCallback(bleNotifyDataCallback);
        } else {
            return false;
        }
        return true;
    }

    /**
     * 移除被动通知回调
     *
     * @param deviceUUID            蓝牙设备UUID或者MAC地址
     * @param bleNotifyDataCallback
     * @return false 设备未连接
     */
    public boolean removeBleNotifyDataCallback(String deviceUUID, IBleNotifyDataCallback bleNotifyDataCallback) {
        if (peripheralDevicePool.contains(deviceUUID)) {
            PeripheralDevice peripheralDevice = peripheralDevicePool.getPeripheralDevice(deviceUUID);
            peripheralDevice.removeBleNotifyDataCallback(bleNotifyDataCallback);
        } else {
            return false;
        }
        return true;
    }

    /**
     * 是否已经连接
     *
     * @param deviceUUID 蓝牙设备UUID或者MAC地址
     * @return true 已连接
     */
    public boolean isConnected(String deviceUUID) {
        if (peripheralDevicePool.contains(deviceUUID)) {
            PeripheralDevice peripheralDevice = peripheralDevicePool.getPeripheralDevice(deviceUUID);
            if (peripheralDevice.isConnected()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据蓝牙设备UUID或者MAC地址获取已连接的蓝牙设备
     *
     * @param deviceUUID
     * @return
     */
    public BleDevice getConnectedBleDevice(String deviceUUID) {
        if (peripheralDevicePool.contains(deviceUUID)) {
            return peripheralDevicePool.getPeripheralDevice(deviceUUID).getBleDevice();
        }
        return null;
    }

    /**
     * 检查当前蓝牙状态 ,检查结果在监听器中返回
     *
     * @return
     */
    public void checkState() {
        if (bluetoothAdapter != null) {
            int state = bluetoothAdapter.getState();
            bleStateChangedCallback.onState(state);
        }
    }

    /**
     * 获取服务列表
     *
     * @param deviceUUID 蓝牙设备UUID或者MAC地址
     * @return
     */
    public List<BluetoothGattService> getGattServiceList(String deviceUUID) {
        if (peripheralDevicePool.contains(deviceUUID)) {
            return peripheralDevicePool.getPeripheralDevice(deviceUUID).getGattServiceList();
        }
        return Collections.emptyList();
    }

    /**
     * 获取某个服务的特征值列表
     *
     * @param deviceUUID  蓝牙设备UUID或者MAC地址
     * @param serviceUUID 服务UUID
     * @return
     */
    public List<BluetoothGattCharacteristic> getGattCharacteristicList(String deviceUUID, String serviceUUID) {
        if (peripheralDevicePool.contains(deviceUUID)) {
            return peripheralDevicePool.getPeripheralDevice(deviceUUID)
                    .getGattCharacteristicList(UUIDHelper.uuidFromString(serviceUUID));
        }
        return Collections.emptyList();
    }

    /**
     * 获取某个特征值的描述属性列表
     *
     * @param deviceUUID         蓝牙设备UUID或者MAC地址
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return
     */
    public List<BluetoothGattDescriptor> getGattDescriptorList(String deviceUUID,
                                                               String serviceUUID, String characteristicUUID) {
        if (peripheralDevicePool.contains(deviceUUID)) {
            return peripheralDevicePool.getPeripheralDevice(deviceUUID)
                    .getGattDescriptorList(UUID.fromString(serviceUUID), UUID.fromString(characteristicUUID));
        }
        return Collections.emptyList();
    }

    void pringLog(String message) {
        Log.d(TAG, message);
    }

}