package utils.data;


public abstract class EncryptingDataTransporter implements IDataTransporter {
    public final byte[] get(){
        return decrypt(getRaw());
    }
    public final void send(byte[] input){
        sendRaw(encrypt(input));
    }

    protected abstract byte[] getRaw();
    protected abstract void sendRaw(byte[] bytes);

    protected abstract byte[] encrypt(byte[] input);
    protected abstract byte[] decrypt(byte[] input);

}
