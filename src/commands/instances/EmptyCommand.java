package commands.instances;

import commands.ICommand;

public class EmptyCommand implements ICommand {
    public static final EmptyCommand INSTANCE = new EmptyCommand();

    @Override
    public void execute() {

    }
}
