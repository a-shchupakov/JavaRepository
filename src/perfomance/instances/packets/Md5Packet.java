package perfomance.instances.packets;

import perfomance.ICommandPacket;

public class Md5Packet implements ICommandPacket {
    public final byte[] md5Bytes;

    private Md5Packet(){
        this.md5Bytes = null;
    }

    public Md5Packet(byte[] md5Bytes){
        this.md5Bytes = md5Bytes;
    }
}
