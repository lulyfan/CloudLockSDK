package com.ut.unilink.cloudLock.protocol;

public interface IEncrypt {
    byte[] encrypt(byte[] src);
    byte[] decrypt(byte[] src);
}
