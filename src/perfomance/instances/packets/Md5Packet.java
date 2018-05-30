package perfomance.instances.packets;

import perfomance.ICommandPacket;

public class Md5Packet implements ICommandPacket {
    public String type;
    public byte[][] md5Bytes;
    public String[] names;

    private Md5Packet(){
    }

    public Md5Packet(String type, String[] names, byte[][] md5Bytes){
        this.names = names;
        this.md5Bytes = md5Bytes;
        this.type = type;
    }
}
