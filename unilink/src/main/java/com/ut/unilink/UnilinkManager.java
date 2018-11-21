package com.ut.unilink;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.ut.unilink.cloudLock.CallBack;
import com.ut.unilink.cloudLock.CloudLock;
import com.ut.unilink.cloudLock.ConnectListener;
import com.ut.unilink.cloudLock.LockStateListener;
import com.ut.unilink.cloudLock.ScanDevice;
import com.ut.unilink.cloudLock.ScanListener;
import com.ut.unilink.cloudLock.Unilink;
import com.ut.unilink.util.Log;
import com.zhichu.nativeplugin.ble.Ble;

/**
 * <p>云锁设备控制管理类。
 * <p>调用该类接口可以对云锁设备进行搜索、连接，并发送相应控制命令。
 * <p>云锁设备分为未激活设备和已激活设备，扫描到设备后可以查看激活状态{@link ScanDevice#isActive()}。
 * <p>对设备进行操作前需先激活设备，先要初始化{@link #initLock(ScanDevice, CallBack)}，并在5秒内确认初始化{@link #confirmInit(CloudLock, CallBack)}。
 * <p><pre>
 *获取该类实例：
 *          UnilinkManager unilinkManager = UnilinkManager.getInstance(context);
 * </pre>
 */
public class UnilinkManager {

    /**
     * 蓝牙不支持
     */
    public static final int BLE_NOT_SUPPORT = -1;
    /**
     * 没有定位权限
     */
    public static final int NO_LOCATION_PERMISSION = -2;
    /**
     * 蓝牙未打开
     */
    public static final int BLE_NOT_OPEN = 10;
    /**
     * 搜索执行成功
     */
    public static final int SCAN_SUCCESS = 0;

    private static UnilinkManager INSTANCE;
    private Unilink mUnilink;
    private Context mContext;

