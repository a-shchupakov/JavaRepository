package perfomance.instances.commands;

import perfomance.ICommand;

public class CloneCommand implements ICommand {
    private final String toClone;

    public CloneCommand(String names){
        toClone = names;
    }

    @Override
    public void execute() {

    }

    public String getToClone(){
        return toClone;
    }
}
