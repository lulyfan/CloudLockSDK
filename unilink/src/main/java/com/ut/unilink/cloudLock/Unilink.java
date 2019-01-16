package com.ut.unilink.cloudLock;

import android.content.Context;

import com.ut.unilink.cloudLock.protocol.AesEncrypt;
import com.ut.unilink.cloudLock.protocol.ClientHelper;
import com.ut.unilink.cloudLock.protocol.GateLockCmd.BatchUpdateAuthInfos;
import com.ut.unilink.cloudLock.protocol.GateLockCmd.DeleteKey;
import com.ut.unilink.cloudLock.protocol.GateLockCmd.OperateAuthTable;
import com.ut.unilink.cloudLock.protocol.GateLockCmd.ReadAuthCountInfo;
import com.ut.unilink.cloudLock.protocol.GateLockCmd.ReadGateLockOpenLockRecord;
import com.ut.unilink.cloudLock.protocol.GateLockCmd.ReadKeyInfos;
import com.ut.unilink.cloudLock.protocol.GateLockCmd.ReadTime;
import com.ut.unilink.cloudLock.protocol.GateLockCmd.WriteKeyInfos;
import com.ut.unilink.cloudLock.protocol.GateLockCmd.WriteTime;
import com.ut.unilink.cloudLock.protocol.TeaEncrypt;
import com.ut.unilink.cloudLock.protocol.cmd.BleCallBack;
import com.ut.unilink.cloudLock.protocol.cmd.BleCmdBase;
import com.ut.unilink.cloudLock.protocol.cmd.ConfirmInitLock;
import com.ut.unilink.cloudLock.protocol.cmd.ErrCode;
import com.ut.unilink.cloudLock.protocol.cmd.GetProductInfo;
import com.ut.unilink.cloudLock.protocol.cmd.InitLock;
import com.ut.unilink.cloudLock.protocol.cmd.OperateLock;
import com.ut.unilink.cloudLock.protocol.cmd.ReadAutoIncreaseNum;
import com.ut.unilink.cloudLock.protocol.cmd.ReadDeviceInfo;
import com.ut.unilink.cloudLock.protocol.cmd.ReadDeviceMutilInfo;
import com.ut.unilink.cloudLock.protocol.cmd.ReadOpenLockRecord;
import com.ut.unilink.cloudLock.protocol.cmd.ReadProductionSerialNum;
import com.ut.unilink.cloudLock.protocol.cmd.ReadVendorId;
import com.ut.unilink.cloudLock.protocol.cmd.WriteDeviceInfo;
import com.ut.unilink.cloudLock.protocol.cmd.ResetLock;
import com.ut.unilink.cloudLock.protocol.cmd.WriteProductionSerialNum;
import com.ut.unilink.cloudLock.protocol.cmd.WriteVendorId;
import com.ut.unilink.cloudLock.protocol.data.AuthCountInfo;
import com.ut.unilink.cloudLock.protocol.data.AuthInfo;
import com.ut.unilink.cloudLock.protocol.data.CloudLockNodeInfo;
import com.ut.unilink.cloudLock.protocol.data.CloudLockOperateRecord;
import com.ut.unilink.cloudLock.protocol.data.DeviceNodeInfo;
import com.ut.unilink.cloudLock.protocol.data.GateLockKey;
import com.ut.unilink.cloudLock.protocol.data.GateLockNodeInfo;
import com.ut.unilink.cloudLock.protocol.data.GateLockOperateRecord;
import com.ut.unilink.cloudLock.protocol.data.GateLockState;
import com.ut.unilink.cloudLock.protocol.data.ProductInfo;
import com.ut.unilink.util.Base64;
import com.ut.unilink.util.Log;
import com.zhichu.nativeplugin.ble.Ble;
import com.zhichu.nativeplugin.ble.BleDevice;
import com.zhichu.nativeplugin.ble.scan.CloudLockFilter;
import com.zhichu.nativeplugin.ble.scan.DeviceId;
import com.zhichu.nativeplugin.ble.scan.GateLockFilter;
import com.zhichu.nativeplugin.ble.scan.IScanCallback;
import com.zhichu.nativeplugin.ble.scan.ScanCallback;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ut.unilink.cloudLock.protocol.cmd.ErrCode.ERR_READ_SERIAL_NUM;
import static com.ut.unilink.cloudLock.protocol.cmd.ErrCode.ERR_TIMEOUT;

public class Unilink {

    private LockConnectionManager mConnectionManager;
    private UTBleLink utBleLink;
    private JinouxBleLink jinouxBleLink;
    private Map<Integer, BaseBleLink> bleLinkMap = new HashMap<>();

    private static final int ENCRYPT_TEA = 0;
    private static final int ENCRYPT_AES = 1;
    private static final int NO_ENCRYPT = -1;
    private BaseBleLink bleLink;

    public Unilink(Context context) {
        mConnectionManager = new LockConnectionManager();
        initBleLink(context);
    }

    private void initBleLink(Context context) {
        utBleLink = new UTBleLink(context);
        utBleLink.setConnectionManager(mConnectionManager);

        jinouxBleLink = new JinouxBleLink(context);
        jinouxBleLink.setConnectionManager(mConnectionManager);

        bleLinkMap.put(DeviceId.CLOUD_LOCK, utBleLink);
        bleLinkMap.put(DeviceId.GATE_LOCK, jinouxBleLink);
    }

    public boolean isConnect(String address) {
        return mConnectionManager.isConnect(address);
    }

    /**
     * 连接指定蓝牙设备
     *
     * @param scanDevice      蓝牙低功耗设备
     * @param connectListener 监听连接结果
     */
    public void connect(ScanDevice scanDevice, ConnectListener connectListener) {
        bleLink = getBleLink(scanDevice.getDeviceId());
        bleLink.connect(scanDevice.getAddress(), connectListener);

        mConnectionManager.setDeviceId(scanDevice.getAddress(), scanDevice.getDeviceId());
        mConnectionManager.setBleLink(bleLink);
    }

