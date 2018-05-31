package perfomance.instances.commands;

import perfomance.ICommand;

public class RevertCommand implements ICommand {
    private final String version;
    private final boolean hard;

    public RevertCommand(String version, boolean hard){
        this.version = version;
        this.hard = hard;
    }

    public String getVersion() {
        return version;
    }

    public boolean isHard() {
        return hard;
    }

    @Override
    public void execute() {

    }
}
