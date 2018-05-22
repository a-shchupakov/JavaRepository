package main;

import utils.data.EncryptingDataTransporter;
import utils.encrypt.IEncryptor;

public class StraightEncryptingDataTransporter extends EncryptingDataTransporter {
    private StraightEncryptingDataTransporter anotherTransporter;
    private byte[] data = null;
    private final IEncryptor encryptor;

    public StraightEncryptingDataTransporter(IEncryptor encryptor){
        this.encryptor = encryptor;
    }

    public void setAnotherTransporter(Object anotherTransporter) {
        this.anotherTransporter = (StraightEncryptingDataTransporter) anotherTransporter;
    }

    private void setData(byte[] data) {
        this.data = data;
    }

    protected byte[] getRaw() {
        byte[] tempData = data;
        data = null;
        return tempData;
    }

    protected void sendRaw(byte[] bytes) {
        anotherTransporter.setData(bytes);
    }

    @Override
    protected byte[] encrypt(byte[] input) {
        return encryptor.encrypt(input);
    }

    @Override
    protected byte[] decrypt(byte[] input) {
        return encryptor.decrypt(input);
    }
}
