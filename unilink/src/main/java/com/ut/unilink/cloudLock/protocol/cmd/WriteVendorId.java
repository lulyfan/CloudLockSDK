package com.ut.unilink.cloudLock.protocol.cmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;

import java.nio.ByteBuffer;

public class WriteVendorId extends BleCmdBase<Void>{

    private static final byte CODE = 0x29;

    private byte[] vendorId;
    private byte[] deviceType;

    public WriteVendorId(byte[] vendorId, byte[] deviceType) {
        if (vendorId == null || deviceType == null) {
            throw new IllegalArgumentException("厂商标识或设备类型不能为空");
        }

        if (vendorId.length != 4 || deviceType.length != 2) {
            throw new IllegalArgumentException("厂商标识需为4字节，设备类型需为2字节");
        }

        this.vendorId = vendorId;
        this.deviceType = deviceType;
    }

    @Override
    public BleMsg build() {
        BleMsg msg = new BleMsg();
        msg.setCode(CODE);
        msg.setEncryptType(BleMsg.ENCRYPT_TYPE_FIXED);

        ByteBuffer buffer = ByteBuffer.allocate(4 + 2);
        buffer.put(vendorId);
        buffer.put(deviceType);
        msg.setContent(buffer.array());

        return msg;
    }

    @Override
    Void parse(BleMsg msg) {
        return null;
    }
}
