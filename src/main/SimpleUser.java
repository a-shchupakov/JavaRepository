package main;

import commands.ICommand;
import commands.ICommandPacket;
import commands.ICommandProcessor;
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
    public void process(ICommand command) {
        System.out.println("Executing command on user " + id);
        command.execute();
    }

    @Override
    public ICommandPacket createPacket() {
        return new SimpleCommandPacket(id);
    }
}
