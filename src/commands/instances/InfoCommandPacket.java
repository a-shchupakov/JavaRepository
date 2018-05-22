package commands.instances;

import commands.ICommandPacket;

public class InfoCommandPacket implements ICommandPacket {
    public final String info;

    private InfoCommandPacket(){
        this.info = null;
    }

    public InfoCommandPacket(String info){
        this.info = info;
    }
}
