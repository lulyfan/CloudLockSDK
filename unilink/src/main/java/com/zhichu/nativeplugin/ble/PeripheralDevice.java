package com.zhichu.nativeplugin.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PeripheralDevice extends BluetoothGattCallback {
    private static final String CHARACTERISTIC_NOTIFICATION_CONFIG
            = "00002902-0000-1000-8000-00805f9b34fb";
    private Context context;
    private BleDevice bleDevice;
    private BluetoothGatt bluetoothGatt;
    private volatile ConnectState connectState = ConnectState.CONNECT_INIT;//连接状态
    private boolean isActiveDisconnect = false;//是否主动断开连接

    private INotifyCallback notifyCallback; //通知回调
    private IRssiCallback rssiCallback;//获取信号值回调
    private IWriteCallback writeCallback;//写回调
    private IReadCallback readCallback;//读回调
    private IConnectCallback connectCallback;//连接回调
    private List<IBleNotifyDataCallback> bleNotifyDataCallbackList = new ArrayList<>();//数据通知回调

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        if (newState == BluetoothGatt.STATE_CONNECTED) {
            gatt.discoverServices();
        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            close();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                connectState = ConnectState.CONNECT_DISCONNECT;
                if (connectCallback != null) {
                    connectCallback.onDisconnect(getBleDevice(), isActiveDisconnect);
                }
            } else {
                connectState = ConnectState.CONNECT_FAILURE;
                if (connectCallback != null) {
                    connectCallback.onFailure(getBleDevice(), connectState.getCode(),
                            "Disconnected onConnectionStateChange status " + status);
                }
            }
        } else if (newState == BluetoothGatt.STATE_CONNECTING) {
            connectState = ConnectState.CONNECT_PROCESS;
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        if (status == 0) {
            bluetoothGatt = gatt;
            connectState = ConnectState.CONNECT_SUCCESS;
            isActiveDisconnect = false;
            if (connectCallback != null) {
                connectCallback.onConnectSuccess(getBleDevice());
            }
        } else {
            close();
            connectState = ConnectState.CONNECT_FAILURE;
            if (connectCallback != null) {
                connectCallback.onFailure(getBleDevice(), connectState.getCode(),
                        "Disconnected onServicesDiscovered status " + status);
            }
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        if (readCallback != null) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                byte[] data = characteristic.getValue();
                readCallback.onRead(bleDevice, data, characteristic.getService().getUuid(),
                        characteristic.getUuid());
            } else {
                readCallback.onFailure(getBleDevice(), status, "Read Error " + status);
            }
            readCallback = null;
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        if (writeCallback != null) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                writeCallback.onWrite(characteristic.getService().getUuid(),
                        characteristic.getUuid());
            } else {
                writeCallback.onFailure(getBleDevice(), status, "Write Error " + status);
            }
            writeCallback = null;
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        byte[] data = characteristic.getValue();
        for (IBleNotifyDataCallback callback : bleNotifyDataCallbackList) {
            callback.onNotify(getBleDevice(), data, characteristic.getService().getUuid(),
                    characteristic.getUuid());
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        if (notifyCallback != null) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                notifyCallback.onNotify(descriptor.getCharacteristic().getService().getUuid(),
                        descriptor.getCharacteristic().getUuid());
            } else {
                notifyCallback.onFailure(getBleDevice(), status, "Notify Error " + status);
            }
            notifyCallback = null;
        }
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
        if (rssiCallback != null) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                bleDevice.setRssi(rssi);
                rssiCallback.onRssi(rssi);
            } else {
                rssiCallback.onFailure(getBleDevice(), status, "Rssi Error " + status);
            }
            rssiCallback = null;
        }
    }

    public PeripheralDevice(Context context, BleDevice bleDevice) {
        this.context = context;
        this.bleDevice = bleDevice;
    }

    //change by huangkaifan
    public synchronized void connect(IConnectCallback connectCallback) {
        this.connectCallback = connectCallback;

        if (connectState == ConnectState.CONNECT_SUCCESS) {
            if (connectCallback != null) {
                connectCallback.onConnectSuccess(getBleDevice());
            }
            return;
        }
        if (connectState == ConnectState.CONNECT_PROCESS) {
            if (connectCallback != null) {
                connectCallback.onConnect(getBleDevice());
            }
            return;
        }

        connectState = ConnectState.CONNECT_PROCESS;
        if (bleDevice.getBluetoothDevice() != null) {
            bleDevice.getBluetoothDevice().connectGatt(context, false, this);
        }
    }

    public synchronized void read(UUID serviceUUID, UUID characteristicUUID,
                                  IReadCallback readCallback) {
        if (bluetoothGatt == null) {
            if (readCallback != null) {
                readCallback.onFailure(getBleDevice(), -1,
                        "Read BluetoothGatt Not Found");
            }
            return;
        }
        BluetoothGattService service = bluetoothGatt.getService(serviceUUID);
        BluetoothGattCharacteristic characteristic = CharacteristicHelper.
                findReadableCharacteristic(service, characteristicUUID);
        if (characteristic == null) {
            if (readCallback != null) {
                readCallback.onFailure(getBleDevice(), -1,
                        "Read Characteristic Not Found");
            }
            return;
        }
        this.readCallback = readCallback;
        if (!bluetoothGatt.readCharacteristic(characteristic)) {
            if (readCallback != null) {
                readCallback.onFailure(getBleDevice(), -1,
                        "Read Operation Not Initiated");
            }
            this.readCallback = null;
        }
    }

    public synchronized void write(UUID serviceUUID, UUID characteristicUUID, byte[] data,
                                   int writeType, IWriteCallback writeCallback) {
        if (bluetoothGatt == null) {
            if (writeCallback != null) {
                writeCallback.onFailure(getBleDevice(), -1,
                        "Write BluetoothGatt Not Found");
            }
            return;
        }
        BluetoothGattService service = bluetoothGatt.getService(serviceUUID);
        BluetoothGattCharacteristic characteristic = CharacteristicHelper.
                findWritableCharacteristic(service, characteristicUUID, writeType);
        if (characteristic == null) {
            if (writeCallback != null) {
                writeCallback.onFailure(getBleDevice(), -1,
                        "Write Characteristic Not Found");
            }
            return;
        }
        characteristic.setWriteType(writeType);
        if (BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT == writeType) {
            this.writeCallback = writeCallback;
        }
        characteristic.setValue(data);
        if (bluetoothGatt.writeCharacteristic(characteristic)) {
            if (BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE == writeType) {
                if (writeCallback != null) {
                    writeCallback.onWrite(serviceUUID, characteristicUUID);
                }
            }
        } else {
            if (writeCallback != null) {
                writeCallback.onFailure(getBleDevice(), -1,
                        "Write Operation Not Initiated");
            }
        }
    }

    public void readRssi(IRssiCallback rssiCallback) {
        if (bluetoothGatt == null) {
            if (rssiCallback != null) {
                rssiCallback.onFailure(getBleDevice(), -1,
                        "Write BluetoothGatt Not Found");
            }
            return;
        }
        this.rssiCallback = rssiCallback;
        if (!bluetoothGatt.readRemoteRssi()) {
            if (rssiCallback != null) {
                rssiCallback.onFailure(getBleDevice(), -1, "Rssi Error Read");
            }
            this.rssiCallback = null;
        }
    }

    public synchronized void registerNotify(UUID serviceUUID, UUID characteristicUUID,
                                            INotifyCallback notifyCallback) {
        this.setNotify(serviceUUID, characteristicUUID, true, notifyCallback);
    }

    public synchronized void unregisterNotify(UUID serviceUUID, UUID characteristicUUID,
                                              INotifyCallback notifyCallback) {
        this.setNotify(serviceUUID, characteristicUUID, false, notifyCallback);
    }

    private void setNotify(UUID serviceUUID, UUID characteristicUUID, boolean notify,
                           INotifyCallback notifyCallback) {
        if (bluetoothGatt == null) {
            if (notifyCallback != null) {
                notifyCallback.onFailure(getBleDevice(), -1,
                        "Notify BluetoothGatt Not Found");
            }
            return;
        }
        BluetoothGattService service = bluetoothGatt.getService(serviceUUID);
        BluetoothGattCharacteristic characteristic = CharacteristicHelper.
                findNotifyCharacteristic(service, characteristicUUID);
        if (characteristic == null) {
            if (notifyCallback != null) {
                notifyCallback.onFailure(getBleDevice(), -1,
                        "Notify Characteristic Not Found");
            }
            return;
        }
        if (!bluetoothGatt.setCharacteristicNotification(characteristic, notify)) {
            if (notifyCallback != null) {
                notifyCallback.onFailure(getBleDevice(), -1,
                        "Notify Failed To Register Notification");
            }
            return;
        }
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                UUIDHelper.uuidFromString(CHARACTERISTIC_NOTIFICATION_CONFIG));
        if (descriptor == null) {
            if (notifyCallback != null) {
                notifyCallback.onFailure(getBleDevice(), -1,
                        "Notify Descriptor Not Found");
            }
            return;
        }
        // Prefer notify over indicate
        if ((characteristic.getProperties()
                & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
            descriptor.setValue(notify ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        } else if ((characteristic.getProperties()
                & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
            descriptor.setValue(notify ? BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                    : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        } else {
            Log.d("PeripheralDevice", "Characteristic " + characteristicUUID
                    + " does not have NOTIFY or INDICATE property set1");
        }
        if (bluetoothGatt.writeDescriptor(descriptor)) {
            this.notifyCallback = notifyCallback;
        } else {
            if (notifyCallback != null) {
                notifyCallback.onFailure(getBleDevice(), -1,
                        "Notify Operation Not Initiated");
            }
        }
    }

    public void addBleNotifyDataCallback(IBleNotifyDataCallback bleNotifyDataCallback) {
        if (!bleNotifyDataCallbackList.contains(bleNotifyDataCallback)) {
            bleNotifyDataCallbackList.add(bleNotifyDataCallback);
        }
    }

    public void removeBleNotifyDataCallback(IBleNotifyDataCallback bleNotifyDataCallback) {
        if (bleNotifyDataCallbackList.contains(bleNotifyDataCallback)) {
            bleNotifyDataCallbackList.remove(bleNotifyDataCallback);
        }
    }

    /**
     * 是否连接
     *
     * @return
     */
    public boolean isConnected() {
        return connectState == ConnectState.CONNECT_SUCCESS;
    }

    /**
     * 获取连接GATT配置
     *
     * @return
     */
    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }

    /**
     * 获取蓝牙设备
     *
     * @return
     */
    public BleDevice getBleDevice() {
        return bleDevice;
    }

    /**
     * 获取服务列表
     *
     * @return
     */
    public List<BluetoothGattService> getGattServiceList() {
        if (bluetoothGatt != null) {
            return bluetoothGatt.getServices();
        }
        return Collections.emptyList();
    }

    /**
     * 根据服务UUID获取指定服务
     *
     * @param serviceUUID
     * @return
     */
    public BluetoothGattService getGattService(UUID serviceUUID) {
        if (bluetoothGatt != null && serviceUUID != null) {
            return bluetoothGatt.getService(serviceUUID);
        }
        return null;
    }

    /**
     * 获取某个服务的特征值列表
     *
     * @param serviceUUID
     * @return
     */
    public List<BluetoothGattCharacteristic> getGattCharacteristicList(UUID serviceUUID) {
        if (getGattService(serviceUUID) != null && serviceUUID != null) {
            return getGattService(serviceUUID).getCharacteristics();
        }
        return Collections.emptyList();
    }

    /**
     * 根据特征值UUID获取某个服务的指定特征值
     *
     * @param serviceUUID
     * @param characteristicUUID
     * @return
     */
    public BluetoothGattCharacteristic getGattCharacteristic(UUID serviceUUID, UUID characteristicUUID) {
        if (getGattService(serviceUUID) != null && serviceUUID != null && characteristicUUID != null) {
            return getGattService(serviceUUID).getCharacteristic(characteristicUUID);
        }
        return null;
    }

    /**
     * 获取某个特征值的描述属性列表
     *
     * @param serviceUUID
     * @param characteristicUUID
     * @return
     */
    public List<BluetoothGattDescriptor> getGattDescriptorList(UUID serviceUUID, UUID characteristicUUID) {
        if (getGattCharacteristic(serviceUUID, characteristicUUID) != null && serviceUUID != null && characteristicUUID != null) {
            return getGattCharacteristic(serviceUUID, characteristicUUID).getDescriptors();
        }
        return Collections.emptyList();
    }

    /**
     * 根据描述属性UUID获取某个特征值的指定属性值
     *
     * @param serviceUUID
     * @param characteristicUUID
     * @param descriptorUUID
     * @return
     */
    public BluetoothGattDescriptor getGattDescriptor(UUID serviceUUID, UUID characteristicUUID, UUID descriptorUUID) {
        if (getGattCharacteristic(serviceUUID, characteristicUUID) != null && serviceUUID != null && characteristicUUID != null && descriptorUUID != null) {
            return getGattCharacteristic(serviceUUID, characteristicUUID).getDescriptor(descriptorUUID);
        }
        return null;
    }

    /**
     * 刷新设备缓存
     *
     * @return
     */
    public synchronized boolean refreshDeviceCache() {
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null && bluetoothGatt != null) {
                final boolean success = (Boolean) refresh.invoke(bluetoothGatt);
                return success;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 断开连接
     */
    public synchronized void disconnect() {
        connectState = ConnectState.CONNECT_INIT;
        if (bluetoothGatt != null) {
            isActiveDisconnect = true;
            bluetoothGatt.disconnect();
            refreshDeviceCache();
        }
    }

    private synchronized void close() {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
        }
    }
}
