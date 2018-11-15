package com.zhichu.nativeplugin.ble;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PeripheralDevicePool {
    private Map<String, PeripheralDevice> peripheralDeviceMap;

    public PeripheralDevicePool() {
        peripheralDeviceMap = Collections.synchronizedMap(new HashMap<String, PeripheralDevice>());
    }

    public void addPeripheralDevice(PeripheralDevice peripheralDevice) {
        if (peripheralDeviceMap.containsKey(peripheralDevice.getBleDevice().getDeviceUUID())) {
            peripheralDeviceMap.remove(peripheralDevice.getBleDevice().getDeviceUUID());
        }
        peripheralDeviceMap.put(peripheralDevice.getBleDevice().getDeviceUUID(), peripheralDevice);
    }

    public void removePeripheralDevice(PeripheralDevice peripheralDevice) {
        if (!peripheralDeviceMap.containsKey(peripheralDevice.getBleDevice().getDeviceUUID())) {
            peripheralDeviceMap.remove(peripheralDevice.getBleDevice().getDeviceUUID());
        }
    }

    public void removePeripheralDevice(String deviceUUID) {
        if (peripheralDeviceMap.containsKey(deviceUUID)) {
            peripheralDeviceMap.remove(deviceUUID);
        }
    }

    public boolean contains(String deviceUUID) {
        return peripheralDeviceMap.containsKey(deviceUUID);
    }

    public PeripheralDevice getPeripheralDevice(String deviceUUID) {
        return peripheralDeviceMap.get(deviceUUID);
    }

    public void clear() {
        peripheralDeviceMap.clear();
    }

    public Collection<PeripheralDevice> values() {
        return peripheralDeviceMap.values();
    }
}
