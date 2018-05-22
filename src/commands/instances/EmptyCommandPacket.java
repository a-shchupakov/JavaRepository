package commands.instances;

import commands.ICommandPacket;

public class EmptyCommandPacket implements ICommandPacket {
    public static final EmptyCommandPacket INSTANCE = new EmptyCommandPacket();
}
