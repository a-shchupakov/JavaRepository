package perfomance;

import perfomance.instances.commands.*;
import perfomance.instances.packets.*;

public class CommandFactory {
    public ICommand createCommand(ICommandPacket packet){
        ICommand command = null;
        if (packet instanceof InfoPacket)
            command = createInfoCommand(packet);
        else if (packet instanceof EmptyPacket)
            command = createEmptyCommand(packet);
        else if (packet instanceof Md5Packet)
            command = createMd5Command(packet);
        else if (packet instanceof ResponsePacket)
            command = createResponseCommand(packet);
        else if (packet instanceof CommitPacket)
            command = new CommitCommand(((CommitPacket) packet).files);
        else if (packet instanceof SocketPacket)
            command = new SocketCommand(((SocketPacket) packet).socketPort);
        else if (packet instanceof RevertPacket)
            command = new RevertCommand(((RevertPacket) packet).version, ((RevertPacket) packet).hard);
        else if (packet instanceof EncryptionPacket)
            command = new EncryptionCommand(((EncryptionPacket) packet).secret, ((EncryptionPacket) packet).type);
        if (command == null)
            command = EmptyCommand.INSTANCE;
        return command;
    }

    private InfoCommand createInfoCommand(ICommandPacket packet){
        return new InfoCommand(((InfoPacket) packet).info);
    }

    private EmptyCommand createEmptyCommand(ICommandPacket packet){
        return EmptyCommand.INSTANCE;
    }

    private Md5Command createMd5Command(ICommandPacket packet){
        return new Md5Command(((Md5Packet) packet).md5Bytes);
    }

    private ResponseCommand createResponseCommand(ICommandPacket packet){
        ResponsePacket responsePacket = (ResponsePacket) packet;
        return new ResponseCommand(responsePacket.error, responsePacket.errorInfo);
    }
}
