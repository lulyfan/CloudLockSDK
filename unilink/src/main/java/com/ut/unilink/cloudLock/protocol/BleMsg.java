package com.ut.unilink.cloudLock.protocol;

import com.ut.unilink.util.Log;

import java.nio.ByteBuffer;

public class BleMsg {

    public static final int ENCRYPT_TYPE_NO = -1;           //不加密
    public static final int ENCRYPT_TYPE_FIXED = 1;         //固定加密方式
    public static final int ENCRYPT_TYPE_DYNAMIC = 0;       //动态加密方式

    private int dataLength;    //数据正文字节长度
    private boolean isResponseError;
    private byte code;           //功能码
    private byte[] content;
    private IEncrypt mEntrypt;
    private int requestID;

    private int encryptType = ENCRYPT_TYPE_DYNAMIC;          //默认使用动态加密

    public static IEncrypt NO_ENCRYPT = new IEncrypt() {
        @Override
        public byte[] encrypt(byte[] src) {
            return src;
        }

        @Override
        public byte[] decrypt(byte[] src) {
            return src;
        }
    };

    public byte[] encode() {

        dataLength = (content == null ? 0 : content.length);
        int msgLength = 1 + 1 + dataLength; //功能码 + 正文长度 + 数据正文
        ByteBuffer buffer = ByteBuffer.allocate(msgLength);
        buffer.put(code);
        buffer.put((byte) dataLength);
        if (content != null) {
            buffer.put(content);
        }

        if (mEntrypt == null) {
            mEntrypt = NO_ENCRYPT;
        }

        Log.i("加密前:" + Log.toUnsignedHex(buffer.array()));
        byte[] encryptMsg = mEntrypt.encrypt(buffer.array());

        return encryptMsg;
    }

    public static BleMsg decode(byte[] data, IEncrypt encrypt) {

        BleMsg msg = new BleMsg();
        msg.mEntrypt = encrypt;

        if (msg.mEntrypt == null) {
            msg.mEntrypt = NO_ENCRYPT;
        }

        byte[] decryptData = msg.getEntrypt().decrypt(data);
        ByteBuffer byteBuf = ByteBuffer.wrap(decryptData);
        Log.i("解密后:" + Log.toUnsignedHex(decryptData));

        byte temp = byteBuf.get();
        msg.code = (byte) (temp & 0x7F);
        msg.isResponseError = ((temp & 0x80) >>> 7) == 1;
        msg.dataLength = byteBuf.get();

        byte[] content = new byte[msg.dataLength];
        byteBuf.get(content);
        msg.content = content;

        return msg;
    }

    public void setRequestID(int requestID) {
        this.requestID = requestID;
    }

    public int getRequestID() {
        return requestID;
    }

    public boolean isResponseError() {
        return isResponseError;
    }

    public void setResponseError(boolean responseError) {
        isResponseError = responseError;
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public IEncrypt getEntrypt() {
        return mEntrypt;
    }

    public void setEntrypt(IEncrypt mEntrypt) {
        this.mEntrypt = mEntrypt;
    }

    public int getEncryptType() {
        return encryptType;
    }

    public void setEncryptType(int encryptType) {
        this.encryptType = encryptType;
    }

    public boolean isEncrypt() {
        return encryptType == ENCRYPT_TYPE_NO ? false : true;
    }
}
