package ut.com.cloudlocklib;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ut.com.cloudlocklib.crc.CrcCheck;

public class FrameHandler {
    private static final byte[] HEAD = {(byte) 0xA5, 0x5A};
    private static int SIZE_FRAME = 20;
    private static final int SIZE_FRAME_CONTROL = 2 + 1 + 1 + 1 + 2; //帧结构信息大小: 同步头 + 报文长度 + 控制字 + ID + 校验码
    private static int SIZE_FRAME_CONTENT = SIZE_FRAME - SIZE_FRAME_CONTROL;  //帧内容大小
    private static final int SIZE_MSG_BUFFER = 512;

    private byte[] msgBuffer = new byte[SIZE_MSG_BUFFER];     //用来存放一条消息
    private int currentCmdID = -1;
    private int receivedByteCount;                //已接收的报文字节

    /**
     * 负责生成消息帧，并根据消息长度进行相应分包
     * @param msg 最后一个字节为cmdID, 倒数第二个字节为是否加密标识, 倒数第三个字节为加密方式标识（动态加密0 固定加密1）
     * @return
     */
    public List<byte[]> handleSend(byte[] msg) {

        if (msg == null) {
            return null;
        }

        int cmdID = msg[msg.length - 1];
        boolean isEncrypt = msg[msg.length - 2] == 1 ? true : false;
        byte encryptType = msg[msg.length - 3];

        byte[] data = new byte[msg.length - 3];
        System.arraycopy(msg, 0, data, 0, data.length);

        int dataLength = data.length;

        int count = dataLength / SIZE_FRAME_CONTENT;   //将数据分成count个帧发送
        if (dataLength % SIZE_FRAME_CONTENT != 0) {
            count ++;
        }

        ByteBuffer frameBuffer = ByteBuffer.allocate(SIZE_FRAME); //存储一帧
        int pos = 0;
        List<byte[]> result = new ArrayList<>();

        for (int i=0; i<count; i++) {
            frameBuffer.put(HEAD);
            frameBuffer.put((byte) dataLength);

            byte control = 0;    //控制字
            control = isEncrypt ? (byte) (control | 0x80) : control;  //设置是否加密
            control = (byte) (control | (encryptType << 6));          //设置加密类型
            control = (byte) (control | i);                           //设置帧号
            frameBuffer.put(control);
            frameBuffer.put((byte) cmdID);

            int remianingLength = dataLength - pos;
            int contentLength = Math.min(remianingLength, SIZE_FRAME_CONTENT) ;   //该帧的内容长度

            frameBuffer.put(data, pos, contentLength);
            pos += contentLength;

            byte[] checkData = new byte[frameBuffer.position() - HEAD.length];
            frameBuffer.position(HEAD.length);
            frameBuffer.get(checkData);
            short crcCode = getCrcCode(checkData);
            frameBuffer.putShort(crcCode);

            byte[] item = new byte[contentLength + SIZE_FRAME_CONTROL];
            frameBuffer.flip();
            frameBuffer.get(item);
            frameBuffer.clear();

            result.add(item);
        }

        return result;
    }

    /**
     * 负责解析消息帧，如果消息包含多帧，还要进行消息组装
     * @param data
     * @return 返回帧的内容信息，不包含帧结构信息；当一条消息由多帧组成时，只有接收处理完所有的帧才返回相应内容，否则返回null;
     *         最后3个字节为附加字节，倒数第一个字节填cmdID， 倒数第二个字节填是否加密（0或1）,倒数第三个字节填加密类型(0或1)
     */
    public byte[] handleReceive(byte[] data) {
        if (!check(data)) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(HEAD.length);
        byte msgLength = buffer.get();
        byte control = buffer.get();
        byte isEncrypt = (byte) ((control & 0x80) >>> 7);
        byte encryptType = (byte) ((control & 0x40) >>> 6);
        int frameNum = control & 0x1F;
        byte cmdID = buffer.get();

        if (cmdID != currentCmdID) {
            currentCmdID = cmdID;
            receivedByteCount = 0;
            msgBuffer = new byte[SIZE_MSG_BUFFER];
        }

        int contentLength = data.length - SIZE_FRAME_CONTROL;
        System.arraycopy(data, 5, msgBuffer, frameNum * SIZE_FRAME_CONTENT, contentLength);
        receivedByteCount += contentLength;

        if (receivedByteCount != msgLength) {
            return null;
        }

        byte[] result = new byte[msgLength + 3];
        System.arraycopy(msgBuffer, 0, result, 0, msgLength);
        result[result.length - 1] = cmdID;
        result[result.length - 2] = isEncrypt;
        result[result.length - 3] = encryptType;
        currentCmdID = -1;

        return result;
    }

    private short getCrcCode(byte[] data) {
        CrcCheck check = new CrcCheck(16, 0x1021, false, 0, 0x00, 0);
        return (short) check.CountCheckAllCode(data);
    }

    private static boolean crc(byte[] data, int crcCode) {

        CrcCheck check = new CrcCheck(16, 0x1021, false, 0, 0x00, 0);
        return crcCode == check.CountCheckAllCode(data);
    }

    private static boolean check(byte[] data) {

        if (data == null) {
            return false;
        }

        if (data.length < SIZE_FRAME_CONTROL) {
            System.out.println("数据长度小于数据包最小长度");
            return false;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        byte[] head = new byte[HEAD.length];
        byteBuffer.get(head);
        if (!Arrays.equals(head, HEAD)) {
            System.out.println("消息头错误");
            return false;
        }

        int bodyLength = data.length - HEAD.length - 2;
        byte[] body = new byte[bodyLength];
        byteBuffer.get(body);

        int crcCode = byteBuffer.getShort() & 0xFFFF;
        if (!crc(body, crcCode)) {
            System.out.println("crc检验失败");
            return false;
        }

        return true;
    }

    public static void setFrameSize(int size) {
        SIZE_FRAME = size;
        SIZE_FRAME_CONTENT = SIZE_FRAME - SIZE_FRAME_CONTROL;
    }
}
