package utils;

public class XorEncryptor implements IEncryptor {
    private final byte[] secret;

    private XorEncryptor(){
        secret = null;
    }

    public XorEncryptor(byte[] key){
        this.secret = key;
    }
    @Override
    public byte[] encrypt(byte[] input) {
        return xor(input);
    }

    @Override
    public byte[] decrypt(byte[] input) {
        return xor(input);
    }

    private byte[] xor(final byte[] input) {
        final byte[] output = new byte[input.length];
        if (secret == null || secret.length == 0) {
            throw new IllegalArgumentException("Empty security key");
        }
        int sPos = 0;
        for (int pos = 0; pos < input.length; ++pos) {
            output[pos] = (byte) (input[pos] ^ secret[sPos]);
            ++sPos;
            if (sPos >= secret.length) {
                sPos = 0;
            }
        }
        return output;
    }
}
