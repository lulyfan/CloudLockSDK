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

public class BindCheckDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_bind_check, null);
        final EditText et_bindPassword = view.findViewById(R.id.bindPassword);
        builder.setTitle("绑定认证")
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String password = et_bindPassword.getText().toString();
                        if (!"".equals(password.trim()) && bindCheck != null) {
                            bindCheck.onBindCheck(password);
                        }
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    private IBindCheck bindCheck;

    public void setBindCheckListener(IBindCheck bindCheck) {
        this.bindCheck = bindCheck;
    }

    public interface IBindCheck {
        void onBindCheck(String password);
    }
}
