package utils;

public interface IEncryptor {
    byte[] encrypt(byte[] input);
    byte[] decrypt(byte[] input);
}
