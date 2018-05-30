package managment;

import perfomance.CommandFactory;
import perfomance.ICommand;
import perfomance.ICommandPacket;
import perfomance.ICommandProcessor;
import utils.data.IDataTransporter;
import utils.data.TransporterException;
import utils.serializers.ISerializer;

public class Manager {
    private ISerializer serializer;
    private ICommandProcessor commandProcessor;
    private final CommandFactory factory;
    private final IDataTransporter dataTransporter;

    public Manager(ISerializer serializer, IDataTransporter dataTransporter, CommandFactory factory){
        this.serializer = serializer;
        this.factory = factory;
        this.dataTransporter = dataTransporter;
    }

    public void setCommandProcessor(ICommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }

    public ICommand getCommand() throws TransporterException{
        byte[] serializedData = dataTransporter.get();
        ICommandPacket packet = (ICommandPacket) serializer.deserialize(serializedData);
        return factory.createCommand(packet);
    }

    public void sendPacket(ICommandPacket packet) throws TransporterException {
        byte[] serializedData = serializer.serialize(packet);
        dataTransporter.send(serializedData);
    }
}
