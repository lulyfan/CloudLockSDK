package com.ut.ble.control.i;

/**
 * author : zhouyubin
 * time   : 2018/08/29
 * desc   :
 * version: 1.0
 */
public interface IEnableResult {
    void onSuccess();

    void onFailure(int msgID);

    void onFailure(String msg);
}
