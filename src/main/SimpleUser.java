package main;

import perfomance.ICommand;
import perfomance.ICommandPacket;
import perfomance.ICommandProcessor;
import perfomance.instances.commands.DataCommand;
import perfomance.instances.commands.InfoCommand;
import perfomance.instances.commands.Md5Command;
import perfomance.instances.packets.DeletePacket;
import perfomance.instances.packets.EmptyPacket;
import perfomance.instances.packets.InfoPacket;
import perfomance.instances.packets.WritePacket;
import managment.Manager;
import utils.data.FolderProvider;
import utils.data.IDataProvider;
import utils.data.TransporterException;

import java.io.IOException;

@SuppressWarnings("Duplicates")
public class SimpleUser implements ICommandProcessor {
    private final Manager manager;
    private final String id;
    private IDataProvider dataProvider;

    public SimpleUser(String root, String id, Manager manager){
        this.id = id;
        this.manager = manager;
        this.dataProvider = new FolderProvider();
        dataProvider.setOrigin(root);
        dataProvider.setCurrentRoot(dataProvider.getOrigin());
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
    public ICommandPacket createPacket(String identifier) {
        return new InfoPacket("Hello from user with id: " + id);
    }

    @Override
    public void send(ICommandPacket packet) throws TransporterException {
        manager.sendToAnotherProcessor(packet);
    }

    @Override
    public ICommandPacket get() throws TransporterException {
        return manager.getFromAnotherManager();
    }

    public ICommandPacket createWritePacket(String ... names){
        byte[][] filesContent = new byte[names.length][];
        for (int i = 0; i < names.length; i++) {
            byte[] data;
            try {
                data = dataProvider.read(names[i]);
            } catch (IOException e) {
                e.printStackTrace();
                return EmptyPacket.INSTANCE;
            }
            filesContent[i] = data;
        }
        return new WritePacket(names, filesContent);
    }

    public ICommandPacket createDeletePacket(String ... names){
        return new DeletePacket(names);
    }
}
