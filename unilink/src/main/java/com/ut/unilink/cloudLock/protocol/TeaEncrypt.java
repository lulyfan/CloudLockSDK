package com.ut.unilink.cloudLock.protocol;

import com.ut.unilink.util.MyTea;

public class TeaEncrypt implements IEncrypt{

    private byte[] key;

    public TeaEncrypt(byte[] key) {
        this.key = key;
    }

    @Override
    public byte[] encrypt(byte[] src) {
        return MyTea.encrypt(src, key);
    }

    @Override
    public byte[] decrypt(byte[] src) {
        return MyTea.decrypt(src, key);
    }
}
