package com.ut.unilink.jobluetooth;

/**
 * Created by Zty on 2016/6/13.
 */
public class HexUtils {

    private static final char HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String getFormatHex(byte[] buff) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= buff.length; i++) {
            sb.append(HEX_DIGITS[(buff[i - 1] & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[buff[i - 1] & 0x0f]);
            sb.append(" ");
            if (i % 20 == 0) {
                sb.append("\n");
            } else if (i % 4 == 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public static String getFormatHexNonSpace(byte[] buff) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= buff.length; i++) {
            sb.append(HEX_DIGITS[(buff[i - 1] & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[buff[i - 1] & 0x0f]);
        }
        return sb.toString();
    }
}
