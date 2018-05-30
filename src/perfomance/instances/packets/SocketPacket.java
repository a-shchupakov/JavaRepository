package perfomance.instances.packets;

import perfomance.ICommandPacket;

public class SocketPacket implements ICommandPacket {
    public int socketPort;

    private SocketPacket() {}

    public SocketPacket(int port){
        socketPort = port;
    }
}
