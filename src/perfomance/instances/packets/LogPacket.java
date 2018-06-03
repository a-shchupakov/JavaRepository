package perfomance.instances.packets;

import perfomance.ICommandPacket;

public class LogPacket implements ICommandPacket {
    public String type;

    private LogPacket() {}

    public LogPacket(String type){
        this.type = type;
    }
}
