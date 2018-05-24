package commands.instances;

import commands.ICommandPacket;

public class WritePacket implements ICommandPacket {
    public final byte[][] bytes;

    private WritePacket(){
        bytes = null;
    }
    public WritePacket(byte[][] bytes){
        this.bytes = bytes;
    }
}
