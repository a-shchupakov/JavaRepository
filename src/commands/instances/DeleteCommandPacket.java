package commands.instances;

import commands.ICommandPacket;

public class DeleteCommandPacket implements ICommandPacket {
    public final String toDelete;
    private DeleteCommandPacket(){
        this.toDelete = null;
    }

    public DeleteCommandPacket(String name){
        this.toDelete = name;
    }
}
