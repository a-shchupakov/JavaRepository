package perfomance.instances.packets;

import perfomance.ICommandPacket;

public class ResponsePacket implements ICommandPacket {
    public final int error;
    public final String errorInfo;

    private ResponsePacket(){
        error = 0;
        errorInfo = null;
    }

    public ResponsePacket(int error, String errorInfo){
        this.errorInfo = errorInfo;
        this.error = error;
    }
}
