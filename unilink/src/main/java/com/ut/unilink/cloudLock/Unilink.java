package com.ut.unilink.cloudLock;

import android.content.Context;

import com.ut.unilink.cloudLock.protocol.AesEncrypt;
import com.ut.unilink.cloudLock.protocol.ClientHelper;
import com.ut.unilink.cloudLock.protocol.TeaEncrypt;
import com.ut.unilink.cloudLock.protocol.cmd.BleCallBack;
import com.ut.unilink.cloudLock.protocol.cmd.BleCmdBase;
import com.ut.unilink.cloudLock.protocol.cmd.ConfirmInitLock;
import com.ut.unilink.cloudLock.protocol.cmd.ErrCode;
import com.ut.unilink.cloudLock.protocol.cmd.GetProductInfo;
import com.ut.unilink.cloudLock.protocol.cmd.InitLock;
import com.ut.unilink.cloudLock.protocol.cmd.ReadAutoIncreaseNum;
import com.ut.unilink.cloudLock.protocol.cmd.ReadDeviceInfo;
import com.ut.unilink.cloudLock.protocol.cmd.ReadDeviceMutilInfo;
import com.ut.unilink.cloudLock.protocol.cmd.ReadProductionSerialNum;
import com.ut.unilink.cloudLock.protocol.cmd.ReadVendorId;
import com.ut.unilink.cloudLock.protocol.cmd.WriteDeviceInfo;
import com.ut.unilink.cloudLock.protocol.cmd.ResetLock;
import com.ut.unilink.cloudLock.protocol.cmd.WriteProductionSerialNum;
import com.ut.unilink.cloudLock.protocol.cmd.WriteVendorId;
import com.ut.unilink.cloudLock.protocol.data.DeviceNodeInfo;
import com.ut.unilink.cloudLock.protocol.data.ProductInfo;
import com.ut.unilink.util.Log;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static com.ut.unilink.cloudLock.protocol.cmd.ErrCode.ERR_TIMEOUT;

public class Unilink extends UTBleLink {

    private CloundLockConnectionManager mConnectionManager;
    private static final int ENCRYPT_TEA = 0;
    private static final int ENCRYPT_AES = 1;
    private static final int NO_ENCRYPT = -1;

    public Unilink(Context context) {
        super(context);
        mConnectionManager = new CloundLockConnectionManager(this);
        setConnectionManager(mConnectionManager);
    }

    public boolean isConnect(String address) {
        return mConnectionManager.isConnect(address);
    }

    /**
     * 连接指定蓝牙设备
     *
     * @param address           设备MAC地址
     * @param connectListener   监听连接结果
     * @param lockStateListener 连接成功后，监听云锁的状态信息
     */
    public void connect(String address, ConnectListener connectListener, LockStateListener lockStateListener) {
        addLockStateListener(address, lockStateListener);
        super.connect(address, connectListener);
    }

    /**
     * 连接指定蓝牙设备
     *
     * @param scanDevice         蓝牙低功耗设备
     * @param connectListener   监听连接结果
     * @param lockStateListener 连接成功后，监听云锁的状态信息
     */
    public void connect(ScanDevice scanDevice, ConnectListener connectListener, LockStateListener lockStateListener) {
        connect(scanDevice.getAddress(), connectListener, lockStateListener);
    }

    /**
     * 连接指定蓝牙设备
     *
     * @param scanDevice       蓝牙低功耗设备
     * @param connectListener 监听连接结果
     */
    public void connect(ScanDevice scanDevice, ConnectListener connectListener) {
        super.connect(scanDevice.getAddress(), connectListener);
    }

    /**
     * 初始化云锁设备
     *
     * @param scanDevice 蓝牙低功耗设备
     * @param callBack  操作回调接口，初始化成功会返回CloudLock对象
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
                    setEncryptType(address, initLock.getEncryptVersion(), initLock.getSecretKey());

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

    private void setEncryptType(String address, int encryptType, byte[] key) {
        ClientHelper clientHelper = mConnectionManager.getBleHelper(address);
        if (clientHelper == null) {
            return;
        }

        switch (encryptType) {
            case ENCRYPT_TEA:
                clientHelper.setEncrypt(new TeaEncrypt(key));
                break;

            case ENCRYPT_AES:
                clientHelper.setEncrypt(new AesEncrypt(key));
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

        setEncryptType(lock.getAddress(), lock.getEncryptType(), lock.getEntryptKey());
        final WriteDeviceInfo openLock = new WriteDeviceInfo(lock.getOpenLockPassword(), (byte) 1, new byte[]{1});
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

        setEncryptType(lock.getAddress(), lock.getEncryptType(), lock.getEntryptKey());
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

        setEncryptType(lock.getAddress(), lock.getEncryptType(), lock.getEntryptKey());
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

        setEncryptType(lock.getAddress(), lock.getEncryptType(), lock.getEntryptKey());
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

        setEncryptType(lock.getAddress(), lock.getEncryptType(), lock.getEntryptKey());

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

        setEncryptType(lock.getAddress(), lock.getEncryptType(), lock.getEntryptKey());
        final ReadDeviceMutilInfo readDeviceMutilInfo = new ReadDeviceMutilInfo();
        readDeviceMutilInfo.setClientHelper(clientHelper);
        readDeviceMutilInfo.sendMsg(new BleCallBack<ReadDeviceMutilInfo.Data>() {
            @Override
            public void success(ReadDeviceMutilInfo.Data result) {
                int count = result.deviceNodeCount;
                ByteBuffer buffer = ByteBuffer.wrap(result.deviceValues);
                Map<Byte, byte[]> map = new HashMap<>();

                for (int i=0; i<count; i++) {

                    byte deviceNum = buffer.get();
                    byte[] deviceNodeInfo = new byte[DeviceNodeInfo.getLength(deviceNum)];
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
     * @param lock
     * @param callBack
     */
    public void writeVendorId(final CloudLock lock, final CallBack callBack) {
        if (lock == null ) {
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
        setEncryptType(lock.getAddress(), lock.getEncryptType(), lock.getEntryptKey());
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
                    sendCmd(cloudLock, cmd, callBack, bleCallBack);
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

    private void sendCmd(final CloudLock cloudLock, BleCmdBase cmd, final CallBack callBack, BleCallBack bleCallBack) {
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
