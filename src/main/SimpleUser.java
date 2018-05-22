package main;

import commands.ICommand;
import commands.ICommandPacket;
import commands.ICommandProcessor;
import commands.instances.EmptyCommandPacket;
import commands.instances.InfoCommand;
import commands.instances.InfoCommandPacket;
import managment.Manager;

public class SimpleUser implements ICommandProcessor {
    private final Manager manager;
    private final int id;

    private SimpleUser() {
        manager = null;
        id = 0;
    }

    public SimpleUser(int id, Manager manager){
        this.id = id;
        this.manager = manager;
    }

    @Override
    public ICommandPacket process(ICommand command) {
        ICommandPacket response = null;
        System.out.println("Executing command on user " + id);
        if (command instanceof InfoCommand) {
            InfoCommand infoCommand = (InfoCommand) command;
            infoCommand.setStream(System.out);
        }
        command.execute();

        if (response == null)
            response = EmptyCommandPacket.INSTANCE;
        return response;
    }

    @Override
    public ICommandPacket createPacket() {
        return new InfoCommandPacket("Hello from user " + id);
    }
}
