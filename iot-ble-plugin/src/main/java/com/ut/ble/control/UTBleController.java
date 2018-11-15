package com.ut.ble.control;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;

import com.ut.ble.control.i.IEnableResult;
import com.zhichu.nativeplugin.ble.Ble;
import com.zhichu.nativeplugin.ble.BleDevice;
import com.zhichu.nativeplugin.ble.IBleNotifyDataCallback;
import com.zhichu.nativeplugin.ble.IBleStateChangedCallback;
import com.zhichu.nativeplugin.ble.IConnectCallback;
import com.zhichu.nativeplugin.ble.INotifyCallback;
import com.zhichu.nativeplugin.ble.IReadCallback;
import com.zhichu.nativeplugin.ble.IRssiCallback;
import com.zhichu.nativeplugin.ble.IWriteCallback;
import com.zhichu.nativeplugin.ble.R;
import com.zhichu.nativeplugin.ble.scan.IScanCallback;
import com.zhichu.nativeplugin.ble.scan.UTFilterScanCallback;

import java.util.List;
import java.util.UUID;

public class UTBleController {
    private static final int ACTIVITY_REQUESTCODE_ENABLEBLUETOOTH = 539;//

    private static final String IOT_EVENT_BLESTATECHANGED = "iot_event_blestatechanged"; //蓝牙状态事件监听
    private static final String IOT_EVENT_SCANDEVICEFOUND = "iot_event_scandevicefound"; //搜索事件监听
    private static final String IOT_EVENT_SCANDEVICEFOUNDFINISHED = "iot_event_scandevicefoundfinished"; //搜索完成事件监听
    private static final String IOT_EVENT_BLENOTIFYDATA = "iot_event_blenotifydata"; //数据事件监听

    private IEnableResult mEnableResult;

    private Activity mActivity = null;

