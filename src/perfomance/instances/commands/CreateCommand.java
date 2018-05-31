package perfomance.instances.commands;

import perfomance.ICommand;

public class CreateCommand implements ICommand {
    private final String toCreate;

    public CreateCommand(String names){
        toCreate = names;
    }

    @Override
    public void execute() {

    }

    public String getToCreate(){
        return toCreate;
    }
}
