package com.ut.unilink;

import android.support.test.runner.AndroidJUnit4;

import com.zhichu.nativeplugin.ble.BleDevice;
import com.zhichu.nativeplugin.ble.scan.IScanCallback;
import com.zhichu.nativeplugin.ble.scan.UTFilterScanCallback;

import org.junit.runner.RunWith;

import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class Test {

    @org.junit.Test
    public void test() {
        UTFilterScanCallback scanCallback = new UTFilterScanCallback(new IScanCallback() {
            @Override
            public void onDeviceFound(BleDevice bleDevice, List<BleDevice> result) {

            }

            @Override
            public void onScanFinish(List<BleDevice> result) {

            }

            @Override
            public void onScanTimeout() {

            }
        });
        ByteBuffer buffer = ByteBuffer.allocate(18);
        buffer.put((byte) 0x0C);
        buffer.put((byte) 0xFF);
        buffer.put((byte) 0x55);
        buffer.put((byte) 0x54);
        buffer.put((byte) 0x01);                                      //version
        buffer.put("aaaa".getBytes());               //vendorId
        buffer.put((byte) 0x00);                                      //isActive (0,1)
        buffer.put(new byte[]{(byte) 0xA4, 0x00});                    //lock type
        buffer.put(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06});   //mac

        scanCallback.setVendorId("aaaa");
        scanCallback.setDeviceType(new byte[]{(byte) 0xA4, 0x00});
        boolean result = scanCallback.onFilter(null, 0, buffer.array());

        assertEquals(true, result);
        System.out.println("result:" + result);
    }
}