    public void connect(ScanDevice scanDevice, final int encryptType, final String encryptKey, ConnectListener connectListener,
                        LockStateListener lockStateListener) {
        addLockStateListener(scanDevice.getAddress(), lockStateListener);
        mConnectionManager.setConnectListener(new IConnectionManager.ConnectListener() {
            @Override
            public void onConnect(String address) {
                setEncryptType(address, encryptType, encryptKey);
            }
        });
        connect(scanDevice, connectListener);
    }

    private BaseBleLink getBleLink(int deviceId) {
        BaseBleLink bleLink = bleLinkMap.get(deviceId);

        if (bleLink == null) {
            return utBleLink;
        }
        return bleLink;
    }

    public void send(String address, byte[] data) {
        if (bleLink != null) {
            bleLink.send(address, data);
        }
    }

    public void close(String address) {
        if (bleLink != null) {
            bleLink.close(address);
        }
    }

    /**
     * 搜索云锁设备
     *
     * @param scanListener 搜索结果监听器
     * @param scanTime     搜索时间,以秒为单位
     * @return -1 蓝牙不支持  10 蓝牙没有打开  0 搜索执行成功
     */
    public int scan(final ScanListener scanListener, int scanTime) {
        return scan(scanListener, scanTime, null, null);
    }

    /**
     * 搜索指定厂商和设备类型的云锁设备
     *
     * @param scanListener 搜索结果监听器
     * @param scanTime     搜索时间,以秒为单位
     * @param vendorId     要搜索的厂商标识， 可为null
     * @param deviceType   要搜索的设备类型， 可为null
     * @return -1 蓝牙不支持  10 蓝牙没有打开  0 搜索执行成功
     */
    public int scan(final ScanListener scanListener, int scanTime, byte[] vendorId, byte[] deviceType) {
        ScanCallback filterScanCallback = new ScanCallback(new IScanCallback() {
            @Override
            public void onDeviceFound(BleDevice bleDevice, List<BleDevice> result) {
                if (scanListener != null) {
                    ScanDevice currentScanDevice = null; //本次扫描出的设备
                    List<ScanDevice> scanDevices = new ArrayList<>();
                    for (BleDevice device : result) {
                        ScanDevice scanDevice = new ScanDevice();
                        scanDevice.setBleDevice(device);
                        scanDevice.setAddress(device.getDeviceUUID());
                        scanDevices.add(scanDevice);

                        if (bleDevice.getDeviceUUID().equals(scanDevice.getAddress())) {
                            currentScanDevice = scanDevice;
                        }
                    }
                    scanListener.onScan(currentScanDevice, scanDevices);
                }
            }

            @Override
            public void onScanFinish(List<BleDevice> result) {
                if (scanListener != null) {
                    scanListener.onFinish();
                }
            }

            @Override
            public void onScanTimeout() {
                if (scanListener != null) {
                    scanListener.onFinish();
                }
            }
        });

        CloudLockFilter cloudLockFilter = new CloudLockFilter();
        cloudLockFilter.setVendorId(vendorId);
        cloudLockFilter.setDeviceType(deviceType);

        filterScanCallback.addFilter(cloudLockFilter);
        filterScanCallback.addFilter(new GateLockFilter());
        filterScanCallback.scanSecond(scanTime);

        return Ble.get().scan(filterScanCallback);
    }

