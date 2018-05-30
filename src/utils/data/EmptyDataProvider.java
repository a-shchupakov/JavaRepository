package utils.data;

import javafx.util.Pair;

import java.io.IOException;
import java.util.List;

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
    public void createDirectory(String name) {

    }

    @Override
    public String getCurrentRoot() {
        return null;
    }

    @Override
    public void setCurrentRoot(String currentRoot) {

    }

    @Override
    public String resolve(String root, String name) {
        return null;
    }

    @Override
    public String getOrigin() {
        return null;
    }

    @Override
    public void setOrigin(String origin) {

    }

    @Override
    public List<Pair<String, byte[]>> walkThrough(String dir) throws IOException {
        return null;
    }
}
