package ut.com.cloudlocksdk;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ut.unilink.UnilinkManager;
import com.ut.unilink.cloudLock.CallBack;
import com.ut.unilink.cloudLock.CallBack2;
import com.ut.unilink.cloudLock.CloudLock;
import com.ut.unilink.cloudLock.ConnectListener;
import com.ut.unilink.cloudLock.LockStateListener;
import com.ut.unilink.cloudLock.ScanDevice;
import com.ut.unilink.cloudLock.protocol.data.AuthCountInfo;
import com.ut.unilink.cloudLock.protocol.data.AuthInfo;
import com.ut.unilink.cloudLock.protocol.data.CloudLockOperateRecord;
import com.ut.unilink.cloudLock.protocol.data.GateLockKey;
import com.ut.unilink.cloudLock.protocol.data.GateLockOperateRecord;
import com.ut.unilink.cloudLock.protocol.data.LockState;
import com.ut.unilink.util.BitUtil;
import com.ut.unilink.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.Lock;

public class LockActivity extends AppCompatActivity {

    private TextView lockInfo;
    private ImageView lock;
    private UnilinkManager unilinkManager;
    private String address;
    private CloudLock mCloudLock;
    private TextView power;
    private ScanDevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        initUI();
        device = (ScanDevice) getIntent().getParcelableExtra("lock");
        address = device.getAddress();

        String info = "Mac:" + device.getAddress() + "\n" +
                        "厂商标识:" + toUnsignedHexString(device.getVendorId()) + "\n" +
                        "设备类型:" + toUnsignedHexString(device.getDeviceType()) + "\n" +
                        "激活状态:" + (device.isActive() ? "已激活" : "未激活");
        lockInfo.setText(info);

        unilinkManager = UnilinkManager.getInstance(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestPermission();
    }

    private void initUI() {
        lockInfo = findViewById(R.id.lockInfo);
        lock = findViewById(R.id.lock);
        power = findViewById(R.id.power);
    }

    private String toUnsignedHexString(byte[] data) {
        String result = "0x";
        for(int i=0; i<data.length; i++) {
            result += (String.format("%02x", data[i] & 0xFF));
        }

        return result;
    }

    private void requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void saveLock(CloudLock cloudLock) {
        if (cloudLock == null) {
            return;
        }

        LockInfo lockInfo = new LockInfo();
        lockInfo.address = cloudLock.getAddress();
        lockInfo.adminPW = cloudLock.getAdminPassword();
        lockInfo.openLockPW = cloudLock.getOpenLockPassword();
        lockInfo.encrypt = cloudLock.getEncryptType();
        lockInfo.key = cloudLock.getEntryptKey();
        lockInfo.deviceId = cloudLock.getDeviceId();

        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("/sdcard/lockInfo.txt"));
            outputStream.writeObject(lockInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private CloudLock readLock() {
        try {
            FileInputStream fileInputStream  = new FileInputStream("/sdcard/lockInfo.txt");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            LockInfo lockInfo = (LockInfo) objectInputStream.readObject();

            if (!address.equals(lockInfo.address)) {
                clear();
                return null;
            }

            CloudLock cloudLock = new CloudLock(lockInfo.address);
            cloudLock.setAdminPassword(lockInfo.adminPW);
            cloudLock.setOpenLockPassword(lockInfo.openLockPW);
            cloudLock.setEncryptType(lockInfo.encrypt);
            cloudLock.setEntryptKey(lockInfo.key);
            cloudLock.setActive(true);
            cloudLock.setDeviceId(lockInfo.deviceId);
            return cloudLock;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void clear() {
        File file = new File("/sdcard/lockInfo.txt");
        if (file.exists()) {
            file.delete();
        }
        mCloudLock = null;
    }

    private void inputLockInfo() {
        CloudLock cloudLock = readLock();

        SetLockDialogFragment lockDialogFragment = SetLockDialogFragment.getInstance(cloudLock);
        lockDialogFragment.setLockEditListener(new SetLockDialogFragment.LockEditListener() {
            @Override
            public void onEditLock(CloudLock cloudLock) {
                mCloudLock = cloudLock;
                mCloudLock.setAddress(address);
                saveLock(mCloudLock);
            }
        });
        lockDialogFragment.show(getSupportFragmentManager(), "lockInfo");
    }

    private ConnectListener connectListener = new ConnectListener() {
        @Override
        public void onConnect() {
            LogInFile.write("connect success time:" + TimeRecord.end("connect") + "ms");
            lock.setImageResource(R.drawable.lock_connect);
//            openGateLock();
        }

        @Override
        public void onDisconnect(int code, String message) {
            Log.i("onDisconnect--------");
            lock.setImageResource(R.drawable.lock_disconnect);
        }
    };

    private LockStateListener lockStateListener = new LockStateListener() {
        @Override
        public void onState(final LockState state) {
            power.setText("电量:" + state.getElect());

        }
    };

    private void openGateLock() {
        if (mCloudLock == null) {
            mCloudLock = readLock();
        }

        if (mCloudLock == null) {
            showMsg("获取锁信息失败，请先初始化锁");
            return;
        }

        unilinkManager.openGateLock(mCloudLock.getAddress(), mCloudLock.getEncryptType(), mCloudLock.getEntryptKeyString(),
                mCloudLock.getOpenLockPassword(), new CallBack2<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        showMsg("门锁打开成功");
                    }

                    @Override
                    public void onFailed(int errCode, String errMsg) {
                        showMsg("门锁打开失败：" + errMsg);
                    }
                });
    }

    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.lock:
                inputLockInfo();
                break;

            case R.id.connect:
            case R.id.connect2:
                LogInFile.write("\nstart connect...");
                TimeRecord.start("connect");

                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    unilinkManager.connect(device, connectListener);
                }
                else {
                    unilinkManager.connect(device, mCloudLock.getEncryptType(), mCloudLock.getEntryptKeyString(),
                            connectListener, lockStateListener);
                }
                break;

