package utils.data;

import utils.encrypt.IEncryptor;

import java.io.*;

public class NetDataTransporter implements IDataTransporter {
    private final InputStream reader;
    private final OutputStream writer;
    private ByteArrayOutputStream tempStream;
    private int byteCount;
    private final IEncryptor encryptor;

    public NetDataTransporter(IEncryptor encryptor, InputStream inputStream, OutputStream outputStream){
        this.encryptor = encryptor;
        reader = inputStream;
        writer = outputStream;
        byteCount = 4096;
    }

    @Override
    public byte[] get() throws TransporterException {
        byte[] bytes;
        try {
            bytes = read();
        }
        catch (IOException e){
            close();
            throw new TransporterException(e.getMessage());
        }
        return decrypt(bytes);
    }

    @Override
    public void send(byte[] bytes) throws TransporterException {
        try {
            write(encrypt(bytes));
        }
        catch (IOException e){
            close();
            throw new TransporterException(e.getMessage());
        }
    }

    public void setByteCount(int byteCount) {
        this.byteCount = byteCount;
    }

    private byte[] encrypt(byte[] input) {
        return encryptor.encrypt(input);
    }

    private byte[] decrypt(byte[] input) {
        return encryptor.decrypt(input);
    }

    private byte[] read() throws IOException {
        tempStream = new ByteArrayOutputStream();
        int count;
        byte[] buffer = new byte[byteCount];
        count = reader.read(buffer); // TODO: возможно придется считывать несколько раз (read - блокирующий)
        if (count > 0) {
            tempStream.write(buffer, 0, count);
            byte[] bytes = tempStream.toByteArray();
            closeStream(tempStream);
            return bytes;
        }
        else
            closeStream(tempStream);
        return new byte[0];
    }

    private void write(byte[] bytes) throws IOException{
        writer.write(bytes);
    }

    public void close(){
        closeStream(reader);
        closeStream(writer);
        closeStream(tempStream);
    }

    private static void closeStream(Closeable stream) {
        if (stream != null)
        {
            try
            {
                stream.close();
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }
}
