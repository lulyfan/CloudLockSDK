package com.ut.unilink.cloudLock.protocol.cmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;

public class StateResponse extends BleCmdBase<Void>{

    private static final int CODE = 0x25;

    @Override
    public BleMsg build() {
        BleMsg msg = new BleMsg();
        msg.setCode((byte) CODE);
        msg.setNeedResponse(false);
        return msg;
    }

    @Override
    public Void parse(BleMsg msg) {
        return null;
    }
}
