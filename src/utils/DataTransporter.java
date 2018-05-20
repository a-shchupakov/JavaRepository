package utils;


public abstract class DataTransporter {
    public final byte[] getData(){
        return decrypt(get());
    }

    public final void sendData(byte[] input){
        send(encrypt(input));
    }

    protected abstract byte[] get();
    protected abstract void send(byte[] bytes);

    protected abstract byte[] encrypt(byte[] input);
    protected abstract byte[] decrypt(byte[] input);

    public abstract void setAnotherTransporter(DataTransporter transporter);
}
