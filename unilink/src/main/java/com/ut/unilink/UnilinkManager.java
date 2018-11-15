package com.ut.unilink;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.ut.unilink.cloudLock.CloudLock;
import com.ut.unilink.cloudLock.ConnectListener;
import com.ut.unilink.cloudLock.LockStateListener;
import com.ut.unilink.cloudLock.ScanListener;
import com.ut.unilink.cloudLock.UTBleDevice;
import com.ut.unilink.cloudLock.Unilink;
import com.ut.unilink.util.Log;
import com.zhichu.nativeplugin.ble.Ble;

public class UnilinkManager {

    private static UnilinkManager INSTANCE;
    private Unilink mUnilink;
    private Context mContext;

    synchronized public static UnilinkManager getINSTANCE(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new UnilinkManager(context);
        }
        return INSTANCE;
    }

    private UnilinkManager(Context context) {
        mContext = context.getApplicationContext();
        mUnilink = new Unilink(mContext);
    }

    /**
     * 打开蓝牙
     *
     * @param activity    上下文
     * @param requestCode
     * @return -1 蓝牙不支持  10 蓝牙没有打开 12 蓝牙已经打开 0 正在打开
     */
    public void enableBluetooth(Activity activity, int requestCode) {
        Ble.get().enableBluetooth(activity, requestCode);
    }

    /**
     * 请求蓝牙搜索所需的定位权限
     * @param activity
     * @param requestCode
     */
    public void requestPermission(Activity activity, int requestCode) {
        if (!checkPermission()) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    requestCode);
        }
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return false;
        }
        return true;
    }

    /**
     * 查询指定设备是否已连接
     * @param address 设备Mac地址
     * @return true:已连接 false:未连接
     */
    public boolean isConnect(String address) {
        return mUnilink.isConnect(address);
    }

    /**
     * 搜索云锁设备
     * @param scanListener 搜索结果监听器
     * @param scanTime 搜索时间,以秒为单位
     * @return -1：蓝牙不支持  -2：没有获取位权限{ACCESS_COARSE_LOCATION 或 ACCESS_FINE_LOCATION} 10：蓝牙没有打开  0：搜索执行成功
     */
    public int scan(final ScanListener scanListener, int scanTime) {
        return scan(scanListener, scanTime, null, null);
    }

    /**
     * 搜索指定厂商和设备类型的云锁设备
     * @param scanListener 搜索结果监听器
     * @param scanTime 搜索时间,以秒为单位
     * @param vendorId 要搜索的厂商标识， 可为null
     * @param deviceType 要搜索的设备类型， 可为null
     * @return -1：蓝牙不支持  -2：没有获取位权限{ACCESS_COARSE_LOCATION 或 ACCESS_FINE_LOCATION} 10：蓝牙没有打开  0：搜索执行成功
     */
    public int scan(final ScanListener scanListener, int scanTime, byte[] vendorId, byte[] deviceType) {
        if (!checkPermission()) {
            return -2;
        }
        return mUnilink.scan(scanListener, scanTime, vendorId, deviceType);
    }


    /**
     * 连接指定云锁设备
     * @param address 云锁设备MAC地址，可以从搜索获取的云锁设备{@link UTBleDevice#getAddress()}得到
     * @param connectListener 连接结果监听器
     */
    public void connect(String address, final ConnectListener connectListener) {
        mUnilink.connect(address, connectListener);
    }

    /**
     *连接指定蓝牙设备
     * @param address 设备MAC地址
     * @param connectListener 监听连接结果
     * @param lockStateListener 连接成功后，监听云锁的状态信息
     */
    public void connect(String address, ConnectListener connectListener, LockStateListener lockStateListener) {
        mUnilink.connect(address, connectListener, lockStateListener);
    }

    /**
     * 连接指定蓝牙设备
     * @param bleDevice 蓝牙低功耗设备
     * @param connectListener 监听连接结果
     * @param lockStateListener 连接成功后，监听云锁的状态信息
     */
    public void connect(UTBleDevice bleDevice, ConnectListener connectListener, LockStateListener lockStateListener) {
        connect(bleDevice.getAddress(), connectListener, lockStateListener);
    }

    /**
     * 连接指定蓝牙设备
     * @param bleDevice 蓝牙低功耗设备
     * @param connectListener 监听连接结果
     */
    public void connect(UTBleDevice bleDevice, ConnectListener connectListener) {
        mUnilink.connect(bleDevice.getAddress(), connectListener);
    }


    /**
     * 初始化云锁设备
     * @param bleDevice 蓝牙低功耗设备
     * @param callBack 操作回调接口，初始化成功会返回CloudLock对象
     */
    public void initLock(UTBleDevice bleDevice, Unilink.CallBack callBack) {
        mUnilink.initLock(bleDevice, callBack);
    }

    /**
     * 对指定云锁设备进行开锁操作
     * @param lock 表示某个云锁设备， 初始化云锁成功后{@link #initLock(UTBleDevice, Unilink.CallBack)}会返回相应CloudLock对象
     * @param callBack 操作回调接口
     */
    public void openLock(CloudLock lock, Unilink.CallBack callBack) {
        mUnilink.openLock(lock, callBack);
    }

    /**
     * 重置指定云锁设备
     * @param lock 表示某个云锁设备， 初始化云锁成功后{@link #initLock(UTBleDevice, Unilink.CallBack)}会返回相应CloudLock对象
     * @param callBack 操作回调接口
     */
    public void resetLock(CloudLock lock, Unilink.CallBack callBack) {
        mUnilink.resetLock(lock, callBack);
    }

    /**
     * 读取从设备自增变量，成功读取的自增变量保存到回调的lock对象中，使用{@link CloudLock#getAutuIncreaseNum()}获取
     * @param lock 表示某个云锁设备， 初始化云锁成功后{@link #initLock(UTBleDevice, Unilink.CallBack)}会返回相应CloudLock对象
     * @param callBack 操作回调接口
     */
    public void readAutoIncreaseNum(CloudLock lock, Unilink.CallBack callBack) {
        mUnilink.readAutoIncreaseNum(lock, callBack);
    }

    /**
     * 发送超级密码重置设备， 测试用
     * @param address 设备Mac地址
     */
    public void sendSuperPassword(String address) {
        mUnilink.send(address, "!@#QWE123".getBytes());
    }

    /**
     * 主设备读从设备指定节点信息, 参数lock对象需要先设置要读取的设备节点编号{@link CloudLock#setDeviceNum(byte)}
     * 成功读取后的设备节点信息保存到回调的lock对象中，使用{@link CloudLock#getDeviceInfo(byte)}读取
     * @param lock 表示某个云锁设备， 初始化云锁成功后{@link #initLock(UTBleDevice, Unilink.CallBack)}会返回相应CloudLock对象
     * @param callBack 操作回调接口
     */
    public void readDeviceInfo(final CloudLock lock, final Unilink.CallBack callBack) {
        mUnilink.readDeviceInfo(lock, callBack);
    }

    /**
     * 主设备读从设备多点节点信息，成功读取后的设备节点信息保存到回调的lock对象中，使用{@link CloudLock#getDeviceInfoMap()}读取
     * @param lock 表示某个云锁设备， 初始化云锁成功后{@link #initLock(UTBleDevice, Unilink.CallBack)}会返回相应CloudLock对象
     * @param callBack 操作回调接口
     */
    public void readMutilDeviceInfo(final CloudLock lock, final Unilink.CallBack callBack) {
        mUnilink.readMutilDeviceInfo(lock, callBack);
    }

    /**
     * 写入从设备生产序列号
     * @param lock 表示某个云锁设备， 初始化云锁成功后{@link #initLock(UTBleDevice, Unilink.CallBack)}会返回相应CloudLock对象
     * @param callBack 操作回调接口
     */
    public void writeSerialNum(final CloudLock lock, final Unilink.CallBack callBack) {
        mUnilink.writeSerialNum(lock, callBack);
    }

    /**
     * 读取生产序列号， 读取到的数据存放到参数lock里
     * @param lock 表示某个云锁设备， 初始化云锁成功后{@link #initLock(UTBleDevice, Unilink.CallBack)}会返回相应CloudLock对象
     * @param callBack 操作回调接口
     */
    public void readSerialNum(final CloudLock lock, final Unilink.CallBack callBack) {
        mUnilink.readSerialNum(lock, callBack);
    }

    /**
     * 写入从设备厂商标识, 传入参数lock需先设置厂商标识和设备类型{@link CloudLock#setVendorId(byte[])}、{@link CloudLock#setDeviceType(byte[])}
     * @param lock 表示某个云锁设备， 初始化云锁成功后{@link #initLock(UTBleDevice, Unilink.CallBack)}会返回相应CloudLock对象
     * @param callBack 操作回调接口
     */
    public void writeVendorId(final CloudLock lock, final Unilink.CallBack callBack) {
        mUnilink.writeVendorId(lock, callBack);
    }

    /**
     * 读取从设备厂商标识和设备类型， 读取到的数据存放到参数lock里
     * @param lock 表示某个云锁设备， 初始化云锁成功后{@link #initLock(UTBleDevice, Unilink.CallBack)}会返回相应CloudLock对象
     * @param callBack 操作回调接口
     */
    public void readVendorId(final CloudLock lock, final Unilink.CallBack callBack) {
        mUnilink.readVendorId(lock, callBack);
    }

    /**
     * 获取设备产品信息
     * @param lock 表示某个云锁设备， 初始化云锁成功后{@link #initLock(UTBleDevice, Unilink.CallBack)}会返回相应CloudLock对象
     * @param callBack 操作回调接口
     */
    public void getProductInfo(final CloudLock lock, final Unilink.CallBack callBack) {
        mUnilink.getProductInfo(lock, callBack);
    }

    /**
     * 开启或关闭log
     * @param isEnable
     */
    public void enableLog(boolean isEnable) {
        Log.enableLog(isEnable);
    }
}
