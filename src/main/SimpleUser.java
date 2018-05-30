package main;

import perfomance.ICommand;
import perfomance.ICommandPacket;
import perfomance.ICommandProcessor;
import managment.Manager;
import utils.data.FolderProvider;
import utils.data.IDataProvider;
import utils.data.TransporterException;

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
        manager.sendPacket(packet);
    }

    @Override
    public ICommand get() throws TransporterException {
        return manager.getCommand();
    }
}
