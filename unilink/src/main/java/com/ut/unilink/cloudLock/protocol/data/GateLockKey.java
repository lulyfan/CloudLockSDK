package com.ut.unilink.cloudLock.protocol.data;

import com.ut.unilink.util.BitUtil;

public class GateLockKey {

    public static final int TYPE_FINGERPRINT = 0; //指纹钥匙
    public static final int TYPE_PASSWORD = 1;    //密码钥匙
    public static final int TYPE_ELECT = 2;  //电子钥匙
    public static final int TYPE_BLE = 3;    //蓝牙钥匙
    public static final int TYPE_CARD = 4;   //卡片钥匙

    private byte keyType;
    private byte attribute; //钥匙配置属性
    private int innerNum;  //钥匙内部编号
    private int keyId;     //钥匙编号

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public byte getKeyType() {
        return keyType;
    }

    public void setKeyType(byte keyType) {
        this.keyType = keyType;
    }

    public int getInnerNum() {
        return innerNum;
    }

    public void setInnerNum(int innerNum) {
        this.innerNum = innerNum;
    }

    public byte getAttribute() {
        return attribute;
    }

    public void setAttribute(byte attribute) {
        this.attribute = attribute;
    }

    /**
     * 设置钥匙授权状态
     * @param isAuth
     */
    public void setAuthState(boolean isAuth) {
        int auth = isAuth ? 1 : 0;
        attribute = (byte) BitUtil.set(attribute, 0, auth);
    }

    /**
     * 获取钥匙授权状态
     * @return
     */
    public boolean isAuthKey() {
        int auth = BitUtil.bit(attribute, 0);
        return auth == 1 ? true : false;
    }

    /**
     * 获取钥匙名称备注状态
     * @return
     */
    public boolean isNameMark() {
        int mark = BitUtil.bit(attribute, 1);
        return mark == 1 ? true : false;
    }

    /**
     *
     * 设置钥匙名称备注状态
     * @param isMark
     */
    public void setNameMarkState(boolean isMark) {
        int mark = isMark ? 1 : 0;
        attribute = (byte) BitUtil.set(attribute, 1, mark);
    }

    /**
     * 得到钥匙冻结状态
     * @return
     */
    public boolean isFreeze() {
        int freeze = BitUtil.bit(attribute, 2);
        return freeze == 1 ? true : false;
    }

    /**
     * 设置钥匙冻结状态
     * @param isFreeze
     */
    public void setFreezeState(boolean isFreeze) {
        int freeze = isFreeze ? 1 : 0;
        attribute = (byte) BitUtil.set(attribute, 2, freeze);
    }

    @Override
    public String toString() {
        String type = "";
        switch (keyType) {
            case TYPE_FINGERPRINT:
                type = "指纹";
                break;

            case TYPE_BLE:
                type = "蓝牙";
                break;

            case TYPE_CARD:
                type = "卡片";
                break;

            case TYPE_ELECT:
                type = "电子钥匙";
                break;

            case TYPE_PASSWORD:
                type = "密码";
                break;

                default:
        }
        return "钥匙ID:" + keyId +
                "\n钥匙类型:" + type +
                "\n" + (isAuthKey() ? "授权钥匙" : "正常钥匙") +
                "\n备注状态:" + (isNameMark() ? "已备注" : "未备注") +
                "\n冻结状态:" + (isFreeze() ? "已冻结" : "未冻结");
    }
}
