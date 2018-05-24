package utils.data;

public class EmptyDataProvider implements IDataProvider {
    public static final EmptyDataProvider INSTANCE = new EmptyDataProvider();

    @Override
    public byte[] read(String name) {
        return new byte[0];
    }

    @Override
    public void write(String name, byte[] bytes) {

    }

    @Override
    public void delete(String name) {

    }

    @Override
    public String resolvePath(String main, String other) {
        return "";
    }
}
