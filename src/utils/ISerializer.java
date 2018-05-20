package utils;

public interface ISerializer {
    Object deserialize(byte[] data);
    byte[] serialize(Object object);
}
