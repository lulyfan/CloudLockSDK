package ut.com.cloudlocksdk;

import java.io.Serializable;

public class LockInfo implements Serializable {
    public String address;
    public byte[] adminPW;
    public byte[] openLockPW;
    public byte[] key;
    public int encrypt;
    public int deviceId;
}
