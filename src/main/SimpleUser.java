package main;

import commands.ICommand;
import commands.ICommandPacket;
import commands.ICommandProcessor;
import commands.instances.*;
import managment.Manager;

public class SimpleUser implements ICommandProcessor {
    private final Manager manager;
    private final String id;

    private SimpleUser() {
        manager = null;
        id = "";
    }

    public SimpleUser(String id, Manager manager){
        this.id = id;
        this.manager = manager;
    }

    @Override
    public ICommandPacket process(ICommand command) {
        ICommandPacket response = null;
        System.out.println("Executing command on user with id: " + id);
        if (command instanceof InfoCommand) {
            InfoCommand infoCommand = (InfoCommand) command;
            infoCommand.setStream(System.out);
        }
        else if (command instanceof Md5Command)
        {
            Md5Command md5Command = (Md5Command) command;
            for(byte b: md5Command.getMd5Bytes())
                System.out.print(b);
            System.out.println();
        }
        command.execute();

        if (response == null)
            response = EmptyPacket.INSTANCE;
        return response;
    }

    @Override
    public ICommandPacket createPacket() {
        return new InfoPacket("Hello from user with id: " + id);
    }
}
