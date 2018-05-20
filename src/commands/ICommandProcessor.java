package commands;

public interface ICommandProcessor {
    void process(ICommand command);
    ICommandPacket createPacket();
}
