package utils.serializers;

public interface ISerializer {
    Object deserialize(byte[] data);
    byte[] serialize(Object object);
}
