package perfomance.instances.commands;

import perfomance.ICommand;

public class EncryptionCommand implements ICommand {
    private final byte[] secret;
    private final String type;

    public EncryptionCommand(byte[] secret, String type){
        this.secret = secret;
        this.type = type;
    }

    public byte[] getSecret() {
        return secret;
    }

    public String getType() {
        return type;
    }

    @Override
    public void execute() {

    }
}