            case R.id.disconnect:
                unilinkManager.close(address);
                power.setText("");
                break;

            case R.id.init:
                LogInFile.write("\nstart init...");
                TimeRecord.start("init");

                unilinkManager.initLock(device, new CallBack() {
                    @Override
                    public void onSuccess(final CloudLock cloudLock) {
                        LogInFile.write("init success time:" + TimeRecord.end("init") + "ms");

                        mCloudLock = cloudLock;
                        Log.i("initLock success");
                        showMsg("initLock success");
                        lockInfo.post(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("openLock pw:" + Log.toUnsignedHex(cloudLock.getOpenLockPassword(), " "));
                                System.out.println("admin pw:" + Log.toUnsignedHex(cloudLock.getAdminPassword(), " "));
                                System.out.println("encrpt:" + (cloudLock.getEncryptType() == 0 ? "TEA" : "AES"));
                                System.out.println("key:" + Log.toUnsignedHex(cloudLock.getEntryptKey()));
//                                lockInfo.append("\nopenLock pw:" + Log.toUnsignedHex(cloudLock.getOpenLockPassword(), " ") + "\n");
//                                lockInfo.append("admin pw:" + Log.toUnsignedHex(cloudLock.getAdminPassword(), " ") + "\n");
//                                lockInfo.append("encrpt:" + (cloudLock.getEncryptType() == 0 ? "TEA" : "AES"));
                            }
                        });
                    }

