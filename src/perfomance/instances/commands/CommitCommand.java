package perfomance.instances.commands;

import perfomance.ICommand;

public class CommitCommand implements ICommand {
    private final String[] files;

    public CommitCommand(String[] files){
        this.files = files;
    }

    public String[] getFiles() {
        return files;
    }

    @Override
    public void execute() {

    }
}
