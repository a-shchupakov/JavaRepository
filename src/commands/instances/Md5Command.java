package commands.instances;

import commands.ICommand;

public class Md5Command implements ICommand {
    public final byte[] md5Bytes;


    public Md5Command(byte[] md5Bytes){
        this.md5Bytes = md5Bytes;
    }
    @Override
    public void execute() {

    }
}
