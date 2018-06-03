package perfomance.instances.commands;

import perfomance.ICommand;

public class LogCommand implements ICommand {
    private final String type;

    private LogCommand() {
        type = null;
    }

    public String getType() {
        return type;
    }

    public LogCommand(String type){
        this.type = type;
    }
    @Override
    public void execute() {

    }
}
