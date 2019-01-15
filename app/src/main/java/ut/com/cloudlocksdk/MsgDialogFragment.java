package ut.com.cloudlocksdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

public class MsgDialogFragment extends DialogFragment {

    public static MsgDialogFragment getInstance(String title, String msg) {
        MsgDialogFragment fragment = new MsgDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("msg", msg);
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        String title = getArguments().getString("title");
        String msg = getArguments().getString("msg");
        builder.setTitle(title)
                .setMessage(msg)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    public static void show(AppCompatActivity activity, String title, String msg) {
        MsgDialogFragment fragment = getInstance(title, msg);
        fragment.show(activity.getSupportFragmentManager(), "");
    }
}
