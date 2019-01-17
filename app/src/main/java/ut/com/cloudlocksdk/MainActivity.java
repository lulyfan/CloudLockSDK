package ut.com.cloudlocksdk;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ut.unilink.UnilinkManager;
import com.ut.unilink.cloudLock.ScanListener;
import com.ut.unilink.cloudLock.ScanDevice;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private EditText et_vendorId;
    private EditText et_deviceType;
    private DeviceAdapter deviceAdapter;
    private static final int REQUEST_BLUETOOTH = 1;
    private static final int REQUEST_PERMISSION = 2;
    private UnilinkManager unilinkManager;
    private List<ScanDevice> scanDevices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        unilinkManager = UnilinkManager.getInstance(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        unilinkManager.enableBluetooth(this, REQUEST_BLUETOOTH);
        unilinkManager.requestPermission(this, REQUEST_PERMISSION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void initUI() {
        progressBar = findViewById(R.id.progressBar);
        et_vendorId = findViewById(R.id.vendorId);
        et_deviceType = findViewById(R.id.deviceType);

        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        deviceAdapter = new DeviceAdapter(this);
        deviceAdapter.setItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = recyclerView.getChildAdapterPosition(v);
                ScanDevice bleDevice = deviceAdapter.getScanDevices().get(position);

                Intent intent = new Intent(MainActivity.this, LockActivity.class);
                intent.putExtra("lock", bleDevice);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(deviceAdapter);
    }

    public void onClick(View view) {

        if (view.getId() == R.id.stopScan) {
            UnilinkManager.getInstance(this).stopScan();
            return;
        }

        String sVendorId = et_vendorId.getText().toString();
        String sDeviceType = et_deviceType.getText().toString();

        byte[] vendorId = null;
        byte[] deviceType = null;

        if (!sVendorId.equals("")) {
            int temp = (int) Long.parseLong(sVendorId, 16);
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putInt(temp);
            vendorId = buffer.array();
        }

        if (!sDeviceType.equals("")) {
            short temp = (short) Integer.parseInt(sDeviceType, 16);
            ByteBuffer buffer = ByteBuffer.allocate(2);
            buffer.putShort(temp);
            deviceType = buffer.array();
        }

        deviceAdapter.setScanDevices(null);
        deviceAdapter.notifyDataSetChanged();
        scanDevices.clear();
        int result = unilinkManager.scan(new ScanListener() {
            @Override
            public void onScan(ScanDevice scanDevice) {
                scanDevices.add(scanDevice);
                deviceAdapter.setScanDevices(scanDevices);
                deviceAdapter.notifyDataSetChanged();

            }

            @Override
            public void onScanTimeout() {
                progressBar.setVisibility(View.GONE);
                showMsg("未发现指定设备");
            }

            @Override
            public void onFinish(List<ScanDevice> scanDevices) {
                progressBar.setVisibility(View.GONE);
            }
        }, 5, vendorId, deviceType);

        switch (result) {
            case 0:
                progressBar.setVisibility(View.VISIBLE);
                break;

            case 10:
                showMsg("请先开启蓝牙");
                break;

            case -1:
                showMsg("蓝牙不支持");
                break;

            case -2:
                showMsg("没有定位权限");
                break;

                default:
        }
    }

    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
