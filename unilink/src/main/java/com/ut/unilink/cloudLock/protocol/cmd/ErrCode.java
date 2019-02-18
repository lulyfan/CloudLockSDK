package com.ut.unilink.cloudLock.protocol.cmd;

public class ErrCode {
    public static final int ERR_ENCRYPT = 0x00;        //解密错误
    public static final int ERR_FUNCTION_CODE = 0x01;  //非法功能码
    public static final int ERR_DATA = 0x02;           //非法数据
    public static final int ERR_DEVICE_BUSY = 0x03;    //设备忙
    public static final int ERR_CHECK_CODE = 0x04;     //CRC校验错误
    public static final int ERR_REPEAT_CODE = 0x05;    //防重复攻击校验码错误
    public static final int ERR_OPERATE = 0x06;        //操作异常
    public static final int ERR_ADMIN_PASSWORD = 0x07;    //管理员密码校验失败
    public static final int ERR_OPENLOCK_PASSWORD = 0x08; //开锁密码校验失败
    public static final int ERR_ALREADY_ACTIVE = 0x09;    //重复激活错误
    public static final int ERR_NOT_ACTIVE = 0x0A;        //未激活错误
    public static final int ERR_BIND_PASSWORD = 0x0E;     //绑定密码错误

    public static final int ERR_UNKNOW = 0xFF;            //未知错误
    public static final int ERR_TIMEOUT = -1;             //应答超时
    public static final int ERR_NO_CONNECT = -2;          //设备未连接
    public static final int ERR_CONNECT_INTERRUPT = -5;   //连接中断

    public static final int ERR_OPERATE_TYPE = -3;        //操作类型错误
    public static final int ERR_READ_SERIAL_NUM = -4;     //读取序号错误

    public static String getMessage(int errCode) {

        String errMsg;

        switch (errCode) {
            case ERR_FUNCTION_CODE:
                errMsg = "非法功能码";
                break;

            case ERR_DATA:
                errMsg = "非法数据";
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

            case ERR_OPERATE:
                errMsg = "操作异常";
                break;

            case ERR_ADMIN_PASSWORD:
                errMsg = "管理员密码校验失败";
                break;

            case ERR_OPENLOCK_PASSWORD:
                errMsg = "开锁密码校验失败";
                break;

            case ERR_ALREADY_ACTIVE:
                errMsg = "设备已激活";
                break;

            case ERR_NOT_ACTIVE:
                errMsg = "设备未激活";
                break;

            case ERR_BIND_PASSWORD:
                errMsg = "认证密码错误";
                break;

            case ERR_TIMEOUT:
                errMsg = "应答超时";
                break;

            case ERR_NO_CONNECT:
                errMsg = "设备未连接";
                break;

            case ERR_UNKNOW:
                errMsg = "未知错误";
                break;

            case ERR_READ_SERIAL_NUM:
                errMsg = "读取序号超出取值范围";
                break;

            case ERR_CONNECT_INTERRUPT:
                errMsg = "连接中断";
                break;

            case ERR_ENCRYPT:
                errMsg = "解密异常";
                break;

                default:
                    errMsg = "未知错误";
        }
        return errMsg;
    }
}