    synchronized public static UnilinkManager getInstance(Context context) {
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
     * 请求打开蓝牙。打开蓝牙结果需要实现{@link Activity#onActivityResult(int, int, Intent)}
     * @param activity
     * @param requestCode 请求码
     */
    public void enableBluetooth(Activity activity, int requestCode) {
        Ble.get().enableBluetooth(activity, requestCode);
    }

    /**
     * 请求蓝牙搜索所需的定位权限。请求权限结果需要实现{@link Activity#onRequestPermissionsResult(int, String[], int[])}
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
     * @return {@link #BLE_NOT_SUPPORT 蓝牙不支持}、{@link #NO_LOCATION_PERMISSION 没有获取位权限ACCESS_COARSE_LOCATION 或 ACCESS_FINE_LOCATION}
     *         {@link #BLE_NOT_OPEN 蓝牙没有打开}、{@link #SCAN_SUCCESS 搜索执行成功}
     */
    public int scan(final ScanListener scanListener, int scanTime) {
        return scan(scanListener, scanTime, null, null);
    }

    /**
     * 搜索指定厂商和设备类型的云锁设备
     * @param scanListener 搜索结果监听器
     * @param scanTime 搜索时间,以秒为单位
     * @param vendorId 要搜索的厂商标识，为4个字节数组，可为null
     * @param deviceType 要搜索的设备类型，为2个字节数组，可为null
     * @return {@link #BLE_NOT_SUPPORT 蓝牙不支持}、{@link #NO_LOCATION_PERMISSION 没有获取位权限ACCESS_COARSE_LOCATION 或 ACCESS_FINE_LOCATION}
     *         {@link #BLE_NOT_OPEN 蓝牙没有打开}、{@link #SCAN_SUCCESS 搜索执行成功}
     */
    public int scan(final ScanListener scanListener, int scanTime, byte[] vendorId, byte[] deviceType) {
        if (!checkPermission()) {
            return -2;
        }
        return mUnilink.scan(scanListener, scanTime, vendorId, deviceType);
    }


    /**
     * 连接指定云锁设备
     * @param mac 云锁设备MAC地址，可以从搜索获取的云锁设备{@link ScanDevice#getAddress()}得到
     * @param connectListener 连接结果监听器
     */
    public void connect(String mac, final ConnectListener connectListener) {
        mUnilink.connect(mac, connectListener);
    }

    /**
     *连接指定蓝牙设备
     * @param mac 设备MAC地址
     * @param connectListener 监听连接结果
     * @param lockStateListener 连接成功后，监听云锁的状态信息
     */
    public void connect(String mac, ConnectListener connectListener, LockStateListener lockStateListener) {
        mUnilink.connect(mac, connectListener, lockStateListener);
    }

    /**
     * 连接指定蓝牙设备
     * @param scanDevice 蓝牙低功耗设备 通过扫描得到{@link #scan(ScanListener, int, byte[], byte[])、 {@link #scan(ScanListener, int)}}
     * @param connectListener 监听连接结果
     * @param lockStateListener 连接成功后，监听云锁的状态信息
     */
    public void connect(ScanDevice scanDevice, ConnectListener connectListener, LockStateListener lockStateListener) {
        connect(scanDevice.getAddress(), connectListener, lockStateListener);
    }

    /**
     * 连接指定蓝牙设备
     * @param scanDevice 蓝牙低功耗设备 通过扫描得到{@link #scan(ScanListener, int, byte[], byte[])、 {@link #scan(ScanListener, int)}}
     * @param connectListener 监听连接结果
     */
    public void connect(ScanDevice scanDevice, ConnectListener connectListener) {
        mUnilink.connect(scanDevice.getAddress(), connectListener);
    }


    /**
     * <p>初始化云锁设备,用于激活云锁设备。
     * <p>调用成功后需要在5秒内调用{@link #confirmInit(CloudLock, CallBack)}进行确认初始化，确认初始化成功后，
     * 云锁设备才成功激活
     * @param scanDevice 蓝牙低功耗设备, 通过扫描得到{@link #scan(ScanListener, int, byte[], byte[])、 {@link #scan(ScanListener, int)}}
     * @param callBack 操作回调接口，初始化成功会返回CloudLock对象
     */
    public void initLock(ScanDevice scanDevice, CallBack callBack) {
        mUnilink.initLock(scanDevice, callBack);
    }

    /**
     * <p>确认初始化云锁设备,用于激活云锁设备。
     * <p>在激活云锁设备时，需先初始化云锁设备{@link #initLock(ScanDevice, CallBack)}，并在5秒内调用该命令进行确认初始化。
     * <p><pre>参数lock对象有两种获取方式
     * 1.通过调用初始化命令成功后获取{@link #initLock(ScanDevice, CallBack)}
     * 2.手动创建,需要设置相应参数
     *
     *     CloudLock cloudLock = new CloudLock(mac);
     *     cloudLock.setAdminPassword(adminPW);
     * </pre>
     * @param lock 表示某个云锁设备
     * @param callBack 操作回调接口
     */
    public void confirmInit(CloudLock lock, CallBack callBack) {
        mUnilink.confirmInit(lock, callBack);
    }

    /**
     * <p>对指定云锁设备进行开锁操作。
     * <p>调用命令前需要先激活云锁设备{@link #initLock(ScanDevice, CallBack)、 {@link #confirmInit(CloudLock, CallBack)}}。
     * <p><pre>参数lock对象有两种获取方式
     * 1.通过调用初始化命令成功后获取{@link #initLock(ScanDevice, CallBack)}
     * 2.手动创建,需要设置相应参数
     *
     *     CloudLock cloudLock = new CloudLock(mac);
     *     cloudLock.setOpenLockPassword(adminPW);
     *     cloudLock.setEncryptType(encryptType);
     *     cloudLock.setEntryptKey(key);
     * </pre>
     * @param lock 表示某个云锁设备
     * @param callBack 操作回调接口
     */
    public void openLock(CloudLock lock, CallBack callBack) {
        mUnilink.openLock(lock, callBack);
    }

    /**
     * <p>重置指定云锁设备。
     * <p>调用命令前需要先激活云锁设备{@link #initLock(ScanDevice, CallBack)、 {@link #confirmInit(CloudLock, CallBack)}}。
     * <p><pre>参数lock对象有两种获取方式
     * 1.通过调用初始化命令成功后获取{@link #initLock(ScanDevice, CallBack)}
     * 2.手动创建,需要设置相应参数
     *
     *     CloudLock cloudLock = new CloudLock(mac);
     *     cloudLock.setAdminPassword(adminPW);
     *     cloudLock.setEncryptType(encryptType);
     *     cloudLock.setEntryptKey(key);
     * </pre>
     * @param lock 表示某个云锁设备， 初始化云锁成功后{@link #initLock(ScanDevice, CallBack)}会返回相应CloudLock对象
     * @param callBack 操作回调接口
     */
    public void resetLock(CloudLock lock, CallBack callBack) {
        mUnilink.resetLock(lock, callBack);
    }

    /**
     * <p>读取云锁设备自增变量。
     * <p>成功读取的自增变量保存到回调的lock对象中，使用{@link CloudLock#getAutuIncreaseNum()}获取。
     * <p>调用命令前需要先激活云锁设备{@link #initLock(ScanDevice, CallBack)、 {@link #confirmInit(CloudLock, CallBack)}}。
     * <p><pre>参数lock对象有两种获取方式
     * 1.通过调用初始化命令成功后获取{@link #initLock(ScanDevice, CallBack)}
     * 2.手动创建,需要设置相应参数
     *
     *     CloudLock cloudLock = new CloudLock(mac);
     * </pre>
     * @param lock 表示某个云锁设备
     * @param callBack 操作回调接口
     */
    public void getAutoIncreaseNum(CloudLock lock, CallBack callBack) {
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
     * <p>读取云锁设备指定节点信息。
     * <p>参数lock对象需要先设置要读取的设备节点编号{@link CloudLock#setDeviceNum(byte)}
     * 成功读取后的设备节点信息保存到回调的lock对象中，使用{@link CloudLock#getDeviceInfo(byte)}读取。
     * <p>调用命令前需要先激活云锁设备{@link #initLock(ScanDevice, CallBack)、 {@link #confirmInit(CloudLock, CallBack)}}。
     * <p><pre>参数lock对象有两种获取方式
     * 1.通过调用初始化命令成功后获取{@link #initLock(ScanDevice, CallBack)}
     * 2.手动创建,需要设置相应参数
     *
     *     CloudLock cloudLock = new CloudLock(mac);
     *     cloudLock.setEncryptType(encryptType);
     *     cloudLock.setEntryptKey(key);
     *     cloudLock.setDeviceNum(deviceNum);
     * </pre>
     * @param lock 表示某个云锁设备
     * @param callBack 操作回调接口
     */
    public void getDeviceInfo(final CloudLock lock, final CallBack callBack) {
        mUnilink.readDeviceInfo(lock, callBack);
    }

    /**
     * <p>读取云锁设备多个节点信息。
     * <p>成功读取后的设备节点信息保存到回调的lock对象中，使用{@link CloudLock#getDeviceInfoMap()}读取。
     * <p>调用命令前需要先激活云锁设备{@link #initLock(ScanDevice, CallBack)、 {@link #confirmInit(CloudLock, CallBack)}}。
     * <p><pre>参数lock对象有两种获取方式
     * 1.通过调用初始化命令成功后获取{@link #initLock(ScanDevice, CallBack)}
     * 2.手动创建,需要设置相应参数
     *
     *     CloudLock cloudLock = new CloudLock(mac);
     *     cloudLock.setEncryptType(encryptType);
     *     cloudLock.setEntryptKey(key);
     * </pre>
     * @param lock 表示某个云锁设备
     * @param callBack 操作回调接口
     */
    public void getDeviceInfos(final CloudLock lock, final CallBack callBack) {
        mUnilink.readMutilDeviceInfo(lock, callBack);
    }

    /**
     * <p>设置云锁设备的生产序列号。
     * <p>参数lock对象需要先设置要写入的生产序列号{@link CloudLock#setSerialNum(byte[])}。
     * <p>调用命令前需要先激活云锁设备{@link #initLock(ScanDevice, CallBack)、 {@link #confirmInit(CloudLock, CallBack)}}。
     * <p><pre>参数lock对象有两种获取方式
     * 1.通过调用初始化命令成功后获取{@link #initLock(ScanDevice, CallBack)}
     * 2.手动创建,需要设置相应参数
     *
     *     CloudLock cloudLock = new CloudLock(mac);
     *     cloudLock.setSerialNum(serialNum);
     * </pre>
     * @param lock 表示某个云锁设备
     * @param callBack 操作回调接口
     */
    public void setSerialNum(final CloudLock lock, final CallBack callBack) {
        mUnilink.writeSerialNum(lock, callBack);
    }

    /**
     * <p>读取生产序列号。
     * <p>读取到的数据存放到参数lock里，使用{@link CloudLock#getSerialNum()}获取。
     * <p>调用命令前需要先激活云锁设备{@link #initLock(ScanDevice, CallBack)、 {@link #confirmInit(CloudLock, CallBack)}}。
     * <p><pre>参数lock对象有两种获取方式
     * 1.通过调用初始化命令成功后获取{@link #initLock(ScanDevice, CallBack)}
     * 2.手动创建,需要设置相应参数
     *
     *     CloudLock cloudLock = new CloudLock(mac);
     * </pre>
     * @param lock 表示某个云锁设备
     * @param callBack 操作回调接口
     */
    public void getSerialNum(final CloudLock lock, final CallBack callBack) {
        mUnilink.readSerialNum(lock, callBack);
    }

    /**
     * <p>设置云锁设备的产商标识和设备类型。
     * <p>传入参数lock需先设置厂商标识和设备类型{@link CloudLock#setVendorId(byte[])}、{@link CloudLock#setDeviceType(byte[])}。
     * <p>调用命令前需要先激活云锁设备{@link #initLock(ScanDevice, CallBack)、 {@link #confirmInit(CloudLock, CallBack)}}。
     * <p><pre>参数lock对象有两种获取方式
     * 1.通过调用初始化命令成功后获取{@link #initLock(ScanDevice, CallBack)}
     * 2.手动创建,需要设置相应参数
     *
     *     CloudLock cloudLock = new CloudLock(mac);
     *     cloudLock.setVendorId(vendorId);
     *     cloudLock.setDeviceType(deviceType);
     * </pre>
     * @param lock 表示某个云锁设备
     * @param callBack 操作回调接口
     */
    public void setVendorId(final CloudLock lock, final CallBack callBack) {
        mUnilink.writeVendorId(lock, callBack);
    }

    /**
     * <p>读取云锁设备厂商标识和设备类型。
     * <p>读取到的数据存放到参数lock里，使用{@link CloudLock#getVendorId()}、{@link CloudLock#getDeviceType()}获取。
     * <p>调用命令前需要先激活云锁设备{@link #initLock(ScanDevice, CallBack)、 {@link #confirmInit(CloudLock, CallBack)}}。
     * <p><pre>参数lock对象有两种获取方式
     * 1.通过调用初始化命令成功后获取{@link #initLock(ScanDevice, CallBack)}
     * 2.手动创建,需要设置相应参数
     *
     *     CloudLock cloudLock = new CloudLock(mac);
     * </pre>
     * @param lock 表示某个云锁设备
     * @param callBack 操作回调接口
     */
    public void getVendorId(final CloudLock lock, final CallBack callBack) {
        mUnilink.readVendorId(lock, callBack);
    }

    /**
     * <p>获取云锁设备产品信息。
     * <p>调用命令前需要先激活云锁设备{@link #initLock(ScanDevice, CallBack)、 {@link #confirmInit(CloudLock, CallBack)}}。
     * <p><pre>参数lock对象有两种获取方式
     * 1.通过调用初始化命令成功后获取{@link #initLock(ScanDevice, CallBack)}
     * 2.手动创建,需要设置相应参数
     *
     *     CloudLock cloudLock = new CloudLock(mac);
     * </pre>
     * @param lock 表示某个云锁设备
     * @param callBack 操作回调接口
     */
    public void getProductInfo(final CloudLock lock, final CallBack callBack) {
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
