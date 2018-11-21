package com.ut.unilink.util;

import com.ut.unilink.util.crc.CrcCheck;

import org.junit.Test;

public class CRCTest {

    @Test
    public void test() {
        byte[] data = new byte[]{(byte) 0xA5};
        CrcCheck check = new CrcCheck(16, 0x1021, false, 0, 0x00, 0);
        System.out.println(check.CountCheckAllCode(data));
    }
}
