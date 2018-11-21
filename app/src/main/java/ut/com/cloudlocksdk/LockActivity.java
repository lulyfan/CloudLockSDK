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
import com.ut.unilink.cloudLock.CloudLock;
import com.ut.unilink.cloudLock.ConnectListener;
import com.ut.unilink.cloudLock.LockStateListener;
import com.ut.unilink.cloudLock.ScanDevice;
import com.ut.unilink.cloudLock.protocol.data.LockState;
import com.ut.unilink.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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

            CloudLock cloudLock = new CloudLock(lockInfo.address);
            cloudLock.setAdminPassword(lockInfo.adminPW);
            cloudLock.setOpenLockPassword(lockInfo.openLockPW);
            cloudLock.setEncryptType(lockInfo.encrypt);
            cloudLock.setEntryptKey(lockInfo.key);
            cloudLock.setActive(true);
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

    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.connect:
                unilinkManager.connect(address, new ConnectListener() {
                    @Override
                    public void onConnect() {
                        lock.setImageResource(R.drawable.lock_connect);
                    }

                    @Override
                    public void onDisconnect(int code, String message) {
                        lock.setImageResource(R.drawable.lock_disconnect);
                    }
                }, new LockStateListener() {
                    @Override
                    public void onState(final LockState state) {
                        power.setText("电量:" + state.getElect());

                    }
                });
                break;

            case R.id.init:
                unilinkManager.initLock(device, new CallBack() {
                    @Override
                    public void onSuccess(final CloudLock cloudLock) {
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

                unilinkManager.confirmInit(mCloudLock, new CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        showMsg("confirm lock success");

                        saveLock(cloudLock);
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
                    showMsg("未初始化锁");
                    return;
                }

                System.out.println("openPW:" + Log.toUnsignedHex(mCloudLock.getOpenLockPassword()) +
                        " key:" + Log.toUnsignedHex(mCloudLock.getEntryptKey()) +
                        " encrypt:" + mCloudLock.getEncryptType());
                unilinkManager.openLock(mCloudLock, new CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        showMsg("openLock success");
                    }

                    @Override
                    public void onFailed(int errCode, String errMsg) {
                        showMsg("openLock failed: " + errMsg);
                    }
                });
                break;

            case R.id.reset:
                if (mCloudLock == null) {
                    mCloudLock = readLock();
                }

                if (mCloudLock == null) {
                    showMsg("未初始化锁");
                    return;
                }

                unilinkManager.resetLock(mCloudLock, new CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        showMsg("resetLock success");
                        clear();
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
                    showMsg("未初始化锁");
                    return;
                }

                unilinkManager.getAutoIncreaseNum(mCloudLock, new CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
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
                    showMsg("未初始化锁");
                    return;
                }

                byte[] serialNum = new byte[6];
                Random random = new Random();
                random.nextBytes(serialNum);
                if (mCloudLock != null) {
                    mCloudLock.setSerialNum(serialNum);
                }
                showMsg("随机生成生产序列号:" + Log.toUnsignedHex(serialNum, ""));

                unilinkManager.setSerialNum(mCloudLock, new CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
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
                    showMsg("未初始化锁");
                    return;
                }

                unilinkManager.getSerialNum(mCloudLock, new CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
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
                    showMsg("未初始化锁");
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

                unilinkManager.setVendorId(mCloudLock, new CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
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
                    showMsg("未初始化锁");
                    return;
                }

                unilinkManager.getVendorId(mCloudLock, new CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
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
                    showMsg("未初始化锁");
                    return;
                }

                unilinkManager.getDeviceInfo(mCloudLock, new CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
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
                    showMsg("未初始化锁");
                    return;
                }

                unilinkManager.getDeviceInfos(mCloudLock, new CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {

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
                    showMsg("未初始化锁");
                    return;
                }

                unilinkManager.getProductInfo(mCloudLock, new CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        showMsg("productInfo:" + cloudLock.getProductInfo());
                    }

                    @Override
                    public void onFailed(int errCode, String errMsg) {
                        showMsg("getProductInfo failed:" + errMsg);
                    }
                });

                default:
        }
    }

    private void showMsg(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LockActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
