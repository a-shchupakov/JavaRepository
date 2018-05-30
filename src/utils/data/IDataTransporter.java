package utils.data;

public interface IDataTransporter {
    byte[] get() throws TransporterException;
    void send(byte[] bytes) throws TransporterException;
    void setSecret(Object secret);
}
