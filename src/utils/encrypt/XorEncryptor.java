package utils.encrypt;

public class XorEncryptor implements IEncryptor {
    private byte[] secret;

    public XorEncryptor(){
        secret = null;
    }

    @Override
    public void setSecret(Object secret) {
        this.secret = (byte[]) secret;
    }

    @Override
    public byte[] getSecret() {
        return secret;
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
            return input;
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
