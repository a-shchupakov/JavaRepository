package main;

import utils.DataTransporter;
import utils.IEncryptor;
import utils.XorEncryptor;

public class StraightDataTransporter extends DataTransporter {
    private StraightDataTransporter anotherTransporter;
    private byte[] data = null;
    private final IEncryptor encryptor;

    public StraightDataTransporter(IEncryptor encryptor){
        this.encryptor = encryptor;
    }

    public void setAnotherTransporter(DataTransporter anotherTransporter) {
        this.anotherTransporter = (StraightDataTransporter) anotherTransporter;
    }

    public DataTransporter getAnotherTransporter() {
        return anotherTransporter;
    }

    private void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] get() {
        byte[] tempData = data;
        data = null;
        return tempData;
    }

    @Override
    public void send(byte[] bytes) {
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
