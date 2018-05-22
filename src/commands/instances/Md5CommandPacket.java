package commands.instances;

import commands.ICommandPacket;

public class Md5CommandPacket implements ICommandPacket {
    public final byte[] md5Bytes;

    private Md5CommandPacket(){
        this.md5Bytes = null;
    }

    public Md5CommandPacket(byte[] md5Bytes){
        this.md5Bytes = md5Bytes;
    }
}
