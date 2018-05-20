package utils;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Serializer implements ISerializer {
    // У сериализуемого объекта должен быть пустой конструктор

    @Override
    public Object deserialize(byte[] data) {
        data = Archiver.dearchive(data);
        if (data == null) {
            System.out.println("Cannot unzip bytes");
            return null;
        }
        Properties properties = new Properties();
        InputStream inputStream = new ByteArrayInputStream(data);
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            System.out.println("An error occurred while loading properties from byte array: " + e.getMessage());
        }
        Object object = ClassToPropertiesConverter.classFromProperties(properties);
        try {
            inputStream.close();
        } catch (IOException e) { }
        return object;
    }

    @Override
    public byte[] serialize(Object object) {
        Properties properties = ClassToPropertiesConverter.classToProperties(object);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            properties.store(byteArrayOutputStream, "");
        }
        catch (IOException e){
            System.out.println("An error occurred while storing properties to a byte array: " + e.getMessage());
        }
        byte[] byteObject = byteArrayOutputStream.toByteArray();
        try {
            byteArrayOutputStream.close();
        } catch (IOException e) { }
        return Archiver.archive(byteObject, "");
    }
}
