package perfomance.instances.packets;

import perfomance.ICommandPacket;

public class RevertPacket implements ICommandPacket {
    public String version;
    public boolean hard;

    private RevertPacket(){ }

    public RevertPacket(String version, boolean hard){
        this.version = version;
        this.hard = hard;
    }
}
