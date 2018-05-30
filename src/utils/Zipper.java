package utils;

import javafx.util.Pair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Zipper {
    public static byte[] unzipOne(byte[] input) throws IOException {
        InputStream archivedBytes = new ByteArrayInputStream(input);
        ZipInputStream stream = new ZipInputStream(archivedBytes);
        byte[] buffer = new byte[2048];
        ZipEntry entry = stream.getNextEntry();
        ByteArrayOutputStream output = null;
        output = new ByteArrayOutputStream();
        int len;
        while ((len = stream.read(buffer)) > 0) {
            output.write(buffer, 0, len);
        }
        byte[] data = output.toByteArray();
        output.close();
        return data;
    }

    public static List<Pair<String, byte[]>> unzipMultiple(byte[] input) throws IOException{
        InputStream archivedBytes = new ByteArrayInputStream(input);
        ZipInputStream stream = new ZipInputStream(archivedBytes);
        byte[] buffer = new byte[2048];
        List<Pair<String, byte[]>> entries = new ArrayList<>();
        ZipEntry entry;
        String name;
        ByteArrayOutputStream output;
        while ((entry = stream.getNextEntry()) != null) {
            // Once we getRaw the entry from the stream, the stream is
            // positioned read to read the raw data, and we keep
            // reading until read returns 0 or less.
            name = entry.getName();
            output = new ByteArrayOutputStream();
            int len;
            while ((len = stream.read(buffer)) > 0) {
                output.write(buffer, 0, len);
            }
            entries.add(new Pair<>(name, output.toByteArray()));
            // we must always close the output file
            output.close();
        }
        return entries;
    }

    public static byte[] zipMultiple(String[] names, byte[][] fileContents) throws IOException{
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        ZipEntry entry;
        for (int i = 0; i < names.length; i++) {
            entry = new ZipEntry(names[i]);
            entry.setSize(fileContents[i].length);
            zipOutputStream.putNextEntry(entry);
            zipOutputStream.write(fileContents[i]);
            zipOutputStream.closeEntry();
        }
        zipOutputStream.close();
        byte[] byteObject = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        return byteObject;
    }

    public static byte[] zipOne(byte[] input, String name) throws IOException{
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        ZipEntry entry = new ZipEntry(name);
        entry.setSize(input.length);
        zipOutputStream.putNextEntry(entry);
        zipOutputStream.write(input);
        zipOutputStream.closeEntry();
        zipOutputStream.close();
        byte[] byteObject = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        return byteObject;
    }
}
