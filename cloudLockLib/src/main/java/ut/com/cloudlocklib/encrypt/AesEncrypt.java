package ut.com.cloudlocklib.encrypt;

public class AesEncrypt implements IEncrypt {

    private byte[] key;

    public AesEncrypt(byte[] key) {
        this.key = key;
    }

    @Override
    public byte[] encrypt(byte[] src) {
        return AES.encrypt(src, key);
    }

    @Override
    public byte[] decrypt(byte[] src) {
        return AES.decrypt(src, key);
    }
}
