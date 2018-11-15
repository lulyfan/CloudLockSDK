package ut.com.cloudlocksdk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ut.unilink.UnilinkManager;
import com.ut.unilink.cloudLock.CloudLock;
import com.ut.unilink.cloudLock.ConnectListener;
import com.ut.unilink.cloudLock.LockStateListener;
import com.ut.unilink.cloudLock.UTBleDevice;
import com.ut.unilink.cloudLock.Unilink;
import com.ut.unilink.cloudLock.protocol.data.BleLockState;
import com.ut.unilink.util.Log;

import java.util.Random;

public class LockActivity extends AppCompatActivity {

    private TextView lockInfo;
    private ImageView lock;
    private UnilinkManager unilinkManager;
    private String address;
    private CloudLock mCloudLock;
    private TextView power;
    private UTBleDevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        initUI();
        device = (UTBleDevice) getIntent().getParcelableExtra("lock");
        address = device.getAddress();

        String info = "Mac:" + device.getAddress() + "\n" +
                        "厂商标识:" + toUnsignedHexString(device.getVendorId()) + "\n" +
                        "设备类型:" + toUnsignedHexString(device.getDeviceType()) + "\n" +
                        "激活状态:" + (device.isActive() ? "已激活" : "未激活");
        lockInfo.setText(info);

        unilinkManager = UnilinkManager.getINSTANCE(this);
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
                    public void onState(final BleLockState state) {
                        Log.i("lock", "elect:" + state.getElect());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                power.setText("电量:" + state.getElect());
                            }
                        });

                    }
                });
                break;

            case R.id.init:
                unilinkManager.initLock(device, new Unilink.CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        mCloudLock = cloudLock;
                        Log.i("initLock success");
                        showMsg("initLock success");
                    }

                    @Override
                    public void onFailed(int code, String errMsg) {
                        Log.i("initLock failed: " + errMsg);
                        showMsg("initLock failed: " + errMsg);
                    }
                });
                break;

            case R.id.open:
                unilinkManager.openLock(mCloudLock, new Unilink.CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        showMsg("openLock success");
                    }

                    @Override
                    public void onFailed(int code, String errMsg) {
                        showMsg("openLock failed: " + errMsg);
                    }
                });
                break;

            case R.id.reset:
                unilinkManager.resetLock(mCloudLock, new Unilink.CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        showMsg("resetLock success");
                    }

                    @Override
                    public void onFailed(int code, String errMsg) {
                        showMsg("resetLock failed: " + errMsg);
                    }
                });
                break;

            case R.id.readCode:
                unilinkManager.readAutoIncreaseNum(mCloudLock, new Unilink.CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        showMsg("AutoIncreaseNum:" + cloudLock.getAutuIncreaseNum());
                    }

                    @Override
                    public void onFailed(int code, String errMsg) {
                        showMsg("readAutoIncreaseNum failed: " + errMsg);
                    }
                });
                break;

            case R.id.superPW:
                unilinkManager.sendSuperPassword(address);
                break;

            case R.id.writeSerialNum:
                byte[] serialNum = new byte[6];
                Random random = new Random();
                random.nextBytes(serialNum);
                mCloudLock.setSerialNum(serialNum);
                showMsg("随机生成生产序列号:" + Log.toUnsignedHex(serialNum, ""));

                unilinkManager.writeSerialNum(mCloudLock, new Unilink.CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        showMsg("writeSerialNum success");
                    }

                    @Override
                    public void onFailed(int code, String errMsg) {
                        showMsg("writeSerialNum failed:" + errMsg);
                    }
                });
                break;

            case R.id.readSerialNum:
                unilinkManager.readSerialNum(mCloudLock, new Unilink.CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        showMsg("readSerialNum:" + Log.toUnsignedHex(cloudLock.getSerialNum(), ""));
                    }

                    @Override
                    public void onFailed(int code, String errMsg) {
                        showMsg("readSerialNum:" + errMsg);
                    }
                });
                break;

            case R.id.writeVendorId:
                byte[] vendorId = new byte[4];
                final byte[] deviceType = new byte[2];
                Random random1 = new Random();
                random1.nextBytes(vendorId);
                random1.nextBytes(deviceType);

                mCloudLock.setVendorId(vendorId);
                mCloudLock.setDeviceType(deviceType);

                showMsg("随机生成厂商标识和设备类型 " + "vendorId:" + Log.toUnsignedHex(vendorId, "")
                 + " deviceType:" + Log.toUnsignedHex(deviceType, ""));

                unilinkManager.writeVendorId(mCloudLock, new Unilink.CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        showMsg("writeVendorId success");
                    }

                    @Override
                    public void onFailed(int code, String errMsg) {
                        showMsg("writeVendorId failed:" + errMsg);
                    }
                });
                break;

            case R.id.readVendorId:
                unilinkManager.readVendorId(mCloudLock, new Unilink.CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        showMsg("readVendorId " + "vendorId:" + Log.toUnsignedHex(cloudLock.getVendorId(), "")
                                + " deviceType:" + Log.toUnsignedHex(cloudLock.getDeviceType(), ""));
                    }

                    @Override
                    public void onFailed(int code, String errMsg) {
                        showMsg("readVendorId failed:" + errMsg);
                    }
                });
                break;

            case R.id.readInfo:
                unilinkManager.readDeviceInfo(mCloudLock, new Unilink.CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        showMsg("readDeviceInfo:" + Log.toUnsignedHex(cloudLock.getDeviceInfo(mCloudLock.getDeviceNum()), ""));
                    }

                    @Override
                    public void onFailed(int code, String errMsg) {
                        showMsg("readDeviceInfo failed:" + errMsg);
                    }
                });
                break;

            case R.id.readMutilInfo:
                unilinkManager.readMutilDeviceInfo(mCloudLock, new Unilink.CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        showMsg("readMutilDeviceInfo success");
                    }

                    @Override
                    public void onFailed(int code, String errMsg) {
                        showMsg("readMutilDeviceInfo failed:" + errMsg);
                    }
                });
                break;

            case R.id.getProductInfo:
                unilinkManager.getProductInfo(mCloudLock, new Unilink.CallBack() {
                    @Override
                    public void onSuccess(CloudLock cloudLock) {
                        showMsg("productInfo:" + cloudLock.getProductInfo());
                    }

                    @Override
                    public void onFailed(int code, String errMsg) {
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
