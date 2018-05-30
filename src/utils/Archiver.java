package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Archiver {
    public static byte[] dearchiveOne(byte[] input){
        InputStream archivedBytes = new ByteArrayInputStream(input);
        ZipInputStream stream = new ZipInputStream(archivedBytes);
        byte[] buffer = new byte[2048];

        try {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) { // TODO: multiple entries
                // Once we getRaw the entry from the stream, the stream is
                // positioned read to read the raw data, and we keep
                // reading until read returns 0 or less.
                ByteArrayOutputStream output = null;
                try {
                    output = new ByteArrayOutputStream();
                    int len = 0;
                    while ((len = stream.read(buffer)) > 0) {
                        output.write(buffer, 0, len);
                    }
                    return output.toByteArray();
                } finally {
                    // we must always close the output file
                    if (output != null) output.close();
                }
            }
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static byte[] archive(byte[] input, String name){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        ZipEntry entry = new ZipEntry(name);
        entry.setSize(input.length);
        try{
            zipOutputStream.putNextEntry(entry);
            zipOutputStream.write(input);
            zipOutputStream.closeEntry();
            zipOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] byteObject = byteArrayOutputStream.toByteArray();
        try {
            byteArrayOutputStream.close();
        } catch (IOException e) {}
        return byteObject;
    }
}
