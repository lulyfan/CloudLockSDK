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
import com.ut.unilink.cloudLock.UTBleDevice;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private EditText et_vendorId;
    private EditText et_deviceType;
    private DeviceAdapter deviceAdapter;
    private static final int REQUEST_BLUETOOTH = 1;
    private static final int REQUEST_PERMISSION = 2;
    private UnilinkManager unilinkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        unilinkManager = UnilinkManager.getINSTANCE(this);
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
                UTBleDevice bleDevice = deviceAdapter.getUTBleDevices().get(position);

                Intent intent = new Intent(MainActivity.this, LockActivity.class);
                intent.putExtra("lock", bleDevice);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(deviceAdapter);
    }

    public void onClick(View view) {
        String sVendorId = et_vendorId.getText().toString();
        String sDeviceType = et_deviceType.getText().toString();

        byte[] vendorId = null;
        byte[] deviceType = null;

        if (!sVendorId.equals("")) {
            vendorId = sVendorId.getBytes();
        }

        if (!sDeviceType.equals("")) {
            deviceType = sDeviceType.getBytes();
        }

        int result = unilinkManager.scan(new ScanListener() {
            @Override
            public void onScan(List<UTBleDevice> bleDevice) {
                deviceAdapter.setUTBleDevices(bleDevice);
                deviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFinish() {
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
