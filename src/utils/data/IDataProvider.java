package utils.data;

import java.io.IOException;

public interface IDataProvider {
    byte[] read(String name) throws IOException;
    void write(String name, byte[] bytes) throws IOException;
    void delete(String name) throws IOException;
    void createDirectory(String name);
    String getCurrentRoot();
    void setCurrentRoot(String currentRoot);
    String resolve(String root, String name);
    String getOrigin();
    void setOrigin(String origin);
}
