package com.ut.unilink.cloudLock.protocol;

import java.nio.ByteBuffer;

public class BleMsg {

    private int dataLength;    //数据正文字节长度
    private boolean isResponseError;
    private byte code;           //功能码
    private byte[] content;
    private IEncrypt mEntrypt = NO_ENCRYPT;
    private int requestID;
    private boolean isEncrypt = true;   //标识消息是否加密, 默认加密

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

        if (mEntrypt == null || !isEncrypt) {
            mEntrypt = NO_ENCRYPT;
        }
        byte[] encryptMsg = mEntrypt.encrypt(buffer.array());

        return encryptMsg;
    }

    public static BleMsg decode(byte[] data, IEncrypt encrypt) {

        BleMsg msg = new BleMsg();
        if (encrypt != null) {
            msg.mEntrypt = encrypt;
        }

        ByteBuffer byteBuf = ByteBuffer.wrap(msg.getEntrypt().decrypt(data));

        byte temp = byteBuf.get();
        msg.code = (byte) (temp & 0x7F);
        msg.isResponseError = ((temp & 0x80) >>> 7) == 1;
        msg.dataLength = byteBuf.get();

        byte[] content = new byte[msg.dataLength];
        byteBuf.get(content);
        msg.content = content;

        return msg;
    }

    public void setEncrypt(boolean encrypt) {
        isEncrypt = encrypt;
    }

    public boolean isEncrypt() {
        return isEncrypt;
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
}
