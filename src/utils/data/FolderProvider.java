package utils.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class FolderProvider implements IDataProvider {
    private String root;
    private Path path;

    public FolderProvider(String root){
        this.root = root;
        path = Paths.get(root);
    }

    public void setRoot(String root) {
        this.root = root;
        path = Paths.get(root);
    }

    public String getRoot() {
        return root;
    }

    @Override
    public byte[] read(String name) throws IOException {
        Path pathToFile = path.resolve(name);
        return Files.readAllBytes(pathToFile);
    }

    @Override
    public void write(String name, byte[] bytes) throws IOException {
        Path pathToFile = path.resolve(name);
        Files.write(pathToFile, bytes);
    }

    @Override
    public void delete(String name) throws IOException {
        Path pathToFile = path.resolve(name);
        Files.deleteIfExists(pathToFile);
    }
}
