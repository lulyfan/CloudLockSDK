package com.ut.unilink.util;

public class Log {
    private static boolean isEnableLog = true;
    private static final String TAG = "unilink";

    public static void enableLog(boolean isEnable) {
        isEnableLog = isEnable;
    }

    public static void i(String tag, String msg) {
        if (isEnableLog) {
            android.util.Log.i(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (isEnableLog) {
            android.util.Log.d(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (isEnableLog) {
            android.util.Log.e(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (isEnableLog) {
            android.util.Log.w(tag, msg);
        }
    }

    public static void i(String msg) {
        if (isEnableLog) {
            android.util.Log.i(TAG, msg);
        }
    }

    public static void d(String msg) {
        if (isEnableLog) {
            android.util.Log.d(TAG, msg);
        }
    }

    public static void e(String msg) {
        if (isEnableLog) {
            android.util.Log.e(TAG, msg);
        }
    }

    public static void w(String msg) {
        if (isEnableLog) {
            android.util.Log.w(TAG, msg);
        }
    }

    public static String toUnsignedHex(byte[] data, String split) {
        String result = "";
        for (int i = 0; i < data.length; i++) {
            result += String.format("%02x", data[i] & 0xFF) + split;
        }
        return result.substring(0, result.length() - 1);
    }

    public static byte[] getBytes(String data) {

        data = data.replaceAll(" ", "");

        if (data.length() % 2 != 0) {
            data = "0" + data;
        }

        int length = data.length() / 2;
        byte[] result = new byte[length];

        for (int i=0, j=0; i<length; i++, j+=2) {
            String item = data.substring(j, j+2);
            result[i] = (byte) Integer.parseInt(item, 16);
        }
        return result;
    }

    public static String toUnsignedHex(byte[] data) {
       return toUnsignedHex(data, " ");
    }
}
