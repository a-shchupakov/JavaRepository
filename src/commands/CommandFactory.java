package commands;

import commands.instances.*;

public class CommandFactory {
    public ICommand createCommand(ICommandPacket packet){
        ICommand command = null;
        if (packet instanceof InfoCommandPacket)
            command = new InfoCommand(((InfoCommandPacket) packet).info);
        else if (packet instanceof EmptyCommandPacket)
            command = EmptyCommand.INSTANCE;
        else if (packet instanceof DeleteCommandPacket)
            command = new DeleteCommand(((DeleteCommandPacket) packet).toDelete);
        else if (packet instanceof Md5CommandPacket)
            command = new Md5Command(((Md5CommandPacket) packet).md5Bytes);
        if (command == null)
            command = EmptyCommand.INSTANCE;
        return command;
    }
}
