package com.ut.unilink.cloudLock;

public interface IConnectionManager {
    void onConnect(String address);
    void onDisConnect(String address, int code);
    void onReceive(String address, byte[] data);
    void send(String address, byte[] data);
}
