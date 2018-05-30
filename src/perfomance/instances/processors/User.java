package perfomance.instances.processors;

import managment.Manager;
import perfomance.ICommand;
import perfomance.ICommandPacket;
import perfomance.ICommandProcessor;
import perfomance.instances.packets.*;
import utils.data.IDataProvider;
import utils.data.TransporterException;


@SuppressWarnings("Duplicates")
public class User implements ICommandProcessor {
    private final Manager manager;
    private IDataProvider dataProvider;

    public User(Manager manager, IDataProvider dataProvider){
        this.manager = manager;
        this.dataProvider = dataProvider;
    }

    @Override
    public ICommandPacket process(ICommand command) { //TODO: finish command processing
        if (command instanceof EmptyPacket)
            return null;

        return null;
    }

    @Override
    public void sendPacket(String identifier) {
        String[] command = identifier.split(" ");
        ICommandPacket packet = null;
        if (identifier.toLowerCase().startsWith("add")){
            packet = sendAddPacket(command);
        }
        else if (identifier.toLowerCase().startsWith("clone")){
            packet = sendClonePacket(command);
        }
        else if (identifier.toLowerCase().startsWith("update")){
            packet = sendUpdatePacket(command);
        }
        else if (identifier.toLowerCase().startsWith("commit")){
            packet = sendCommitPacket(command);
        }
        else if (identifier.toLowerCase().startsWith("revert")){
            packet = sendRevertPacket(command);
        }
        else if (identifier.toLowerCase().startsWith("log")){
            packet = sendLogPacket(command);
        }
        try {
            send(packet);
        } catch (TransporterException e) {
            e.printStackTrace();
        }
    }

    private ICommandPacket sendAddPacket(String[] command){
        return new CreatePacket(command[1]);
    }

    private ICommandPacket sendClonePacket(String[] command){
        boolean straight = false;
        if (command.length == 4) {
            if (command[3].equals("."))
                straight = true;
            else
                return null;
        }
        String path = command[1];
        String name = command[2];
        if (straight)
            dataProvider.setOrigin(path);
        else
            dataProvider.setOrigin(dataProvider.resolve(path, name));
        return new ClonePacket(name);
    }

    private ICommandPacket sendCommitPacket(String[] command){
        return new Md5Packet("query", null, null);
    }

    private ICommandPacket sendUpdatePacket(String[] command){
        return new RevertPacket("", true);
    }

    private ICommandPacket sendRevertPacket(String[] command){
        String version = command[1];
        boolean hard = false;
        if (command.length == 3)
            if (command[2].equals("-hard"))
                hard = true;
        return new RevertPacket(version, hard);
    }

    private ICommandPacket sendLogPacket(String[] command){
        return null;
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
