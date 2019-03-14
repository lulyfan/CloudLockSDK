package ut.com.cloudlocklib;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.Random;

import ut.com.cloudlocklib.encrypt.AesEncrypt;
import ut.com.cloudlocklib.encrypt.IEncrypt;
import ut.com.cloudlocklib.encrypt.TeaEncrypt;

public class CmdUtil {

    public static final int ENCRYPT_TEA = 0;
    public static final int ENCRYPT_AES = 1;

    private static final int CODE_INIT = 0x20;            //初始化锁命令
    private static final int CODE_CONFIRM_INIT = 0x2C;    //确认初始化锁命令
    private static final int CODE_OPERATE_LOCK = 0x1F;    //操作锁命令
    private static final int CODE_RESET = 0x21;           //重置锁命令

    public static int SINGLE_CONTROL = 0;                 //单独控制
    public static int DELAY_CONTROL = 1;                  //延时控制

    public static int OPERATE_OPEN_LOCK = 1;              //开锁
    public static int OPERATE_CLOSE_LOCK = 0;             //关锁

    private static int autoIncreaseNum;                    //自增变量，用于防重复攻击
    private static int cmdId;
    private static IEncrypt encrypt;
    private static IEncrypt fixedEncrypt;
    private static int encryptType;                        //加密类型
    private static byte[] encryptKey;                      //加密密钥

    static {
        byte[] key = new byte[]{0x14, 0x18, (byte) 0x82, 0x02, (byte) 0xE9, 0x6B, (byte) 0x88, (byte) 0xAD,
                (byte) 0xFF, 0x0C, 0x11, 0x79, (byte) 0xAF, 0x39, 0x5B, (byte) 0xEE};
        fixedEncrypt = new TeaEncrypt(key);
    }


    /**
     * 初始化锁
     * @param bindPassword 锁的认证密码
     * @return
     */
    public static List<byte[]> initLock(String bindPassword, InitListener initListener) {

         byte[] adminPassword = new byte[6];
         byte[] openLockPassword = new byte[6];
         byte[] secretKey = new byte[8];     //密钥
         byte encryptVersion = 0;         //加密版本

        Random random = new Random();
        random.nextBytes(adminPassword);
        random.nextBytes(openLockPassword);
        random.nextBytes(secretKey);
        encryptVersion = (byte) random.nextInt(2);

        if (initListener != null) {
            initListener.init(adminPassword, openLockPassword, secretKey, encryptVersion);
        }

        int checkCodeLength = bindPassword == null ? 0 : bindPassword.getBytes().length;
        int contentLength = adminPassword.length + openLockPassword.length + secretKey.length + checkCodeLength + 1 + 2;
        ByteBuffer buffer = ByteBuffer.allocate(contentLength);
        buffer.put(adminPassword);
        buffer.put(openLockPassword);
        buffer.put(encryptVersion);
        buffer.put(secretKey);
        buffer.putShort((short) autoIncreaseNum);

        if (bindPassword != null && !"".equals(bindPassword.trim())) {
            buffer.put(bindPassword.getBytes());
        }

        return buildMsg(CODE_INIT, buffer.array(), true);
    }

    /**
     * 确认初始化锁
     * @param adminPassword 管理员密码
     * @return
     */
    public static List<byte[]> confirmInitLock(byte[] adminPassword) {

        if (adminPassword == null || adminPassword.length != 6) {
            throw new IllegalArgumentException("管理员密码不能为空并且长度必须为6");
        }

        return buildMsg(CODE_CONFIRM_INIT, adminPassword, true);
    }

    /**
     * 开云锁
     * @param openLockPassword 开锁密码
     * @return
     */
    public static List<byte[]> openLock(byte[] openLockPassword) {
        return operateLock(openLockPassword, OPERATE_OPEN_LOCK, SINGLE_CONTROL, 0, 1, 1);
    }

    public static List<byte[]> operateLock(byte[] openLockPassword, int operateType, int controlType, int gapTime, int deviceNode, int value) {
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

        return buildMsg(CODE_OPERATE_LOCK, buffer.array(), false);
    }

    /**
     * 重置锁
     * @param adminPassword 管理员密码
     * @return
     */
    public static List<byte[]> resetLock(byte[] adminPassword) {
        if (adminPassword == null || adminPassword.length != 6) {
            throw new IllegalArgumentException("管理员密码不能为空, 并且长度必须为6位");
        }

        ByteBuffer buffer = ByteBuffer.allocate(adminPassword.length + 2);
        buffer.put(adminPassword);
        buffer.putShort((short) autoIncreaseNum);

        return buildMsg(CODE_RESET, buffer.array(), false);
    }

    /**
     *
     * @param code
     * @param content
     * @param isFixedEncrypt 是否是固定加密
     * @return
     */
    private static List<byte[]> buildMsg(int code, byte[] content, boolean isFixedEncrypt) {
        int contentLength = (content == null ? 0 : content.length);
        int msgLength = 1 + 1 + contentLength; //功能码 + 正文长度 + 数据正文
        ByteBuffer buffer = ByteBuffer.allocate(msgLength);
        buffer.put((byte) code);
        buffer.put((byte) contentLength);
        if (content != null) {
            buffer.put(content);
        }

        autoIncreaseNum = (autoIncreaseNum + 1) % 65536;

        byte[] data = buffer.array();
        if (isFixedEncrypt) {
            data = fixedEncrypt.encrypt(data);
        } else {
            if (encrypt != null) {
                data = encrypt.encrypt(data);
            }
        }

        byte[] wrapData = new byte[data.length + 3];
        System.arraycopy(data, 0, wrapData, 0, data.length);
        wrapData[wrapData.length - 1] = (byte) (cmdId % 256);                //最后一个字节放入requestID,用来当做帧处理层的命令ID
        wrapData[wrapData.length - 2] = 1;                                   //倒数第二个字节放是否加密标识
        wrapData[wrapData.length - 3] = (byte) (isFixedEncrypt ? 1 : 0);
        cmdId ++;

        FrameHandler frameHandler = new FrameHandler();
        return frameHandler.handleSend(wrapData);
    }

    /**
     * 设置自增变量
     * @param autoIncreaseNum
     */
    public static void setAutoIncreaseNum(int autoIncreaseNum) {
        CmdUtil.autoIncreaseNum = autoIncreaseNum;
    }

    /**
     * 设置加密方式
     * @param encryptType 加密类型 {@link #ENCRYPT_TEA}, {@link #ENCRYPT_AES}
     * @param encrypyKey 加密密钥
     */
    public static void setEncrypt(int encryptType, byte[] encrypyKey) {
        switch (encryptType) {
            case ENCRYPT_TEA:
                encrypt = new TeaEncrypt(encrypyKey);
                break;

            case ENCRYPT_AES:
                encrypt = new AesEncrypt(encrypyKey);
                break;

                default:
        }
    }

    public interface InitListener {
        void init(byte[] adminPassword, byte[] openLockPassword, byte[] encryptKey, int enceyptType);
    }
}
