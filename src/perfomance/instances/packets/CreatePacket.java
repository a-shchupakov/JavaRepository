package perfomance.instances.packets;

import perfomance.ICommandPacket;

public class CreatePacket implements ICommandPacket {
    public final String toCreate;
    private CreatePacket(){
        this.toCreate = null;
    }

    public CreatePacket(String name){
        this.toCreate = name;
    }
}
