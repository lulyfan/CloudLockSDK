package com.ut.unilink.util;

public class BitUtil {

    /**
     * 获取指定位，
     * @param data
     * @param pos
     * @return 0或1
     */
    public static int bit(int data, int pos) {
        if (pos < 0) {
            throw new IllegalArgumentException("pos must >= 0");
        }

        return (data & (0x01 << pos)) != 0 ? 1 : 0;
    }

    /**
     * 对指定位进行置一操作
     * @param data
     * @param pos
     * @return
     */
    public static int set1(int data, int pos) {
        if (pos < 0) {
            throw new IllegalArgumentException("pos must >= 0");
        }

        return data | (0x01 << pos);
    }

    /**
     * 对指定位进行清零操作
     * @param data
     * @param pos
     * @return
     */
    public static int set0(int data, int pos) {
        if (pos < 0) {
            throw new IllegalArgumentException("pos must >= 0");
        }

        return data & (~(0x01 << pos));
    }

    /**
     * 对指定位设置值
     * @param data
     * @param pos
     * @param value 0或1
     * @return
     */
    public static int set(int data, int pos, int value) {
        if (pos < 0) {
            throw new IllegalArgumentException("pos must >= 0");
        }

        if (value != 0 && value != 1) {
            throw new IllegalArgumentException("value must be 0 or 1");
        }

        data = set0(data, pos);
        return data | (value << pos);
    }
}