    public static final UTBleController get() {
        return Holder.bleController;
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_REQUESTCODE_ENABLEBLUETOOTH && mEnableResult != null) {
            if (resultCode == Activity.RESULT_OK) {
                mEnableResult.onSuccess();
            } else {
                mEnableResult.onFailure(R.string.ble_not_open);
            }
            mEnableResult = null;
        }
    }

    /**
     * 蓝牙状态改变回调
     */
    private IBleStateChangedCallback bleStateChangedCallback = new IBleStateChangedCallback() {
        @Override
        public void onState(int state) {

        }
    };

    private IBleNotifyDataCallback bleNotifyDataCallback = new IBleNotifyDataCallback() {
        @Override
        public void onNotify(BleDevice bleDevice, byte[] data, UUID serviceUUID, UUID characteristicUUID) {
        }
    };

    /**
     * 设置蓝牙状态监听器
     */
    public void setBleStateChangedCallbackEvent(boolean on) {
        Ble.get().setBleStateChangedCallback(on ? bleStateChangedCallback : null);
    }

    /**
     * 初始化蓝牙
     */
    public void init(Context context) {
        Ble.get().init(context);
    }

    /**
     * 销毁
     */
    public void uninit() {
        Ble.get().uninit();
    }

    /**
     * 是否支持BLE
     */
    public boolean isSupportBle() {
        boolean result = Ble.get().isSupportBle();
        return result;
    }

    /**
     * 是否支持蓝牙
     */
    public boolean isSupportBluetooth() {
        boolean result = Ble.get().isSupportBluetooth();
        return result;
    }

    /**
     * 蓝牙是否打开
     */
    public boolean isEnabled() {
        boolean result = Ble.get().isEnabled();
        return result;
    }

    public void enableBluetooth(IEnableResult callback) {
        if (getCurrentActivity() != null) {

            int result = Ble.get().enableBluetooth(getCurrentActivity(),
                    ACTIVITY_REQUESTCODE_ENABLEBLUETOOTH);
            if (result == 12) {
                callback.onSuccess();
            } else if (result == -1) {
                callback.onFailure(R.string.ble_not_support);
            }
        } else {
            callback.onFailure("has no activity");
        }
    }

    /**
     * 搜索
     *
     * @return -1 蓝牙不支持  10 蓝牙没有打开  0 搜索执行成功
     */
    public int scanUTBle(final int scanSeconds, IScanCallback scanCallback) {
        return Ble.get().scan(new UTFilterScanCallback(scanCallback).scanSecond(scanSeconds));
    }

    /**
     * 停止搜索
     *
     * @return -1 蓝牙不支持  10 蓝牙没有打开  0 停止搜索执行成功
     */
    public int stopScan() {
        int result = Ble.get().stopScan();
        return result;
    }

    /**
     * 通过蓝牙UUID或者MAC地址直接连接
     *
     * @param deviceUUID 蓝牙UUID或者MAC地址
     */
    public void connect(final String deviceUUID, final IConnectCallback iConnectCallback) {
        Ble.get().connect(deviceUUID, iConnectCallback);
    }

    /**
     * 通过蓝牙UUID或者MAC地址连接，先搜索后连接
     *
     * @param deviceUUID 蓝牙设备UUID或者MAC地址
     * @return -1 蓝牙不支持  10 蓝牙没有打开  12 搜索执行成功
     */
    public int scanConnect(final String deviceUUID, final IConnectCallback iConnectCallback) {
        int result = Ble.get().scanConnect(deviceUUID, iConnectCallback);
        return result;
    }

    /**
     * 根据蓝牙UUID或者MAC地址断开当前连接
     *
     * @param deviceUUID 蓝牙设备UUID或者MAC地址
     */
    public boolean disconnect(String deviceUUID) {
        boolean result = Ble.get().disconnect(deviceUUID);
        return result;
    }

    /**
     * 断开所有连接
     */
    public void disconnect() {
        Ble.get().disconnect();
    }

    /**
     * 读取数据
     *
     * @param deviceUUID         蓝牙设备UUID或者MAC地址
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 读取特征UUID
     */
    public void read(String deviceUUID, String serviceUUID, String characteristicUUID,
                     final IReadCallback iReadCallback) {
        Ble.get().read(deviceUUID, serviceUUID, characteristicUUID, iReadCallback);
    }

    /**
     * 写数据 ，需要回复
     *
     * @param deviceUUID         蓝牙设备UUID或者MAC地址
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 写特征UUID
     * @param data               数据
     * @param iWriteCallback     写数据结果回调
     */
    public void write(String deviceUUID, String serviceUUID, String characteristicUUID,
                      byte[] data, final IWriteCallback iWriteCallback) {
        Ble.get().write(deviceUUID, serviceUUID, characteristicUUID, data, iWriteCallback);
    }

    /**
     * 写数据 ，不需要回复
     *
     * @param deviceUUID         蓝牙设备UUID或者MAC地址
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 写特征UUID
     * @param data               数据
     * @param iWriteCallback
     */
    public void writeNoResp(String deviceUUID, String serviceUUID, String characteristicUUID,
                            String data, final IWriteCallback iWriteCallback) {
        Ble.get().writeNoResp(deviceUUID, serviceUUID, characteristicUUID, data.getBytes(), iWriteCallback);
    }

    /**
     * 注册通知
     *
     * @param deviceUUID         蓝牙设备UUID或者MAC地址
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 通知特征UUID
     * @param iNotifyCallback
     */
    public boolean registerNotify(String deviceUUID, String serviceUUID, String characteristicUUID,
                                  final INotifyCallback iNotifyCallback) {
        boolean result = addBleNotifyDataCallback(deviceUUID);
        if (!result) {
            return false;
        }
        Ble.get().registerNotify(deviceUUID, serviceUUID, characteristicUUID, iNotifyCallback);
        return true;
    }

    /**
     * 取消通知注册
     *
     * @param deviceUUID         蓝牙设备UUID或者MAC地址
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 通知特征UUID
     * @param iNotifyCallback
     */
    public boolean unregisterNotify(String deviceUUID, String serviceUUID, String characteristicUUID,
                                    final INotifyCallback iNotifyCallback) {
        boolean result = removeBleNotifyDataCallback(deviceUUID);
        if (!result) {
            return false;
        }
        Ble.get().unregisterNotify(deviceUUID, serviceUUID, characteristicUUID, iNotifyCallback);
        return true;
    }

    /**
     * 读取RSSI
     *
     * @param deviceUUID 蓝牙设备UUID或者MAC地址
     */
    public void readRssi(String deviceUUID, final IRssiCallback iRssiCallback) {
        Ble.get().readRssi(deviceUUID, iRssiCallback);
    }

    /**
     * 获取最后一次扫描的列表
     */
    public List<BleDevice> getLastScanFinishedDeviceList() {
        List<BleDevice> result = Ble.get().getLastScanFinishedDeviceList();
        return result;
    }


    /***
     * 获取已连接的设备列表
     *
     */
    public List<BleDevice> getConnectedPeripheralDeviceList() {
        List<BleDevice> result = Ble.get().getConnectedPeripheralDeviceList();
        return result;
    }

    /**
     * 添加被动通知回调
     *
     * @param deviceUUID 蓝牙设备UUID或者MAC地址
     */
    private boolean addBleNotifyDataCallback(String deviceUUID) {
        return Ble.get().addBleNotifyDataCallback(deviceUUID, bleNotifyDataCallback);
    }

    /**
     * 移除被动通知回调
     *
     * @param deviceUUID 蓝牙设备UUID或者MAC地址
     */
    private boolean removeBleNotifyDataCallback(String deviceUUID) {
        return Ble.get().removeBleNotifyDataCallback(deviceUUID, bleNotifyDataCallback);
    }

    /**
     * 是否已经连接
     *
     * @param deviceUUID 蓝牙设备UUID或者MAC地址
     */
    public boolean isConnected(String deviceUUID) {
        return Ble.get().isConnected(deviceUUID);
    }

