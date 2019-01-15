//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ut.unilink.jobluetooth;

public class Tools {
    public Tools() {
    }

    public static byte[] hexStrToStr(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];

        for(int i = 0; i < bytes.length; ++i) {
            int n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte)(n & 255);
        }

        return bytes;
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if(src != null && src.length > 0) {
            for(int i = 0; i < src.length; ++i) {
                int v = src[i] & 255;
                String hv = Integer.toHexString(v);
                if(hv.length() < 2) {
                    stringBuilder.append(0);
                }

                stringBuilder.append(hv);
                stringBuilder.append(" ");
            }

            return stringBuilder.toString();
        } else {
            return null;
        }
    }

    public static String byteToHexString(byte buf) {
        byte[] src = new byte[]{buf};
        StringBuilder stringBuilder = new StringBuilder("");
        if(src != null && src.length > 0) {
            for(int i = 0; i < src.length; ++i) {
                int v = src[i] & 255;
                String hv = Integer.toHexString(v);
                if(hv.length() < 2) {
                    stringBuilder.append(0);
                }

                stringBuilder.append(hv);
            }

            return stringBuilder.toString() + " ";
        } else {
            return null;
        }
    }

    public static byte checksum(byte[] buffer, int length) {
        byte nSum = 0;

        for(int i = 0; i < length; ++i) {
            nSum += buffer[i];
        }

        return nSum;
    }
}
