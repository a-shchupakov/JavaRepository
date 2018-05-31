package perfomance.instances.packets;

import perfomance.ICommandPacket;

public class EncryptionPacket implements ICommandPacket {
    public byte[] secret;
    public String type;

    private EncryptionPacket() {}

    public EncryptionPacket(byte[] secret, String type){
        this.secret = secret;
        this.type = type;
    }
}
