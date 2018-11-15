package com.zhichu.nativeplugin.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;
import java.util.UUID;

public class CharacteristicHelper {

    // Some devices reuse UUIDs across characteristics, so we can't use
    // service.getCharacteristic(characteristicUUID) instead check the UUID
    // and properties for each characteristic in the service until we find the best match
    // This function prefers Notify over Indicate
    public static BluetoothGattCharacteristic findNotifyCharacteristic(
            BluetoothGattService service, UUID characteristicUUID) {
        BluetoothGattCharacteristic characteristic = null;
        try {
            // Check for Notify first
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            for (BluetoothGattCharacteristic c : characteristics) {
                if ((c.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0
                        && characteristicUUID.equals(c.getUuid())) {
                    characteristic = c;
                    break;
                }
            }
            if (characteristic != null) return characteristic;
            // If there wasn't Notify Characteristic, check for Indicate
            for (BluetoothGattCharacteristic c : characteristics) {
                if ((c.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0
                        && characteristicUUID.equals(c.getUuid())) {
                    characteristic = c;
                    break;
                }
            }
            // As a last resort, try and find ANY characteristic with this UUID,
            // even if it doesn't have the correct properties
            if (characteristic == null) {
                characteristic = service.getCharacteristic(characteristicUUID);
            }
            return characteristic;
        } catch (Exception e) {
            return null;
        }
    }


    // Some peripherals re-use UUIDs for multiple characteristics so we need to check the properties
    // and UUID of all characteristics instead of using service.getCharacteristic(characteristicUUID)
    public static BluetoothGattCharacteristic findReadableCharacteristic(
            BluetoothGattService service, UUID characteristicUUID) {
        BluetoothGattCharacteristic characteristic = null;
        int read = BluetoothGattCharacteristic.PROPERTY_READ;
        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
        for (BluetoothGattCharacteristic c : characteristics) {
            if ((c.getProperties() & read) != 0 && characteristicUUID.equals(c.getUuid())) {
                characteristic = c;
                break;
            }
        }
        // As a last resort, try and find ANY characteristic with this UUID,
        // even if it doesn't have the correct properties
        if (characteristic == null) {
            characteristic = service.getCharacteristic(characteristicUUID);
        }
        return characteristic;
    }


    // Some peripherals re-use UUIDs for multiple characteristics so we need to check the properties
    // and UUID of all characteristics instead of using service.getCharacteristic(characteristicUUID)
    public static BluetoothGattCharacteristic findWritableCharacteristic(
            BluetoothGattService service, UUID characteristicUUID, int writeType) {
        try {
            BluetoothGattCharacteristic characteristic = null;
            // get write property
            int writeProperty = BluetoothGattCharacteristic.PROPERTY_WRITE;
            if (writeType == BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE) {
                writeProperty = BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE;
            }
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            for (BluetoothGattCharacteristic c : characteristics) {
                if ((c.getProperties() & writeProperty) != 0
                        && characteristicUUID.equals(c.getUuid())) {
                    characteristic = c;
                    break;
                }
            }
            // As a last resort, try and find ANY characteristic with this UUID,
            // even if it doesn't have the correct properties
            if (characteristic == null) {
                characteristic = service.getCharacteristic(characteristicUUID);
            }
            return characteristic;
        } catch (Exception e) {
            return null;
        }
    }
}
