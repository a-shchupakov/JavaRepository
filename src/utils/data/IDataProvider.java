package utils.data;

import javafx.util.Pair;

import java.io.IOException;
import java.util.List;

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
    List<Pair<String, byte[]>> walkThrough(String dir) throws IOException;
    void clearDirectory(String dir);
    void append(String name, byte[] bytes) throws IOException;
}