//    /**
//     * 根据蓝牙设备UUID或者MAC地址获取已连接的蓝牙设备
//     *
//     * @param deviceUUID
//     * @param callback
//     */
//    @ReactMethod
//    public void getConnectedBleDevice(String deviceUUID, Callback callback) {
//        BleDevice bleDevice = Ble.get().getConnectedBleDevice(deviceUUID);
//        callback.invoke(bleDevice);
//    }

    /**
     * 检查当前蓝牙状态 ,检查结果在监听器中返回
     */
    public void checkState() {
        Ble.get().checkState();
    }

    /**
     * 获取服务列表
     *
     * @param deviceUUID 蓝牙设备UUID或者MAC地址
     */
    public List<BluetoothGattService> getGattServiceList(String deviceUUID) {
        List<BluetoothGattService> result = Ble.get().getGattServiceList(deviceUUID);
        return result;
    }

    /**
     * 获取某个服务的特征值列表
     *
     * @param deviceUUID  蓝牙设备UUID或者MAC地址
     * @param serviceUUID 服务UUID
     */
    public List<BluetoothGattCharacteristic> getGattCharacteristicList(String deviceUUID, String serviceUUID) {
        List<BluetoothGattCharacteristic> result = Ble.get().getGattCharacteristicList(deviceUUID,
                serviceUUID);
        return result;
    }

    /**
     * 获取某个特征值的描述属性列表
     *
     * @param deviceUUID         蓝牙设备UUID或者MAC地址
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     */
    public List<BluetoothGattDescriptor> getGattDescriptorList(String deviceUUID, String serviceUUID,
                                                               String characteristicUUID) {
        List<BluetoothGattDescriptor> result = Ble.get().getGattDescriptorList(deviceUUID,
                serviceUUID, characteristicUUID);
        return result;
    }


    private Activity getCurrentActivity() {
        return mActivity;
    }

    public void changeCurrentActivity(Activity activity) {
        this.mActivity = activity;
    }

    private String getString(int id) {
        if (getCurrentActivity() != null) {
            return getCurrentActivity().getString(id);
        } else {
            return "";
        }
    }

    private static final class Holder {
        public static final UTBleController bleController = new UTBleController();
    }
}
