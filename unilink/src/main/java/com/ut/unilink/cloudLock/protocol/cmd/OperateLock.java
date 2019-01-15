package com.ut.unilink.cloudLock.protocol.cmd;

import com.ut.unilink.cloudLock.protocol.BleMsg;
import com.ut.unilink.util.BitUtil;

import java.nio.ByteBuffer;
import java.util.Date;

public class OperateLock extends BleCmdBase<Void>{

    private static final int CODE = 0x1F;

    private int gapTime; //表示间隔时间，当开锁类型为延时控制时有效， 时间范围(0-31)S
    private int operateType;  //操作类型，指开关锁
    private int controlType;  //控制类型
    private int deviceNode;   //设备节点编号
    private int value;        //设置进节点的数据
    private byte[] openLockPassword;

    public static int SINGLE_CONTROL = 0; //单独控制
    public static int DELAY_CONTROL = 1; //延时控制

    public static int OPERATE_OPEN_LOCK = 1; //开锁
    public static int OPERATE_CLOSE_LOCK = 0; //关锁

    public OperateLock(byte[] openLockPassword, int operateType, int controlType, int gapTime, int deviceNode, int value) {
        if (openLockPassword == null || openLockPassword.length != 6) {
            throw new IllegalArgumentException("开锁密码不能为null，并且长度必须为6位");
        }

        if (operateType != OPERATE_OPEN_LOCK && operateType != OPERATE_CLOSE_LOCK) {
            throw new IllegalArgumentException("操作类型必须为开锁或关锁");
        }

        if (!(controlType >=0 && controlType <= 4)) {
            throw new IllegalArgumentException("控制类型必须为0~4");
        }

        if (!(gapTime >=0 && gapTime <= 31)) {
            throw new IllegalArgumentException("间隔时间必须为0~31");
        }

        this.openLockPassword = openLockPassword;
        this.operateType = operateType;
        this.controlType = controlType;
        this.deviceNode = deviceNode;
        this.value = value;
    }

    @Override
    public BleMsg build() {
        BleMsg msg = new BleMsg();
        msg.setCode((byte) CODE);
        ByteBuffer buffer = ByteBuffer.allocate(6 + 2 + 7);
        buffer.put(openLockPassword);
        buffer.putShort((short) autoIncreaseNum);
        int time = (int) (new Date().getTime() / 1000);
        buffer.putInt(time);

        byte controlInfo = 0;
        controlInfo = (byte) BitUtil.set(controlInfo, 7, operateType);
        controlInfo = (byte) (controlInfo | gapTime);
        controlInfo = (byte) (controlInfo | (controlType << 5));
        buffer.put(controlInfo);
        buffer.put((byte) deviceNode);
        buffer.put((byte) value);

        msg.setContent(buffer.array());
        return msg;
    }

    @Override
    public Void parse(BleMsg msg) {
        return null;
    }
}
