package main;

import perfomance.ICommand;
import perfomance.ICommandPacket;
import perfomance.ICommandProcessor;
import perfomance.instances.commands.DataCommand;
import perfomance.instances.commands.InfoCommand;
import perfomance.instances.commands.Md5Command;
import perfomance.instances.packets.EmptyPacket;
import perfomance.instances.packets.InfoPacket;
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
        return null;
    }

    @Override
    public void sendPacket(String identifier) {

    }

    @Override
    public void send(ICommandPacket packet) throws TransporterException {
        manager.sendToAnotherProcessor(packet);
    }

    @Override
    public ICommandPacket get() throws TransporterException {
        return manager.getFromAnotherManager();
    }
}
