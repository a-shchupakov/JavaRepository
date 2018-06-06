package utils.data;

import java.io.*;
import java.nio.ByteBuffer;

public class NetDataTransporter implements IDataTransporter {
    private InputStream reader;
    private OutputStream writer;
    private ByteArrayOutputStream tempStream;
    private int byteCount;

    public NetDataTransporter(){
        reader = null;
        writer = null;
    }

    public NetDataTransporter(InputStream inputStream, OutputStream outputStream){
        reader = inputStream;
        writer = outputStream;
        byteCount = 4096;
    }

    public void setReader(Object reader) {
        this.reader = (InputStream) reader;
    }

    public void setWriter(Object writer) {
        this.writer = (OutputStream) writer;
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
        return bytes;
    }

    @Override
    public void send(byte[] bytes) throws TransporterException {
        try {
            write(bytes);
        }
        catch (IOException e){
            close();
            throw new TransporterException(e.getMessage());
        }
    }

    private byte[] read() throws IOException {
        tempStream = new ByteArrayOutputStream();
        int sum = 0;
        int count;
        byte[] buffer = new byte[byteCount];
        count = reader.read(buffer);
        if (count <= 0)
            return new byte[0];
        sum += (count - 4);
        int dataLength = ByteBuffer.wrap(buffer, 0, 4).getInt();
        tempStream.write(buffer, 4, count - 4);
        while (sum < dataLength){
            count = reader.read(buffer);
            sum += count;
            tempStream.write(buffer, 0, count);
        }
        byte[] bytes = tempStream.toByteArray();
        closeStream(tempStream);
        return bytes;
    }

    private void write(byte[] bytes) throws IOException{
        byte[] dataLength = ByteBuffer.allocate(4).putInt(bytes.length).array();
        byte[] packet = new byte[dataLength.length + bytes.length];
        System.arraycopy(dataLength, 0, packet, 0, dataLength.length);
        System.arraycopy(bytes, 0, packet, dataLength.length, bytes.length);
        writer.write(packet);
        writer.flush();
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