    /**
     * 初始化云锁设备
     *
     * @param scanDevice 蓝牙低功耗设备
     * @param callBack   操作回调接口，初始化成功会返回CloudLock对象
     */
    public void initLock(final ScanDevice scanDevice, final CallBack callBack) {

        final String address = scanDevice.getAddress();
        final ClientHelper clientHelper = mConnectionManager.getBleHelper(address);
        if (clientHelper == null) {
            callBack.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        final InitLock initLock = new InitLock();
        initLock.setClientHelper(clientHelper);
        initLock.sendMsg(new BleCallBack<InitLock.Data>() {
            @Override
            public void success(InitLock.Data data) {

                if (callBack != null) {
                    CloudLock cloudLock = new CloudLock(address);
                    cloudLock.setBleDevice(scanDevice);
                    cloudLock.setAdminPassword(initLock.getAdminPassword());
                    cloudLock.setOpenLockPassword(initLock.getOpenLockPassword());
                    cloudLock.setEntryptKey(initLock.getSecretKey());
                    cloudLock.setEncryptType(initLock.getEncryptVersion());

                    ProductInfo productInfo = new ProductInfo();
                    productInfo.setVersion(data.version);
                    cloudLock.setProductInfo(productInfo);
                    setEncryptType(address, initLock.getEncryptVersion(), Base64.encode(initLock.getSecretKey()));

                    callBack.onSuccess(cloudLock);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {

                if (callBack != null) {
                    callBack.onFailed(errCode, errMsg);
                }
            }

            @Override
            public void timeout() {

                if (callBack != null) {
                    callBack.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });
    }

    /**
     * 确认初始化
     *
     * @param lock
     * @param callBack
     */
    public void confirmInit(final CloudLock lock, final CallBack callBack) {

        if (lock == null) {
            throw new NullPointerException("CloudLock对象不能为null");
        }

        final String address = lock.getAddress();
        final ClientHelper clientHelper = mConnectionManager.getBleHelper(address);
        if (clientHelper == null) {
            callBack.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        ConfirmInitLock confirmInitLock = new ConfirmInitLock(lock.getAdminPassword());
        confirmInitLock.setClientHelper(clientHelper);
        confirmInitLock.sendMsg(new BleCallBack<Void>() {
            @Override
            public void success(Void result) {
                if (callBack != null) {
                    lock.setActive(true);
                    callBack.onSuccess(lock);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {
                if (callBack != null) {
                    callBack.onFailed(errCode, errMsg);
                }
            }

            @Override
            public void timeout() {
                if (callBack != null) {
                    callBack.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });
    }

    private void setEncryptType(String address, int encryptType, String key) {
        ClientHelper clientHelper = mConnectionManager.getBleHelper(address);
        if (clientHelper == null) {
            return;
        }

        byte[] keyBytes = Base64.decode(key);

        switch (encryptType) {
            case ENCRYPT_TEA:
                clientHelper.setEncrypt(new TeaEncrypt(keyBytes));
                break;

            case ENCRYPT_AES:
                clientHelper.setEncrypt(new AesEncrypt(keyBytes));
                break;

            default:
        }
    }

    /**
     * 对指定云锁设备进行开锁操作
     *
     * @param lock     表示某个云锁设备， 初始化云锁成功后{@link #initLock(ScanDevice, CallBack)}会返回相应CloudLock对象
     * @param callBack 操作回调接口
     */
    public void openLock(final CloudLock lock, final CallBack callBack) {

        if (lock == null) {
            throw new NullPointerException("CloudLock对象不能为null");
        }

        ClientHelper clientHelper = mConnectionManager.getBleHelper(lock.getAddress());
        if (clientHelper == null) {
            callBack.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        setEncryptType(lock.getAddress(), lock.getEncryptType(), lock.getEntryptKeyString());

        int deviceNode = 1;
        if (lock.getDeviceId() == DeviceId.GATE_LOCK) {
            deviceNode = 2;
        }

        final WriteDeviceInfo openLock = new WriteDeviceInfo(lock.getOpenLockPassword(), (byte) deviceNode, new byte[]{1});
        openLock.setClientHelper(clientHelper);
        openLock.sendMsg(new BleCallBack<Void>() {
            @Override
            public void success(Void result) {
                if (callBack != null) {
                    callBack.onSuccess(lock);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {
                handleErrCode(lock, errCode, errMsg, openLock, callBack, this);
            }

            @Override
            public void timeout() {
                if (callBack != null) {
                    callBack.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });
    }

    /**
     * 控制指定云锁设备的电机进行正转操作
     *
     * @param lock     表示某个云锁设备， 初始化云锁成功后{@link #initLock(ScanDevice, CallBack)}会返回相应CloudLock对象
     * @param callBack 操作回调接口
     */
    public void setMotorForward(final CloudLock lock, final CallBack callBack) {

        if (lock == null) {
            throw new NullPointerException("CloudLock对象不能为null");
        }

        ClientHelper clientHelper = mConnectionManager.getBleHelper(lock.getAddress());
        if (clientHelper == null) {
            callBack.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        setEncryptType(lock.getAddress(), lock.getEncryptType(), lock.getEntryptKeyString());
        final WriteDeviceInfo writeDeviceInfo = new WriteDeviceInfo(lock.getOpenLockPassword(), (byte) 1, new byte[]{1});
        writeDeviceInfo.setClientHelper(clientHelper);
        writeDeviceInfo.sendMsg(new BleCallBack<Void>() {
            @Override
            public void success(Void result) {
                if (callBack != null) {
                    callBack.onSuccess(lock);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {
                handleErrCode(lock, errCode, errMsg, writeDeviceInfo, callBack, this);
            }

            @Override
            public void timeout() {
                if (callBack != null) {
                    callBack.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });
    }

    /**
     * 控制指定云锁设备的电机进行反转操作
     *
     * @param lock     表示某个云锁设备， 初始化云锁成功后{@link #initLock(ScanDevice, CallBack)}会返回相应CloudLock对象
     * @param callBack 操作回调接口
     */
    public void setMotorReverse(final CloudLock lock, final CallBack callBack) {

        if (lock == null) {
            throw new NullPointerException("CloudLock对象不能为null");
        }

        ClientHelper clientHelper = mConnectionManager.getBleHelper(lock.getAddress());
        if (clientHelper == null) {
            callBack.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        setEncryptType(lock.getAddress(), lock.getEncryptType(), lock.getEntryptKeyString());
        final WriteDeviceInfo writeDeviceInfo = new WriteDeviceInfo(lock.getOpenLockPassword(), (byte) 1, new byte[]{0});
        writeDeviceInfo.setClientHelper(clientHelper);
        writeDeviceInfo.sendMsg(new BleCallBack<Void>() {
            @Override
            public void success(Void result) {
                if (callBack != null) {
                    callBack.onSuccess(lock);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {
                handleErrCode(lock, errCode, errMsg, writeDeviceInfo, callBack, this);
            }

            @Override
            public void timeout() {
                if (callBack != null) {
                    callBack.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });
    }

    /**
     * 重置指定云锁设备
     *
     * @param lock     表示某个云锁设备， 初始化云锁成功后{@link #initLock(ScanDevice, CallBack)}会返回相应CloudLock对象
     * @param callBack 操作回调接口
     */
    public void resetLock(final CloudLock lock, final CallBack callBack) {

        if (lock == null) {
            throw new NullPointerException("CloudLock对象不能为null");
        }

        ClientHelper clientHelper = mConnectionManager.getBleHelper(lock.getAddress());
        if (clientHelper == null) {
            callBack.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        setEncryptType(lock.getAddress(), lock.getEncryptType(), lock.getEntryptKeyString());
        final ResetLock resetLock = new ResetLock(lock.getAdminPassword());
        resetLock.setClientHelper(clientHelper);
        resetLock.sendMsg(new BleCallBack<Void>() {
            @Override
            public void success(Void result) {
                if (callBack != null) {
                    callBack.onSuccess(lock);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {
                handleErrCode(lock, errCode, errMsg, resetLock, callBack, this);
            }

            @Override
            public void timeout() {
                if (callBack != null) {
                    callBack.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });
    }

    /**
     * 主设备读从设备指定节点信息
     *
     * @param lock
     * @param callBack
     */
    public void readDeviceInfo(final CloudLock lock, final CallBack callBack) {
        if (lock == null) {
            throw new NullPointerException("CloudLock对象不能为null");
        }

        ClientHelper clientHelper = mConnectionManager.getBleHelper(lock.getAddress());
        if (clientHelper == null) {
            callBack.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        setEncryptType(lock.getAddress(), lock.getEncryptType(), lock.getEntryptKeyString());

        final ReadDeviceInfo readDeviceInfo = new ReadDeviceInfo(lock.getDeviceNum());
        readDeviceInfo.setClientHelper(clientHelper);
        readDeviceInfo.sendMsg(new BleCallBack<ReadDeviceInfo.Data>() {
            @Override
            public void success(ReadDeviceInfo.Data result) {
                if (callBack != null) {
                    lock.addDeviceInfo(result.deviceNum, result.value);
                    callBack.onSuccess(lock);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {
                handleErrCode(lock, errCode, errMsg, readDeviceInfo, callBack, this);
            }

            @Override
            public void timeout() {
                if (callBack != null) {
                    callBack.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });

    }

    /**
     * 主设备读从设备多点节点信息
     *
     * @param lock
     * @param callBack
     */
    public void readMutilDeviceInfo(final CloudLock lock, final CallBack callBack) {
        if (lock == null) {
            throw new NullPointerException("CloudLock对象不能为null");
        }

        ClientHelper clientHelper = mConnectionManager.getBleHelper(lock.getAddress());
        if (clientHelper == null) {
            callBack.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        setEncryptType(lock.getAddress(), lock.getEncryptType(), lock.getEntryptKeyString());
        final ReadDeviceMutilInfo readDeviceMutilInfo = new ReadDeviceMutilInfo();
        readDeviceMutilInfo.setClientHelper(clientHelper);
        readDeviceMutilInfo.sendMsg(new BleCallBack<ReadDeviceMutilInfo.Data>() {
            @Override
            public void success(ReadDeviceMutilInfo.Data result) {
                int count = result.deviceNodeCount;
                ByteBuffer buffer = ByteBuffer.wrap(result.deviceValues);
                Map<Byte, byte[]> map = new HashMap<>();

                for (int i = 0; i < count; i++) {

                    byte deviceNum = buffer.get();
                    byte[] deviceNodeInfo;

                    if (bleLink instanceof JinouxBleLink) {
                        GateLockNodeInfo nodeInfo = new GateLockNodeInfo(deviceNum);
                        deviceNodeInfo = new byte[nodeInfo.getLength(deviceNum)];
                    } else {
                        CloudLockNodeInfo nodeInfo = new CloudLockNodeInfo(deviceNum);
                        deviceNodeInfo = new byte[nodeInfo.getLength(deviceNum)];
                    }

                    buffer.get(deviceNodeInfo);
                    map.put(deviceNum, deviceNodeInfo);
                }
                lock.setDeviceInfoMap(map);

                if (callBack != null) {
                    callBack.onSuccess(lock);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {
                handleErrCode(lock, errCode, errMsg, readDeviceMutilInfo, callBack, this);
            }

            @Override
            public void timeout() {
                if (callBack != null) {
                    callBack.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });
    }

    /**
     * 获取设备产品信息
     *
     * @param lock
     * @param callBack
     */
    public void getProductInfo(final CloudLock lock, final CallBack callBack) {
        if (lock == null) {
            throw new NullPointerException("CloudLock对象不能为null");
        }

        ClientHelper clientHelper = mConnectionManager.getBleHelper(lock.getAddress());
        if (clientHelper == null) {
            callBack.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        final GetProductInfo getProductInfo = new GetProductInfo();
        getProductInfo.setClientHelper(clientHelper);
        getProductInfo.sendMsg(new BleCallBack<ProductInfo>() {
            @Override
            public void success(ProductInfo productInfo) {
                lock.setProductInfo(productInfo);
                if (callBack != null) {
                    callBack.onSuccess(lock);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {
                if (callBack != null) {
                    callBack.onFailed(errCode, errMsg);
                }
            }

            @Override
            public void timeout() {
                if (callBack != null) {
                    callBack.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });
    }

    /**
     * 写入从设备生产序列号
     *
     * @param lock
     * @param callBack
     */
    public void writeSerialNum(final CloudLock lock, final CallBack callBack) {
        if (lock == null) {
            throw new NullPointerException("CloudLock对象不能为null");
        }

        ClientHelper clientHelper = mConnectionManager.getBleHelper(lock.getAddress());
        if (clientHelper == null) {
            callBack.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        WriteProductionSerialNum writeProductionSerialNum = new WriteProductionSerialNum(lock.getSerialNum());
        writeProductionSerialNum.setClientHelper(clientHelper);
        writeProductionSerialNum.sendMsg(new BleCallBack<Void>() {
            @Override
            public void success(Void result) {
                if (callBack != null) {
                    callBack.onSuccess(lock);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {
                if (callBack != null) {
                    callBack.onFailed(errCode, errMsg);
                }
            }

            @Override
            public void timeout() {
                if (callBack != null) {
                    callBack.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });
    }

    /**
     * 读取生产序列号， 读取到的数据存放到参数lock里
     *
     * @param lock
     * @param callBack
     */
    public void readSerialNum(final CloudLock lock, final CallBack callBack) {
        if (lock == null) {
            throw new NullPointerException("CloudLock对象不能为null");
        }

        ClientHelper clientHelper = mConnectionManager.getBleHelper(lock.getAddress());
        if (clientHelper == null) {
            callBack.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        ReadProductionSerialNum readProductionSerialNum = new ReadProductionSerialNum();
        readProductionSerialNum.setClientHelper(clientHelper);
        readProductionSerialNum.sendMsg(new BleCallBack<ReadProductionSerialNum.Data>() {
            @Override
            public void success(ReadProductionSerialNum.Data result) {
                if (callBack != null) {
                    lock.setSerialNum(result.productionSerialNum);
                    callBack.onSuccess(lock);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {
                if (callBack != null) {
                    callBack.onFailed(errCode, errMsg);
                }
            }

            @Override
            public void timeout() {
                if (callBack != null) {
                    callBack.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });
    }

    /**
     * 写入从设备厂商标识
     *
     * @param lock
     * @param callBack
     */
    public void writeVendorId(final CloudLock lock, final CallBack callBack) {
        if (lock == null) {
            throw new NullPointerException("CloudLock对象不能为null");
        }

        ClientHelper clientHelper = mConnectionManager.getBleHelper(lock.getAddress());
        if (clientHelper == null) {
            callBack.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        WriteVendorId writeVendorId = new WriteVendorId(lock.getVendorId(), lock.getDeviceType());
        writeVendorId.setClientHelper(clientHelper);
        writeVendorId.sendMsg(new BleCallBack<Void>() {
            @Override
            public void success(Void result) {
                if (callBack != null) {
                    callBack.onSuccess(lock);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {
                if (callBack != null) {
                    callBack.onFailed(errCode, errMsg);
                }
            }

            @Override
            public void timeout() {
                if (callBack != null) {
                    callBack.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });
    }

    /**
     * 读取从设备厂商标识和设备类型， 读取到的数据存放到参数lock里
     *
     * @param lock
     * @param callBack
     */
    public void readVendorId(final CloudLock lock, final CallBack callBack) {
        if (lock == null) {
            throw new NullPointerException("CloudLock对象不能为null");
        }

        ClientHelper clientHelper = mConnectionManager.getBleHelper(lock.getAddress());
        if (clientHelper == null) {
            callBack.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        ReadVendorId readVendorId = new ReadVendorId();
        readVendorId.setClientHelper(clientHelper);
        readVendorId.sendMsg(new BleCallBack<ReadVendorId.Data>() {
            @Override
            public void success(ReadVendorId.Data result) {
                if (callBack != null) {
                    lock.setVendorId(result.vendorId);
                    lock.setDeviceType(result.deviceType);
                    callBack.onSuccess(lock);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {
                if (callBack != null) {
                    callBack.onFailed(errCode, errMsg);
                }
            }

            @Override
            public void timeout() {
                if (callBack != null) {
                    callBack.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });
    }

    /**
     * 读取自增变量
     *
     * @param lock     表示某个云锁设备， 初始化云锁成功后{@link #initLock(ScanDevice, CallBack)}会返回相应CloudLock对象
     * @param callback 操作回调接口
     */
    public void readAutoIncreaseNum(final CloudLock lock, final CallBack callback) {

        if (lock == null) {
            throw new NullPointerException("CloudLock对象不能为null");
        }

        ClientHelper clientHelper = mConnectionManager.getBleHelper(lock.getAddress());
        setEncryptType(lock.getAddress(), lock.getEncryptType(), lock.getEntryptKeyString());
        if (clientHelper == null) {
            callback.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        ReadAutoIncreaseNum cmd = new ReadAutoIncreaseNum();
        cmd.setClientHelper(clientHelper);
        cmd.sendMsg(new BleCallBack<ReadAutoIncreaseNum.Data>() {
            @Override
            public void success(ReadAutoIncreaseNum.Data result) {
                if (callback != null) {
                    lock.setAutuIncreaseNum(result.autoIncreaseNum);
                    callback.onSuccess(lock);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {
                if (callback != null) {
                    callback.onFailed(errCode, ErrCode.getMessage(errCode));
                }
            }

            @Override
            public void timeout() {
                if (callback != null) {
                    callback.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });
    }

    public void readAutoIncreaseNum(String mac, int encryptType, String encryptKey, final CallBack2<Short> callback) {

        ClientHelper clientHelper = mConnectionManager.getBleHelper(mac);
        setEncryptType(mac, encryptType, encryptKey);
        if (clientHelper == null) {
            callback.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        ReadAutoIncreaseNum cmd = new ReadAutoIncreaseNum();
        cmd.setClientHelper(clientHelper);
        cmd.sendMsg(new BleCallBack<ReadAutoIncreaseNum.Data>() {
            @Override
            public void success(ReadAutoIncreaseNum.Data result) {
                if (callback != null) {
                    callback.onSuccess(result.autoIncreaseNum);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {
                if (callback != null) {
                    callback.onFailed(errCode, ErrCode.getMessage(errCode));
                }
            }

            @Override
            public void timeout() {
                if (callback != null) {
                    callback.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });
    }

    public void readCLoudLockOpenLockRecord(final String mac, final int encryptType, final String encryptKey, int readSerialNum,
                                            final CallBack2<List<CloudLockOperateRecord>> callback) {

        if (!(readSerialNum <= 40 && readSerialNum >= 1)) {
            if (callback != null) {
                callback.onFailed(ERR_READ_SERIAL_NUM, ErrCode.getMessage(ERR_READ_SERIAL_NUM));
            }
            return;
        }

        ClientHelper clientHelper = mConnectionManager.getBleHelper(mac);
        setEncryptType(mac, encryptType, encryptKey);
        if (clientHelper == null) {
            callback.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        final ReadOpenLockRecord readOpenLockRecord = new ReadOpenLockRecord(readSerialNum);
        readOpenLockRecord.setClientHelper(clientHelper);
        readOpenLockRecord.sendMsg(new BleCallBack<ReadOpenLockRecord.Data>() {
            @Override
            public void success(ReadOpenLockRecord.Data result) {
                if (callback != null) {
                    callback.onSuccess(result.operateRecords);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {
                handleErrCode(mac, encryptType, encryptKey, errCode, errMsg, readOpenLockRecord, callback, this);
            }

            @Override
            public void timeout() {
                if (callback != null) {
                    callback.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });
    }

    /* -----------------------------------智能门锁------------------------------------------*/

    public void batchUpdateAuthInfos(final String mac, final int encryptType, final String encryptKey, List<AuthInfo> authInfos, final CallBack2<Void> callback) {

        ClientHelper clientHelper = mConnectionManager.getBleHelper(mac);
        setEncryptType(mac, encryptType, encryptKey);
        if (clientHelper == null) {
            callback.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        final BatchUpdateAuthInfos batchUpdateAuthInfos = new BatchUpdateAuthInfos(authInfos);
        batchUpdateAuthInfos.setClientHelper(clientHelper);
        batchUpdateAuthInfos.sendMsg(new BleCallBack<Void>() {
            @Override
            public void success(Void result) {
                if (callback != null) {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {
                handleErrCode(mac, encryptType, encryptKey, errCode, errMsg, batchUpdateAuthInfos, callback, this);
            }

            @Override
            public void timeout() {
                if (callback != null) {
                    callback.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });
    }

    public void deleteKey(final String mac, final int encryptType, final String encryptKey, int keyId, final CallBack2<Void> callback) {

        ClientHelper clientHelper = mConnectionManager.getBleHelper(mac);
        setEncryptType(mac, encryptType, encryptKey);
        if (clientHelper == null) {
            callback.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        final DeleteKey deleteKey = new DeleteKey(keyId);
        deleteKey.setClientHelper(clientHelper);
        deleteKey.sendMsg(new BleCallBack<Void>() {
            @Override
            public void success(Void result) {
                if (callback != null) {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {
                handleErrCode(mac, encryptType, encryptKey, errCode, errMsg, deleteKey, callback, this);
            }

            @Override
            public void timeout() {
                if (callback != null) {
                    callback.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });
    }

    public void addAuth(String mac, int encryptType, String encryptKey, AuthInfo authInfo, final CallBack2<Integer> callback) {
        operateAuthTable(mac, encryptType, encryptKey, OperateAuthTable.ADD, -1, authInfo, new CallBack2<OperateAuthTable.Data>() {
            @Override
            public void onSuccess(OperateAuthTable.Data data) {
                if (callback != null) {
                    callback.onSuccess((int) data.authId);
                }
            }

            @Override
            public void onFailed(int errCode, String errMsg) {
                if (callback != null) {
                    callback.onFailed(errCode, errMsg);
                }
            }
        });
    }

    public void deleteAuth(String mac, int encryptType, String encryptKey, int authId, final CallBack2<Void> callback) {
        operateAuthTable(mac, encryptType, encryptKey, OperateAuthTable.DELETE, authId, null, new CallBack2<OperateAuthTable.Data>() {
            @Override
            public void onSuccess(OperateAuthTable.Data data) {
                if (callback != null) {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onFailed(int errCode, String errMsg) {
                if (callback != null) {
                    callback.onFailed(errCode, errMsg);
                }
            }
        });
    }

    public void updateAuth(String mac, int encryptType, String encryptKey, AuthInfo authInfo, final CallBack2<Void> callback) {
        operateAuthTable(mac, encryptType, encryptKey, OperateAuthTable.UPDATE, -1, authInfo, new CallBack2<OperateAuthTable.Data>() {
            @Override
            public void onSuccess(OperateAuthTable.Data data) {
                if (callback != null) {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onFailed(int errCode, String errMsg) {
                if (callback != null) {
                    callback.onFailed(errCode, errMsg);
                }
            }
        });
    }

    public void queryAuthById(String mac, int encryptType, String encryptKey, int authId, final CallBack2<AuthInfo> callback) {
        operateAuthTable(mac, encryptType, encryptKey, OperateAuthTable.QUERY, authId, null, new CallBack2<OperateAuthTable.Data>() {
            @Override
            public void onSuccess(OperateAuthTable.Data data) {
                if (callback != null) {
                    callback.onSuccess(data.authInfos.get(0));
                }
            }

            @Override
            public void onFailed(int errCode, String errMsg) {
                if (callback != null) {
                    callback.onFailed(errCode, errMsg);
                }
            }
        });
    }

    public void queryAllAuth(String mac, int encryptType, String encryptKey, final CallBack2<List<AuthInfo>> callback) {
        operateAuthTable(mac, encryptType, encryptKey, OperateAuthTable.QUERY, 0xFF, null, new CallBack2<OperateAuthTable.Data>() {
            @Override
            public void onSuccess(OperateAuthTable.Data data) {
                if (callback != null) {
                    callback.onSuccess(data.authInfos);
                }
            }

            @Override
            public void onFailed(int errCode, String errMsg) {
                if (callback != null) {
                    callback.onFailed(errCode, errMsg);
                }
            }
        });
    }

    private void operateAuthTable(final String mac, final int encryptType, final String encryptKey, int operateType, int operateAuthId, AuthInfo operateAuthInfo,
                                  final CallBack2<OperateAuthTable.Data> callback) {

        ClientHelper clientHelper = mConnectionManager.getBleHelper(mac);
        setEncryptType(mac, encryptType, encryptKey);
        if (clientHelper == null) {
            callback.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        final OperateAuthTable operateAuthTable = new OperateAuthTable(operateType);
        operateAuthTable.setClientHelper(clientHelper);

        switch (operateType) {
            case OperateAuthTable.ADD:
            case OperateAuthTable.UPDATE:
                operateAuthTable.setAuthInfo(operateAuthInfo);
                break;

            case OperateAuthTable.DELETE:
            case OperateAuthTable.QUERY:
                operateAuthTable.setAuthId(operateAuthId);
                break;

            default:
        }

        operateAuthTable.sendMsg(new BleCallBack<OperateAuthTable.Data>() {
            @Override
            public void success(OperateAuthTable.Data result) {
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {
                handleErrCode(mac, encryptType, encryptKey, errCode, errMsg, operateAuthTable, callback, this);
            }

            @Override
            public void timeout() {
                if (callback != null) {
                    callback.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });
    }

    public void readAuthCountInfo(final String mac, final int encryptType, final String encryptKey, final CallBack2<List<AuthCountInfo>> callback) {
        ClientHelper clientHelper = mConnectionManager.getBleHelper(mac);
        setEncryptType(mac, encryptType, encryptKey);
        if (clientHelper == null) {
            callback.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        final ReadAuthCountInfo readAuthCountInfo = new ReadAuthCountInfo();
        readAuthCountInfo.setClientHelper(clientHelper);
        readAuthCountInfo.sendMsg(new BleCallBack<ReadAuthCountInfo.Data>() {
            @Override
            public void success(ReadAuthCountInfo.Data result) {
                if (callback != null) {
                    callback.onSuccess(result.authCountInfos);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {
                handleErrCode(mac, encryptType, encryptKey, errCode, errMsg, readAuthCountInfo, callback, this);
            }

            @Override
            public void timeout() {
                if (callback != null) {
                    callback.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });
    }

    public void readKeyInfos(final String mac, final int encryptType, final String encryptKey, final CallBack2<List<GateLockKey>> callback) {
        ClientHelper clientHelper = mConnectionManager.getBleHelper(mac);
        setEncryptType(mac, encryptType, encryptKey);
        if (clientHelper == null) {
            callback.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        final ReadKeyInfos readKeyInfos = new ReadKeyInfos();
        readKeyInfos.setClientHelper(clientHelper);
        readKeyInfos.sendMsg(new BleCallBack<ReadKeyInfos.Data>() {
            @Override
            public void success(ReadKeyInfos.Data result) {
                if (callback != null) {
                    callback.onSuccess(result.keys);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {
                handleErrCode(mac, encryptType, encryptKey, errCode, errMsg, readKeyInfos, callback, this);
            }

            @Override
            public void timeout() {
                if (callback != null) {
                    callback.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });
    }

    public void writeKeyInfos(final String mac, final int encryptType, final String encryptKey, List<GateLockKey> gateLockKeys,
                              final CallBack2<Void> callback) {
        ClientHelper clientHelper = mConnectionManager.getBleHelper(mac);
        setEncryptType(mac, encryptType, encryptKey);
        if (clientHelper == null) {
            callback.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        final WriteKeyInfos writeKeyInfos = new WriteKeyInfos(gateLockKeys);
        writeKeyInfos.setClientHelper(clientHelper);
        writeKeyInfos.sendMsg(new BleCallBack<Void>() {
            @Override
            public void success(Void result) {
                if (callback != null) {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {
                handleErrCode(mac, encryptType, encryptKey, errCode, errMsg, writeKeyInfos, callback, this);
            }

            @Override
            public void timeout() {
                if (callback != null) {
                    callback.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });
    }

    public void readGateLockOpenLockRecord(final String mac, final int encryptType, final String encryptKey, int readSerialNum,
                                           final CallBack2<List<GateLockOperateRecord>> callback) {
        if (!(readSerialNum <= 40 && readSerialNum >= 1)) {
            if (callback != null) {
                callback.onFailed(ERR_READ_SERIAL_NUM, ErrCode.getMessage(ERR_READ_SERIAL_NUM));
            }
            return;
        }

        ClientHelper clientHelper = mConnectionManager.getBleHelper(mac);
        setEncryptType(mac, encryptType, encryptKey);
        if (clientHelper == null) {
            callback.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        final ReadGateLockOpenLockRecord readGateLockOpenLockRecord = new ReadGateLockOpenLockRecord(readSerialNum);
        readGateLockOpenLockRecord.setClientHelper(clientHelper);
        readGateLockOpenLockRecord.sendMsg(new BleCallBack<ReadGateLockOpenLockRecord.Data>() {
            @Override
            public void success(ReadGateLockOpenLockRecord.Data result) {
                if (callback != null) {
                    callback.onSuccess(result.records);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {
                handleErrCode(mac, encryptType, encryptKey, errCode, errMsg, readGateLockOpenLockRecord, callback, this);
            }

            @Override
            public void timeout() {
                if (callback != null) {
                    callback.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });
    }

    public void readTime(final String mac, final int encryptType, final String encryptKey, final CallBack2<Long> callback) {
        ClientHelper clientHelper = mConnectionManager.getBleHelper(mac);
        setEncryptType(mac, encryptType, encryptKey);
        if (clientHelper == null) {
            callback.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        final ReadTime readTime = new ReadTime();
        readTime.setClientHelper(clientHelper);
        readTime.sendMsg(new BleCallBack<ReadTime.Data>() {
            @Override
            public void success(ReadTime.Data result) {
                if (callback != null) {
                    callback.onSuccess(result.time);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {
                handleErrCode(mac, encryptType, encryptKey, errCode, errMsg, readTime, callback, this);
            }

            @Override
            public void timeout() {
                if (callback != null) {
                    callback.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });
    }

    public void writeTime(final String mac, final int encryptType, final String encryptKey, final CallBack2<Void> callback) {
        ClientHelper clientHelper = mConnectionManager.getBleHelper(mac);
        setEncryptType(mac, encryptType, encryptKey);
        if (clientHelper == null) {
            callback.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        final WriteTime writeTime = new WriteTime();
        writeTime.setClientHelper(clientHelper);
        writeTime.sendMsg(new BleCallBack<Void>() {
            @Override
            public void success(Void result) {
                if (callback != null) {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {
                handleErrCode(mac, encryptType, encryptKey, errCode, errMsg, writeTime, callback, this);
            }

            @Override
            public void timeout() {
                if (callback != null) {
                    callback.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });
    }

    public void openGateLock(final String mac, final int encryptType, final String encryptKey, byte[] openLockPassword,
                             final CallBack2<Void> callback) {
        operateLock(mac, encryptType, encryptKey, openLockPassword, OperateLock.OPERATE_OPEN_LOCK, OperateLock.SINGLE_CONTROL,
                0, GateLockNodeInfo.NODE_AUTH_CONTROL, 1, callback);
    }

    public void openCloudLock(final String mac, final int encryptType, final String encryptKey, byte[] openLockPassword,
                             final CallBack2<Void> callback) {
        operateLock(mac, encryptType, encryptKey, openLockPassword, OperateLock.OPERATE_OPEN_LOCK, OperateLock.SINGLE_CONTROL,
                0, CloudLockNodeInfo.NODE_LOCK_CONTROL, 1, callback);
    }

    public void operateLock(final String mac, final int encryptType, final String encryptKey, byte[] openLockPassword, int operateType,
                            int controlType, int gapTime, int deviceNode, int value, final CallBack2<Void> callback) {
        ClientHelper clientHelper = mConnectionManager.getBleHelper(mac);
        setEncryptType(mac, encryptType, encryptKey);
        if (clientHelper == null) {
            callback.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return;
        }

        final OperateLock operateLock = new OperateLock(openLockPassword, operateType, controlType, gapTime, deviceNode, value);
        operateLock.setClientHelper(clientHelper);
        operateLock.sendMsg(new BleCallBack<Void>() {
            @Override
            public void success(Void result) {
                if (callback != null) {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void fail(int errCode, String errMsg) {
                handleErrCode(mac, encryptType, encryptKey, errCode, errMsg, operateLock, callback, this);
            }

            @Override
            public void timeout() {
                if (callback != null) {
                    callback.onFailed(ERR_TIMEOUT, ErrCode.getMessage(ERR_TIMEOUT));
                }
            }
        });
    }

    private ClientHelper init(String mac, int encryptType, String encryptKey, CallBack2 callback) {
        ClientHelper clientHelper = mConnectionManager.getBleHelper(mac);
        setEncryptType(mac, encryptType, encryptKey);
        if (clientHelper == null) {
            callback.onFailed(ErrCode.ERR_NO_CONNECT, ErrCode.getMessage(ErrCode.ERR_NO_CONNECT));
            return null;
        }
        return clientHelper;
    }

    /**
     * 处理相应应答异常
     *
     * @param cloudLock
     * @param errCode
     * @param cmd       应答异常的命令
     * @param callBack  命令回调
     */
    private void handleErrCode(CloudLock cloudLock, int errCode, String errMessage, final BleCmdBase cmd, final CallBack callBack, final BleCallBack bleCallBack) {
        if (errCode == ErrCode.ERR_REPEAT_CODE) {
            Log.i("autuIncreaseNum error，start read autoIncreaseNum");
            readAutoIncreaseNum(cloudLock, new CallBack() {
                @Override
                public void onSuccess(CloudLock cloudLock) {
                    int autoIncreaseNum = cloudLock.getAutuIncreaseNum();
                    Log.i("read autoIncreaseNum success:" + autoIncreaseNum);
                    BleCmdBase.setAutoIncreaseNum(autoIncreaseNum);
                    sendCmd(cmd, bleCallBack);
                }

                @Override
                public void onFailed(int errCode, String errMsg) {
                    Log.i("read autoIncreaseNum failed:" + errMsg);
                    if (callBack != null) {
                        callBack.onFailed(errCode, errMsg);
                    }
                }
            });
            return;
        }

        if (callBack != null) {
            callBack.onFailed(errCode, errMessage);
        }


    }

    private void handleErrCode(String mac, int encryptType, String encryptKey, int errCode, String errMessage, final BleCmdBase cmd, final CallBack2 callBack, final BleCallBack bleCallBack) {
        if (errCode == ErrCode.ERR_REPEAT_CODE) {
            Log.i("autuIncreaseNum error，start read autoIncreaseNum");
            readAutoIncreaseNum(mac, encryptType, encryptKey, new CallBack2<Short>() {
                @Override
                public void onSuccess(Short autoIncreaseNum) {
                    Log.i("read autoIncreaseNum success:" + autoIncreaseNum);
                    BleCmdBase.setAutoIncreaseNum(autoIncreaseNum);
                    sendCmd(cmd, bleCallBack);
                }

                @Override
                public void onFailed(int errCode, String errMsg) {
                    Log.i("read autoIncreaseNum failed:" + errMsg);
                    if (callBack != null) {
                        callBack.onFailed(errCode, errMsg);
                    }
                }
            });
            return;
        }

        if (callBack != null) {
            callBack.onFailed(errCode, errMessage);
        }


    }

    private void sendCmd(BleCmdBase cmd, BleCallBack bleCallBack) {
        cmd.sendMsg(bleCallBack);
    }

    /**
     * 添加锁状态信息监听器
     *
     * @param lockAddress       云锁设备MAC地址
     * @param lockStateListener 锁状态信息监听器
     */
    public void addLockStateListener(String lockAddress, LockStateListener lockStateListener) {
        mConnectionManager.addLockStateListener(lockAddress, lockStateListener);
    }
}