                    @Override
                    public void onFailed(int errCode, String errMsg) {
                        Log.i("initLock failed: " + errMsg);
                        showMsg("initLock failed: " + errMsg);
                    }
                });
                break;

            case R.id.confirmInit:
                if (mCloudLock == null) {
                    showMsg("请先初始化");
                    return;
                }

                if (mCloudLock.getAdminPassword() == null || mCloudLock.getAdminPassword().length != 6) {
                    showMsg("管理员密码错误");
                    return;
                }

                LogInFile.write("\nstart confirmInit...");
                TimeRecord.start("confirmInit");

                unilinkManager.confirmInit(mCloudLock, new CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        LogInFile.write("confirmInit success time:" + TimeRecord.end("confirmInit") + "ms");
                        showMsg("confirm lock success");

                        saveLock(cloudLock);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String text = lockInfo.getText().toString().replace("未激活", "已激活");
                                lockInfo.setText(text);
                            }
                        });
                    }

                    @Override
                    public void onFailed(int errCode, String errMsg) {
                        showMsg("confirm lock failed:" + errMsg);
                    }
                });
                break;

            case R.id.open:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                if (mCloudLock.getOpenLockPassword() == null || mCloudLock.getOpenLockPassword().length != 6) {
                    showMsg("开锁密码错误");
                    return;
                }

                System.out.println("openPW:" + Log.toUnsignedHex(mCloudLock.getOpenLockPassword()) +
                        " key:" + Log.toUnsignedHex(mCloudLock.getEntryptKey()) +
                        " encrypt:" + mCloudLock.getEncryptType());

                LogInFile.write("\nstart openLock...");
                TimeRecord.start("openLock");

                unilinkManager.openLock(mCloudLock, new CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        LogInFile.write("openLock success time:" + TimeRecord.end("openLock") + "ms");
                        showMsg("openLock success");
                    }

                    @Override
                    public void onFailed(int errCode, String errMsg) {
                        showMsg("openLock failed: " + errMsg);
                    }
                });
                break;

            case R.id.clockwise:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                if (mCloudLock.getOpenLockPassword() == null || mCloudLock.getOpenLockPassword().length != 6) {
                    showMsg("开锁密码错误");
                    return;
                }

                System.out.println("openPW:" + Log.toUnsignedHex(mCloudLock.getOpenLockPassword()) +
                        " key:" + Log.toUnsignedHex(mCloudLock.getEntryptKey()) +
                        " encrypt:" + mCloudLock.getEncryptType());

                LogInFile.write("\nstart setMotorForward...");
                TimeRecord.start("setMotorForward");

                unilinkManager.setMotorForward(mCloudLock, new CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        LogInFile.write("setMotorForward success time:" + TimeRecord.end("setMotorForward") + "ms");
                        showMsg("setMotorForward success");
                    }

                    @Override
                    public void onFailed(int errCode, String errMsg) {
                        showMsg("setMotorForward failed: " + errMsg);
                    }
                });
                break;

            case R.id.anti_cloockwise:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                if (mCloudLock.getOpenLockPassword() == null || mCloudLock.getOpenLockPassword().length != 6) {
                    showMsg("开锁密码错误");
                    return;
                }

                System.out.println("openPW:" + Log.toUnsignedHex(mCloudLock.getOpenLockPassword()) +
                        " key:" + Log.toUnsignedHex(mCloudLock.getEntryptKey()) +
                        " encrypt:" + mCloudLock.getEncryptType());

                LogInFile.write("\nstart setMotorReverse...");
                TimeRecord.start("setMotorReverse");

                unilinkManager.setMotorReverse(mCloudLock, new CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        LogInFile.write("setMotorReverse success time:" + TimeRecord.end("setMotorReverse") + "ms");
                        showMsg("setMotorReverse success");
                    }

                    @Override
                    public void onFailed(int errCode, String errMsg) {
                        showMsg("setMotorReverse failed: " + errMsg);
                    }
                });
                break;

            case R.id.reset:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                if (mCloudLock.getAdminPassword() == null || mCloudLock.getAdminPassword().length != 6) {
                    showMsg("管理员密码错误");
                    return;
                }

                LogInFile.write("\nstart resetLock...");
                TimeRecord.start("resetLock");

                unilinkManager.resetLock(mCloudLock, new CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        unilinkManager.close(address);
                        LogInFile.write("resetLock success time:" + TimeRecord.end("resetLock") + "ms");
                        showMsg("resetLock success");
                        clear();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String text = lockInfo.getText().toString().replace("已激活", "未激活");
                                lockInfo.setText(text);
                            }
                        });
                    }

                    @Override
                    public void onFailed(int errCode, String errMsg) {
                        showMsg("resetLock failed: " + errMsg);
                    }
                });
                break;

            case R.id.readCode:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                LogInFile.write("\nstart readAutoIncreaseNum...");
                TimeRecord.start("readAutoIncreaseNum");

                unilinkManager.getAutoIncreaseNum(mCloudLock, new CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        LogInFile.write("readAutoIncreaseNum success time:" + TimeRecord.end("readAutoIncreaseNum") + "ms");
                        showMsg("AutoIncreaseNum:" + cloudLock.getAutuIncreaseNum());
                    }

                    @Override
                    public void onFailed(int errCode, String errMsg) {
                        showMsg("getAutoIncreaseNum failed: " + errMsg);
                    }
                });
                break;

            case R.id.superPW:
                if (!unilinkManager.isConnect(address)) {
                    showMsg("设备未连接");
                    return;
                }
                unilinkManager.sendSuperPassword(address);
                clear();
                break;

            case R.id.writeSerialNum:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                byte[] serialNum = new byte[6];
                Random random = new Random();
                random.nextBytes(serialNum);
                if (mCloudLock != null) {
                    mCloudLock.setSerialNum(serialNum);
                }
                showMsg("随机生成生产序列号:" + Log.toUnsignedHex(serialNum, ""));

                LogInFile.write("\nstart writeSerialNum...");
                TimeRecord.start("writeSerialNum");

                unilinkManager.setSerialNum(mCloudLock, new CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        LogInFile.write("writeSerialNum success time:" + TimeRecord.end("writeSerialNum") + "ms");
                        showMsg("setSerialNum success");
                    }

                    @Override
                    public void onFailed(int errCode, String errMsg) {
                        showMsg("setSerialNum failed:" + errMsg);
                    }
                });
                break;

            case R.id.readSerialNum:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                LogInFile.write("\nstart readSerialNum...");
                TimeRecord.start("readSerialNum");

                unilinkManager.getSerialNum(mCloudLock, new CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        LogInFile.write("readSerialNum success time:" + TimeRecord.end("readSerialNum") + "ms");
                        showMsg("getSerialNum:" + Log.toUnsignedHex(cloudLock.getSerialNum(), ""));
                    }

                    @Override
                    public void onFailed(int errCode, String errMsg) {
                        showMsg("getSerialNum:" + errMsg);
                    }
                });
                break;

            case R.id.writeVendorId:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                byte[] vendorId = new byte[4];
                final byte[] deviceType = new byte[2];
                Random random1 = new Random();
                random1.nextBytes(vendorId);
                random1.nextBytes(deviceType);

                if (mCloudLock != null) {
                    mCloudLock.setVendorId(vendorId);
                    mCloudLock.setDeviceType(deviceType);
                }

                showMsg("随机生成厂商标识和设备类型 " + "vendorId:" + Log.toUnsignedHex(vendorId, "")
                 + " deviceType:" + Log.toUnsignedHex(deviceType, ""));

                LogInFile.write("\nstart writeVendorId...");
                TimeRecord.start("writeVendorId");

                unilinkManager.setVendorId(mCloudLock, new CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        LogInFile.write("writeVendorId success time:" + TimeRecord.end("writeVendorId") + "ms");
                        showMsg("setVendorId success");
                    }

                    @Override
                    public void onFailed(int errCode, String errMsg) {
                        showMsg("setVendorId failed:" + errMsg);
                    }
                });
                break;

            case R.id.readVendorId:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                LogInFile.write("\nstart readVendorId...");
                TimeRecord.start("readVendorId");

                unilinkManager.getVendorId(mCloudLock, new CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        LogInFile.write("readVendorId success time:" + TimeRecord.end("readVendorId") + "ms");
                        showMsg("getVendorId " + "vendorId:" + Log.toUnsignedHex(cloudLock.getVendorId(), "")
                                + " deviceType:" + Log.toUnsignedHex(cloudLock.getDeviceType(), ""));
                    }

                    @Override
                    public void onFailed(int errCode, String errMsg) {
                        showMsg("getVendorId failed:" + errMsg);
                    }
                });
                break;

            case R.id.readInfo:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                LogInFile.write("\nstart readDeviceNodeInfo...");
                TimeRecord.start("readDeviceNodeInfo");

                unilinkManager.getDeviceInfo(mCloudLock, new CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        LogInFile.write("readDeviceNodeInfo success time:" + TimeRecord.end("readDeviceNodeInfo") + "ms");
                        showMsg("getDeviceInfo:" + Log.toUnsignedHex(cloudLock.getDeviceInfo(mCloudLock.getDeviceNum()), ""));
                    }

                    @Override
                    public void onFailed(int errCode, String errMsg) {
                        showMsg("getDeviceInfo failed:" + errMsg);
                    }
                });
                break;

            case R.id.readMutilInfo:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                LogInFile.write("\nstart readMultiDeviceNodeInfo...");
                TimeRecord.start("readMultiDeviceNodeInfo");

                unilinkManager.getDeviceInfos(mCloudLock, new CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {

                        LogInFile.write("readMultiDeviceNodeInfo success time:" + TimeRecord.end("readMultiDeviceNodeInfo") + "ms");

                        String str = "";
                        Set<Map.Entry<Byte, byte[]>> set = cloudLock.getDeviceInfoMap().entrySet();
                        for (Map.Entry<Byte, byte[]> entry : set) {
                            str += "deviceNum:" + entry.getKey() + " value:" + Log.toUnsignedHex(entry.getValue()) + "\n";
                        }
                        showMsg(str);
                    }

                    @Override
                    public void onFailed(int errCode, String errMsg) {
                        showMsg("getDeviceInfos failed:" + errMsg);
                    }
                });
                break;

            case R.id.getProductInfo:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                LogInFile.write("\nstart getProductInfo...");
                TimeRecord.start("getProductInfo");

                unilinkManager.getProductInfo(mCloudLock, new CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        LogInFile.write("getProductInfo success time:" + TimeRecord.end("getProductInfo") + "ms");
                        showMsg("productInfo:" + cloudLock.getProductInfo());
                    }

                    @Override
                    public void onFailed(int errCode, String errMsg) {
                        showMsg("getProductInfo failed:" + errMsg);
                    }
                });
                break;

            case R.id.readTime:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                unilinkManager.readTime(mCloudLock.getAddress(), mCloudLock.getEncryptType(), mCloudLock.getEntryptKeyString(),
                        new CallBack2<Long>() {
                            @Override
                            public void onSuccess(Long data) {
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                showMsg("readTime:\n" + simpleDateFormat.format(new Date(data)));
                            }

                            @Override
                            public void onFailed(int errCode, String errMsg) {
                                showMsg("readTime failed:" + errMsg);
                            }
                        });
                break;

            case R.id.writeTime:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                unilinkManager.writeTime(mCloudLock.getAddress(), mCloudLock.getEncryptType(), mCloudLock.getEntryptKeyString(),
                        new Date().getTime(), new CallBack2<Void>() {
                            @Override
                            public void onSuccess(Void data) {
                                showMsg("writeTime success");
                            }

                            @Override
                            public void onFailed(int errCode, String errMsg) {
                                showMsg("writeTime failed:" + errMsg);
                            }
                        });
                break;

            case R.id.readKeys:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                unilinkManager.readKeyInfos(mCloudLock.getAddress(), mCloudLock.getEncryptType(), mCloudLock.getEntryptKeyString(),
                        new CallBack2<List<GateLockKey>>() {
                            @Override
                            public void onSuccess(List<GateLockKey> data) {

                                gateLockKeys = data;

                                String str = "";
                                for (GateLockKey key : data) {
                                    str += key.toString() + "\n\n";
                                }
                                MsgDialogFragment.show(LockActivity.this, "读取钥匙配置表成功", str);
                            }

                            @Override
                            public void onFailed(int errCode, String errMsg) {
                                showMsg("readKeyInfos failed " + errMsg);
                            }
                        });
                break;

            case R.id.writeKeys:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                if (gateLockKeys == null) {
                    showMsg("请先读取钥匙配置表");
                    return;
                }

                for (GateLockKey key : gateLockKeys) {
                    key.setAuthState(!key.isAuthKey());
                    key.setFreezeState(!key.isFreeze());
                    key.setNameMarkState(!key.isNameMark());
                }

                unilinkManager.writeKeyInfos(mCloudLock.getAddress(), mCloudLock.getEncryptType(), mCloudLock.getEntryptKeyString(),
                        gateLockKeys, new CallBack2<Void>() {
                            @Override
                            public void onSuccess(Void data) {
                                String str = "";
                                for (GateLockKey key : gateLockKeys) {
                                    str += key + "\n\n";
                                }
                                MsgDialogFragment.show(LockActivity.this, "修改钥匙配置表成功", str);
                            }

                            @Override
                            public void onFailed(int errCode, String errMsg) {
                                showMsg("writeKeyInfos failed " + errMsg);
                            }
                        });
                break;

            case R.id.readCloudLockOpenRecord:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                unilinkManager.readCLoudLockOpenLockRecord(mCloudLock.getAddress(), mCloudLock.getEncryptType(), mCloudLock.getEntryptKeyString(),
                        1, new CallBack2<List<CloudLockOperateRecord>>() {
                            @Override
                            public void onSuccess(List<CloudLockOperateRecord> data) {
                                String str = "";
                                for (CloudLockOperateRecord record : data) {
                                    str += record.toString() + "\n\n";
                                }
                                MsgDialogFragment.show(LockActivity.this, "读取开锁记录成功", str);
                            }

                            @Override
                            public void onFailed(int errCode, String errMsg) {
                                showMsg("read openLockRecord failed " + errMsg);
                            }
                        });
                break;

            case R.id.readGateLockOpenRecord:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                unilinkManager.readGateLockOpenLockRecord(mCloudLock.getAddress(), mCloudLock.getEncryptType(), mCloudLock.getEntryptKeyString(),
                        1, new CallBack2<List<GateLockOperateRecord>>() {
                            @Override
                            public void onSuccess(List<GateLockOperateRecord> data) {
                                String str = "";
                                for (GateLockOperateRecord record : data) {
                                    str += record.toString() + "\n\n";
                                }
                                MsgDialogFragment.show(LockActivity.this, "读取开锁记录成功", str);
                            }

                            @Override
                            public void onFailed(int errCode, String errMsg) {
                                showMsg("read openLockRecord failed " + errMsg);
                            }
                        });
                break;

            case R.id.readAuthCount:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                unilinkManager.readAuthCountInfo(mCloudLock.getAddress(), mCloudLock.getEncryptType(), mCloudLock.getEntryptKeyString(),
                        new CallBack2<List<AuthCountInfo>>() {
                            @Override
                            public void onSuccess(List<AuthCountInfo> data) {
                                String str = "";
                                for (AuthCountInfo authCountInfo : data) {
                                    str += authCountInfo.toString() + "\n\n";
                                }
                                MsgDialogFragment.show(LockActivity.this, "读取授权次数成功", str);
                            }

                            @Override
                            public void onFailed(int errCode, String errMsg) {
                                showMsg("readAuthCountInfo failed " + errMsg);
                            }
                        });
                break;

            case R.id.delKey:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                if (gateLockKeys == null) {
                    showMsg("请先读取钥匙配置表");
                    return;
                }

                if (gateLockKeys.size() <= 0) {
                    showMsg("没有可删除的钥匙，请先读取钥匙配置表");
                    return;
                }

                final int delKeyID = gateLockKeys.get(gateLockKeys.size() - 1).getKeyId();
                unilinkManager.deleteKey(mCloudLock.getAddress(), mCloudLock.getEncryptType(), mCloudLock.getEntryptKeyString(),
                        delKeyID, new CallBack2<Void>() {
                            @Override
                            public void onSuccess(Void data) {
                                MsgDialogFragment.show(LockActivity.this, "删除钥匙成功", "删除钥匙ID:" + delKeyID);
                                gateLockKeys.remove(gateLockKeys.size() - 1);
                            }

                            @Override
                            public void onFailed(int errCode, String errMsg) {
                                showMsg("deleteKey failed " + errMsg);
                            }
                        });
                break;

            case R.id.batchUpdateAuth:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                if (authInfos == null) {
                    showMsg("请先获取授权表信息");
                    return;
                }

                Random random2 = new Random();
                for (AuthInfo authInfo : authInfos) {
                    byte validWeekDay = (byte) BitUtil.set1(random2.nextInt(256), 7);
                    authInfo.setValidWeekDay(validWeekDay);
                    authInfo.setOpenLockCount((byte) (authInfo.getOpenLockCount() + 1));
                    authInfo.setStartTime(new Date().getTime());
                    authInfo.setEndTime(new Date().getTime());
                }

                unilinkManager.batchUpdateAuthInfos(mCloudLock.getAddress(), mCloudLock.getEncryptType(), mCloudLock.getEntryptKeyString(),
                        authInfos, new CallBack2<Void>() {
                            @Override
                            public void onSuccess(Void data) {
                                String str = "";
                                for (AuthInfo authInfo : authInfos) {
                                    str += authInfo + "\n\n";
                                }
                                MsgDialogFragment.show(LockActivity.this, "批量修改授权成功", str);
                            }

                            @Override
                            public void onFailed(int errCode, String errMsg) {
                                showMsg("batchUpdateAuthInfos failed " + errMsg);
                            }
                        });
                break;

            case R.id.addAuth:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                if (gateLockKeys == null || gateLockKeys.size() <= 0) {
                    showMsg("没有可用钥匙，请先读取钥匙配置表");
                    return;
                }

                Random random3 = new Random();
                final AuthInfo authInfo = new AuthInfo();
                authInfo.setKeyId(gateLockKeys.get(0).getKeyId());
                byte validWeekDay = (byte) BitUtil.set1(random3.nextInt(256), 7);
                authInfo.setValidWeekDay(validWeekDay);
                authInfo.setOpenLockCount((byte) (authInfo.getOpenLockCount() + 1));
                authInfo.setStartTime(new Date().getTime());
                authInfo.setEndTime(new Date().getTime());

                unilinkManager.addAuth(mCloudLock.getAddress(), mCloudLock.getEncryptType(), mCloudLock.getEntryptKeyString(), authInfo,
                        new CallBack2<Integer>() {
                            @Override
                            public void onSuccess(Integer data) {
                                authInfo.setAuthId(data.byteValue());
                                if (authInfos == null) {
                                    authInfos = new ArrayList<>();
                                }
                                authInfos.add(authInfo);
                                MsgDialogFragment.show(LockActivity.this, "添加授权成功", authInfo.toString());
                            }

                            @Override
                            public void onFailed(int errCode, String errMsg) {
                                showMsg("addAuth failed " + errMsg);
                            }
                        });
                break;

            case R.id.updateAuth:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                if (authInfos == null) {
                    showMsg("请先读取授权配置表");
                    return;
                }

                if (authInfos.size() <= 0) {
                    showMsg("没有可修改的授权信息，请先读取授权配置表");
                    return;
                }

                Random random4 = new Random();
                final AuthInfo authInfo1 = authInfos.get(0);
                authInfo1.setValidWeekDay((byte) BitUtil.set1(random4.nextInt(256), 7));
                authInfo1.setOpenLockCount((byte) (authInfo1.getOpenLockCount() + 1));
                authInfo1.setStartTime(new Date().getTime());
                authInfo1.setEndTime(new Date().getTime());

                unilinkManager.updateAuth(mCloudLock.getAddress(), mCloudLock.getEncryptType(), mCloudLock.getEntryptKeyString(), authInfo1,
                        new CallBack2<Void>() {
                            @Override
                            public void onSuccess(Void data) {
                                MsgDialogFragment.show(LockActivity.this, "修改授权成功", authInfo1.toString());
                            }

                            @Override
                            public void onFailed(int errCode, String errMsg) {
                                showMsg("updateAuth failed " + errMsg);
                            }
                        });
                break;

            case R.id.delAuth:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                if (authInfos == null || authInfos.size() <= 0) {
                    showMsg("没有可删除的授权，请先读取授权授权配置表或添加授权");
                    return;
                }

                final int delAuthID = authInfos.get(0).getAuthId();
                unilinkManager.deleteAuth(mCloudLock.getAddress(), mCloudLock.getEncryptType(), mCloudLock.getEntryptKeyString(),
                        delAuthID, new CallBack2<Void>() {
                            @Override
                            public void onSuccess(Void data) {
                                MsgDialogFragment.show(LockActivity.this, "删除授权成功", "删除授权ID:" + delAuthID);
                                authInfos.remove(0);
                            }

                            @Override
                            public void onFailed(int errCode, String errMsg) {
                                showMsg("deleteAuth failed "+ errMsg);
                            }
                        });
                break;

            case R.id.queryAuth:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                unilinkManager.queryAllAuth(mCloudLock.getAddress(), mCloudLock.getEncryptType(), mCloudLock.getEntryptKeyString(),
                        new CallBack2<List<AuthInfo>>() {
                            @Override
                            public void onSuccess(List<AuthInfo> data) {
                                authInfos = data;
                                String str = "";
                                for (AuthInfo authInfo2 : data) {
                                    str += authInfo2.toString() + "\n\n";
                                }
                                MsgDialogFragment.show(LockActivity.this, "查询授权成功", str);
                            }

                            @Override
                            public void onFailed(int errCode, String errMsg) {
                                showMsg("queryAuth failed " + errMsg);
                            }
                        });
                break;

            case R.id.openCloudLock:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("获取锁信息失败，请先初始化锁");
                    return;
                }

                unilinkManager.openCloudLock(mCloudLock.getAddress(), mCloudLock.getEncryptType(), mCloudLock.getEntryptKeyString(),
                        mCloudLock.getOpenLockPassword(), new CallBack2<Void>() {
                            @Override
                            public void onSuccess(Void data) {
                                showMsg("云锁打开成功");
                            }

                            @Override
                            public void onFailed(int errCode, String errMsg) {
                                showMsg("云锁打开失败:" + errMsg);
                            }
                        });
                break;

            case R.id.openGateLock:
                openGateLock();
                break;

                default:

        }
    }

    private List<AuthInfo> authInfos;
    private List<GateLockKey> gateLockKeys;

    private void showMsg(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LockActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
