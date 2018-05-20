package commands;

public abstract class CommandFactory {
    public abstract ICommand create_command(ICommandPacket packet);
}
