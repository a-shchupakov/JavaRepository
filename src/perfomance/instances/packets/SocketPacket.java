package perfomance.instances.packets;

import perfomance.ICommandPacket;

public class SocketPacket implements ICommandPacket {
    public int socketPort;
    public String type;

    private SocketPacket() {}

    public SocketPacket(int port, String type){
        socketPort = port;
        this.type = type;
    }
}
