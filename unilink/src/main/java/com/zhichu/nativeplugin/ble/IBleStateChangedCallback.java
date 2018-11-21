package com.zhichu.nativeplugin.ble;

public interface IBleStateChangedCallback {

    /**
     * @param state {@link android.bluetooth.BluetoothAdapter#STATE_OFF }
     *              {@link android.bluetooth.BluetoothAdapter#STATE_TURNING_OFF}
     *              {@link android.bluetooth.BluetoothAdapter#STATE_ON}
     *              {@link android.bluetooth.BluetoothAdapter#STATE_TURNING_ON}
     */
    void onState(int state);

}
