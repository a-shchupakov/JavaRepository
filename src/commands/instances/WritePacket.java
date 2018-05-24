package commands.instances;

import commands.ICommandPacket;

public class WritePacket implements ICommandPacket {
    public final String[] names;
    public final byte[][] bytes;

    private WritePacket(){
        names = null;
        bytes = null;
    }
    public WritePacket(String[] names, byte[][] bytes){
        this.bytes = bytes;
        this.names = names;
    }
}
