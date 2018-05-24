package commands.instances;

import commands.ICommandPacket;

public class ResponsePacket implements ICommandPacket {
    public final int error;
    public final String errorInfo;
    public final ICommandPacket packet;

    private ResponsePacket(){
        error = 0;
        errorInfo = null;
        packet = null;
    }

    public ResponsePacket(int error, String errorInfo, ICommandPacket packet){
        this.errorInfo = errorInfo;
        this.error = error;
        this.packet = packet;
    }
}
