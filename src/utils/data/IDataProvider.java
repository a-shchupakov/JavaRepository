package utils.data;

public interface IDataProvider {
    byte[] read(String name);
    void write(String name, byte[] bytes);
    void delete(String name);
}
