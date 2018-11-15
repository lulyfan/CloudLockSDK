package com.ut.unilink.cloudLock.protocol.cmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;
import com.ut.unilink.cloudLock.protocol.data.ProductInfo;

import java.nio.ByteBuffer;

public class GetProductInfo extends BleCmdBase<ProductInfo>{

    private static final byte CODE = 0x26;

    @Override
    public BleMsg build() {
        BleMsg msg = new BleMsg();
        msg.setCode(CODE);
        return msg;
    }

    @Override
    ProductInfo parse(BleMsg msg) {
        ByteBuffer buffer = ByteBuffer.wrap(msg.getContent());
        ProductInfo data = new ProductInfo();
        buffer.get(data.version);
        buffer.get(data.serialNum);
        buffer.get(data.vendorId);
        return data;
    }


}
