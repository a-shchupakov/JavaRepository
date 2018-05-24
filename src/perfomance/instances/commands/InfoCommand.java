package perfomance.instances.commands;

import perfomance.ICommand;

import java.io.PrintStream;

public class InfoCommand implements ICommand {
    private PrintStream stream;
    private final String message;

    public InfoCommand(String message){
        this.message = message;
    }

    public void setStream(PrintStream stream) {
        this.stream = stream;
    }

    @Override
    public void execute() {
        if (stream == null)
            return;
        stream.println(message);
    }
}
