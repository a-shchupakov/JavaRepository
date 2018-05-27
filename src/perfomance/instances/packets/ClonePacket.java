package perfomance.instances.packets;

import perfomance.ICommandPacket;

public class ClonePacket implements ICommandPacket {
    public final String toClone;

    private ClonePacket(){
        this.toClone = null;
    }

    public ClonePacket(String name){
        this.toClone = name;
    }
}
