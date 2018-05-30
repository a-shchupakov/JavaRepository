package perfomance;

import perfomance.instances.commands.*;
import perfomance.instances.packets.*;

public class CommandFactory {
    public ICommand createCommand(ICommandPacket packet){
        ICommand command = null;
        if (packet instanceof InfoPacket)
            command = createInfoCommmand(packet);
        else if (packet instanceof EmptyPacket)
            command = createEmptyCommand(packet);
        else if (packet instanceof Md5Packet)
            command = createMd5Command(packet);
        else if (packet instanceof ResponsePacket)
            command = createResponseCommamd(packet);
        if (command == null)
            command = EmptyCommand.INSTANCE;
        return command;
    }

    private InfoCommand createInfoCommmand(ICommandPacket packet){
        return new InfoCommand(((InfoPacket) packet).info);
    }

    private EmptyCommand createEmptyCommand(ICommandPacket packet){
        return EmptyCommand.INSTANCE;
    }

    private Md5Command createMd5Command(ICommandPacket packet){
        return new Md5Command(((Md5Packet) packet).md5Bytes);
    }

    private ResponseCommand createResponseCommamd(ICommandPacket packet){
        ResponsePacket responsePacket = (ResponsePacket) packet;
        DataCommand innerCommand;
        try {
            innerCommand = (DataCommand) createCommand(responsePacket.packet);
        }
        catch (ClassCastException e) {
            innerCommand = EmptyDataCommand.INSTANCE;
        }

        return new ResponseCommand(responsePacket.error, responsePacket.errorInfo, innerCommand);
    }
}
