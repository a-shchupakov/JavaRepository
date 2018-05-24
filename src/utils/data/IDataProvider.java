package utils.data;

public interface IDataProvider {
    byte[] read(String name);
    void write(String name, byte[] bytes);
    void delete(String name);
    String resolvePath(String main, String other);
}
