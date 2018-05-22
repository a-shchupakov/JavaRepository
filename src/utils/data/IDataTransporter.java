package utils.data;

public interface IDataTransporter {
    byte[] get();
    void send(byte[] bytes);
    void setAnotherTransporter(Object transporter);
}
