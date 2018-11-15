package com.ut.unilink.cloudLock.protocol.cmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;
import com.ut.unilink.cloudLock.protocol.ClientHelper;

/**
 * Created by huangkaifan on 2018/6/8.
 */

public abstract class BleCmdBase<T> {

    protected ClientHelper clientHelper;
    private int timeoutCount;
    protected static int autoIncreaseNum = 1;

    abstract public BleMsg build();
    abstract T parse(BleMsg msg);

    public void setClientHelper(ClientHelper clientHelper) {
        this.clientHelper = clientHelper;
    }

    public static void setAutoIncreaseNum(int num) {
        autoIncreaseNum = num;
    }

    private void wrapSend(ClientHelper.ResponseListener responseListener) {

        autoIncreaseNum = (autoIncreaseNum + 1) % 65535;
        BleMsg msg = build();
        clientHelper.asyncSend(msg, responseListener);
    }

    public void sendMsg(final BleCallBack<T> callBack) {

        if (clientHelper == null) {
            return;
        }
        timeoutCount = 0;

        ClientHelper.ResponseListener responseListener = new ClientHelper.ResponseListener() {

            @Override
            public void timeout(BleMsg sendMsg) {
                // TODO Auto-generated method stub
                timeoutCount ++;

                if (timeoutCount >= 3) {
                    if (callBack != null) {
                        callBack.timeout();
                    }
                } else {
                    wrapSend(this);
                }
            }

            @Override
            public void onACk(BleMsg sendMsg, BleMsg responseMsg) {
                if (callBack != null) {
                    callBack.success(parse(responseMsg));
                }
            }

            @Override
            public void onNAk(BleMsg sendMsg, int errCode) {
                if (callBack != null) {
                    String errMsg = ErrCode.getMessage(errCode);
                    callBack.fail(errCode, errMsg);
                }
            }
        };
        wrapSend(responseListener);

    }

    public void syncSendMsg(final BleCallBack<T> callBack) {

        if (clientHelper == null) {
            return;
        }

        timeoutCount = 0;

        BleMsg msg = build();

        clientHelper.send(msg, new ClientHelper.ResponseListener() {

            @Override
            public void timeout(BleMsg sendMsg) {
                // TODO Auto-generated method stub
                timeoutCount ++;

                if (timeoutCount >= 3) {
                    if (callBack != null) {
                        callBack.timeout();
                    }
                } else {
                    clientHelper.send(sendMsg, this);
                }
            }

            @Override
            public void onACk(BleMsg sendMsg, BleMsg responseMsg) {
                if (callBack != null) {
                    callBack.success(parse(responseMsg));
                }
            }

            @Override
            public void onNAk(BleMsg sendMsg, int errCode) {
                if (callBack != null) {
                    String errMsg = ErrCode.getMessage(errCode);
                    callBack.fail(errCode, errMsg);
                }
            }
        });
    }
}
