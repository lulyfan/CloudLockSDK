package ut.com.cloudlocklib.encrypt;

public interface IEncrypt {
    byte[] encrypt(byte[] src);
    byte[] decrypt(byte[] src);
}
