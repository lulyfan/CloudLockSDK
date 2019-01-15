package com.ut.unilink.cloudLock;

import com.ut.unilink.cloudLock.protocol.data.AuthInfo;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class GateLockAuthTest {

    @Test
    public void test() {
        AuthInfo authInfo = new AuthInfo();
        System.out.println(Arrays.toString(authInfo.getAuthDay()));
        authInfo.setAuthDay(new int[]{0, 1, 2, 5});
        System.out.println(Arrays.toString(authInfo.getAuthDay()));

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        long time = new Date().getTime();
        authInfo.setStartTime(time);
        authInfo.setEndTime(time);

        System.out.println(dateFormat.format(new Date(authInfo.getStartTime())));

        byte[] t = new byte[]{0x36, (byte) 0xcd, 0x05, (byte) 0xa2};
        ByteBuffer buffer = ByteBuffer.wrap(t);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        long temp = buffer.getInt() * 1000L;
        System.out.println(dateFormat.format(temp));
    }
}
