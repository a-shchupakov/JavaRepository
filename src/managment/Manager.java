package managment;

import commands.CommandFactory;
import commands.ICommand;
import commands.ICommandPacket;
import commands.ICommandProcessor;
import utils.DataTransporter;
import utils.ISerializer;

public class Manager {
    private ISerializer serializer;
    private ICommandProcessor commandProcessor;
    private final CommandFactory factory;
    private final DataTransporter dataTransporter;

    private Manager(){
        factory = null;
        dataTransporter = null;
    }

    public Manager(ISerializer serializer, DataTransporter dataTransporter, CommandFactory factory){
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

    private void sendToAnotherManager(ICommandPacket packet){
        byte[] serializedData = serializer.serialize(packet);
        dataTransporter.sendData(serializedData);
    }

    public void getFromAnotherManager(){
        byte[] serializedData = dataTransporter.getData();
        ICommandPacket packet = (ICommandPacket) serializer.deserialize(serializedData);
        ICommand command = factory.create_command(packet);
        sendToProcessor(command);
    }

    public void sendToAnotherProcessor(ICommandPacket packet){
        sendToAnotherManager(packet);
    }

    private void sendToProcessor(ICommand command){
        commandProcessor.process(command);
    }
}
