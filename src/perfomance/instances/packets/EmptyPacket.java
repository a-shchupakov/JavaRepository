package perfomance.instances.packets;

import perfomance.ICommandPacket;

public class EmptyPacket implements ICommandPacket {
    public static final EmptyPacket INSTANCE = new EmptyPacket();
}
