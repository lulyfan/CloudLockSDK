package ut.com.cloudlocksdk;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.ut.unilink.cloudLock.CloudLock;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class SetLockDialogFragment extends DialogFragment {

    private EditText et_adminPW;
    private EditText et_openLockPW;
    private EditText et_encryptKey;
    private EditText et_encryptType;

    public static SetLockDialogFragment getInstance(CloudLock cloudLock) {
        SetLockDialogFragment fragment = new SetLockDialogFragment();
        if (cloudLock != null) {
            Bundle bundle = new Bundle();
            bundle.putString("adminPW", encode(cloudLock.getAdminPassword()));
            bundle.putString("openLockPW", encode(cloudLock.getOpenLockPassword()));
            bundle.putString("encryptKey", encode(cloudLock.getEntryptKey()));
            bundle.putString("encryptType", cloudLock.getEncryptType() + "");
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.setting_lock, null);
        et_adminPW = view.findViewById(R.id.adminPW);
        et_encryptKey = view.findViewById(R.id.encryptKey);
        et_encryptType = view.findViewById(R.id.encryptType);
        et_openLockPW = view.findViewById(R.id.openLockPW);

        Bundle bundle = getArguments();
        if (bundle != null) {
            et_adminPW.setText(bundle.getString("adminPW"));
            et_openLockPW.setText(bundle.getString("openLockPW"));
            et_encryptKey.setText(bundle.getString("encryptKey"));
            et_encryptType.setText(bundle.getString("encryptType"));
        }

        builder.setView(view);
        builder.setTitle("配置锁信息");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                byte[] adminPW = parseData(et_adminPW.getText().toString());
                byte[] openLockPW = parseData(et_openLockPW.getText().toString());
                byte[] encryptKey = parseData(et_encryptKey.getText().toString());
                int encryptType = 0;
                try {
                    encryptType = Integer.parseInt(et_encryptType.getText().toString());
                } catch (NumberFormatException e) {

                }

                CloudLock cloudLock = new CloudLock("");
                cloudLock.setAdminPassword(adminPW);
                cloudLock.setOpenLockPassword(openLockPW);
                cloudLock.setEntryptKey(encryptKey);
                cloudLock.setEncryptType(encryptType);
                cloudLock.setActive(true);

                if (lockEditListener != null) {
                    lockEditListener.onEditLock(cloudLock);
                }
            }
        });
        return builder.create();
    }

    private byte[] parseData(String data) {

        if ("".equals(data.trim())) {
            return null;
        }

        int count = data.length() / 2;
        ByteBuffer buffer = ByteBuffer.allocate(count);
        for (int i=0, j=0; j<count; i+=2, j++) {
            String item = data.substring(i, i+2);
            int temp = Integer.parseInt(item,16);
            buffer.put((byte) temp);
        }
        return buffer.array();
    }

    private static String encode(byte[] data) {
        if (data == null) {
            return "";
        }

        String result = "";
        for (int i=0; i<data.length; i++) {
            result += String.format("%02x", data[i] & 0xFF);
        }

        return result;
    }

    public void setLockEditListener(LockEditListener lockEditListener) {
        this.lockEditListener = lockEditListener;
    }

    private LockEditListener lockEditListener;

    public interface LockEditListener {
        void onEditLock(CloudLock cloudLock);
    }


}
