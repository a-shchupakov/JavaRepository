package perfomance.instances.packets;

import perfomance.ICommandPacket;

public class InfoPacket implements ICommandPacket {
    public final String info;

    private InfoPacket(){
        this.info = null;
    }

    public InfoPacket(String info){
        this.info = info;
    }
}
