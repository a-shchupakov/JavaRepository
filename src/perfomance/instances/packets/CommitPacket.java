package perfomance.instances.packets;

import perfomance.ICommandPacket;

public class CommitPacket implements ICommandPacket {
    public String[] files;

    private CommitPacket(){}

    public CommitPacket(String[] files){
        this.files = files;
    }
}
