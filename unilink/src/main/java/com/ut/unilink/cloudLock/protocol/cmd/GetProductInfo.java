package com.ut.unilink.cloudLock.protocol.cmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;
import com.ut.unilink.cloudLock.protocol.data.ProductInfo;

import java.nio.ByteBuffer;

public class GetProductInfo extends BleCmdBase<ProductInfo>{

    private static final byte CODE = 0x26;

    @Override
    public BleMsg build() {
        BleMsg msg = new BleMsg();
        msg.setEncryptType(BleMsg.ENCRYPT_TYPE_FIXED);
        msg.setCode(CODE);
        return msg;
    }

    @Override
    public ProductInfo parse(BleMsg msg) {
        ByteBuffer buffer = ByteBuffer.wrap(msg.getContent());
        ProductInfo data = new ProductInfo();
        buffer.get(data.getVersion());
        buffer.get(data.getSerialNum());
        buffer.get(data.getVendorId());
        return data;
    }


}
