package utils.encrypt;

public interface IEncryptor {
    byte[] encrypt(byte[] input);
    byte[] decrypt(byte[] input);
}
