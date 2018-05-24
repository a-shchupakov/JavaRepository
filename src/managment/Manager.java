package managment;

import commands.CommandFactory;
import commands.ICommand;
import commands.ICommandPacket;
import commands.ICommandProcessor;
import utils.data.IDataTransporter;
import utils.data.TransporterException;
import utils.serializers.ISerializer;

public class Manager {
    private ISerializer serializer;
    private ICommandProcessor commandProcessor;
    private final CommandFactory factory;
    private final IDataTransporter dataTransporter;

    private Manager(){
        factory = null;
        dataTransporter = null;
    }

    public Manager(ISerializer serializer, IDataTransporter dataTransporter, CommandFactory factory){
        this.serializer = serializer;
        this.factory = factory;
        this.dataTransporter = dataTransporter;
    }

    public void setCommandProcessor(ICommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }

    public ICommandProcessor getCommandProcessor() {
        return commandProcessor;
    }

    private void sendToAnotherManager(ICommandPacket packet) throws TransporterException {
        byte[] serializedData = serializer.serialize(packet);
        dataTransporter.send(serializedData);
    }

    public void getFromAnotherManager() throws TransporterException{
        byte[] serializedData = dataTransporter.get();
        ICommandPacket packet = (ICommandPacket) serializer.deserialize(serializedData);
        ICommand command = factory.createCommand(packet);
        sendToProcessor(command);
    }

    public void sendToAnotherProcessor(ICommandPacket packet) throws TransporterException {
        sendToAnotherManager(packet);
    }

    private void sendToProcessor(ICommand command){
        commandProcessor.process(command);
    }
}
