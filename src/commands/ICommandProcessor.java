package commands;

public interface ICommandProcessor {
    ICommandPacket process(ICommand command);
    ICommandPacket createPacket();
}
