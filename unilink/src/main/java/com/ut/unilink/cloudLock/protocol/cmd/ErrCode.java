package com.ut.unilink.cloudLock.protocol.cmd;

public class ErrCode {

    public static final int ERR_FUNCTION_CODE = 0x01;  //功能码错误
    public static final int ERR_DATA = 0x02;           //数据错误
    public static final int ERR_DEVICE_BUSY = 0x03;    //设备忙
    public static final int ERR_CHECK_CODE = 0x04;     //CRC校验错误
    public static final int ERR_REPEAT_CODE = 0x05;    //防重复攻击校验码错误
    public static final int ERR_TIMEOUT = -1;
    public static final int ERR_OPENLOCK = -2;          //开锁失败
    public static final int ERR_ADMIN_PASSWORD = -3;    //管理员密码错误
    public static final int ERR_WRITE_VENDOR_ID = -4;   //写入厂商标识失败


    public static String getMessage(int errCode) {

        String errMsg;

        switch (errCode) {
            case ERR_FUNCTION_CODE:
                errMsg = "功能码错误";
                break;

            case ERR_DATA:
                errMsg = "数据错误";
                break;

            case ERR_DEVICE_BUSY:
                errMsg = "设备忙";
                break;

            case ERR_CHECK_CODE:
                errMsg = "CRC校验错误";
                break;

            case ERR_REPEAT_CODE:
                errMsg = "防重复攻击校验码错误";
                break;

            case ERR_TIMEOUT:
                errMsg = "应答超时";
                break;

            case ERR_OPENLOCK:
                errMsg = "开锁失败";
                break;

            case ERR_ADMIN_PASSWORD:
                errMsg = "管理员密码错误";
                break;

            case ERR_WRITE_VENDOR_ID:
                errMsg = "写入厂商标识失败";
                break;

                default:
                    errMsg = "未知错误";
        }
        return errMsg;
    }
}
