package main;

import commands.ICommand;
import commands.ICommandPacket;
import commands.ICommandProcessor;
import commands.instances.*;
import managment.Manager;
import utils.data.FolderProvider;
import utils.data.IDataProvider;

import java.io.IOException;

public class SimpleUser implements ICommandProcessor {
    private final Manager manager;
    private final String id;
    private IDataProvider dataProvider;

    public SimpleUser(String root, String id, Manager manager){
        this.id = id;
        this.manager = manager;
        this.dataProvider = new FolderProvider(root);
    }

    @Override
    public ICommandPacket process(ICommand command) {
        ICommandPacket response = null;
        System.out.println("Executing command on CommandProcessor with id: " + id);
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
        else if (command instanceof DataCommand){
            DataCommand dataCommand = (DataCommand) command;
            dataCommand.setDataProvider(dataProvider);
            System.out.println("Executing DataCommand on CommandProcessor with id: " + id);
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

    public ICommandPacket createWritePacket(String name){
        byte[] bytes;
        try {
            bytes = dataProvider.read(name);
        } catch (IOException e) {
            e.printStackTrace();
            return EmptyPacket.INSTANCE;
        }
        return new WritePacket(new String[] {name}, new byte[][] {bytes});
    }

    public ICommandPacket createDeletePacket(String name){
        return new DeletePacket(name);
    }
}
