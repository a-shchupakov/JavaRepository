package perfomance.instances.processors;

import managment.Manager;
import perfomance.ICommand;
import perfomance.ICommandPacket;
import perfomance.ICommandProcessor;
import perfomance.instances.packets.EmptyPacket;
import utils.data.IDataProvider;
import utils.data.TransporterException;

import java.io.IOException;

@SuppressWarnings("Duplicates")
public class User implements ICommandProcessor {
    private final Manager manager;
    private IDataProvider dataProvider;

    public User(Manager manager){
        this.manager = manager;
    }

    public void setDataProvider(IDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public ICommandPacket process(ICommand command) { //TODO: finish command processing
        return null;
    }

    @Override
    public ICommandPacket createPacket(String identifier) {
        return null;
    }

    @Override
    public void send(ICommandPacket packet) throws TransporterException {
        manager.sendToAnotherProcessor(packet);
    }

    @Override
    public ICommandPacket get() throws TransporterException {
        return manager.getFromAnotherManager();
    }

    private ICommandPacket createDeletePacket(String ... names){
        return new DeletePacket(names);
    }

    private ICommandPacket createWritePacket(String ... names){
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
}
