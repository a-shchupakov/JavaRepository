package commands.instances;

import commands.ICommandPacket;

public class EmptyPacket implements ICommandPacket {
    public static final EmptyPacket INSTANCE = new EmptyPacket();
}
