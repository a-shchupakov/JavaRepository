package perfomance.instances.commands;

import perfomance.ICommand;

public class Md5Command implements ICommand {
    private final byte[][] md5Bytes;
    private final String[] names;
    private final String type;

    public Md5Command(String type, String[] names, byte[][] md5Bytes){
        this.type = type;
        this.md5Bytes = md5Bytes;
        this.names = names;
    }
    @Override
    public void execute() {

    }

    public String[] getNames() {
        return names;
    }

    public String getType() {
        return type;
    }

    public byte[][] getMd5Bytes() {
        return md5Bytes;
    }
}
