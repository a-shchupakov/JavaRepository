package perfomance.instances.packets;

import perfomance.ICommandPacket;

public class DeletePacket implements ICommandPacket {
    public final String[] toDelete;
    private DeletePacket(){
        this.toDelete = null;
    }

    public DeletePacket(String[] name){
        this.toDelete = name;
    }
}
