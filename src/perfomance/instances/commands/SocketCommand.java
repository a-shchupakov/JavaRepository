package perfomance.instances.commands;

import perfomance.ICommand;

public class SocketCommand implements ICommand {
    private final int socketPort;
    private final String type;

    public SocketCommand(int port, String type){
        socketPort = port;
        this.type = type;
    }

    public int getSocketPort() {
        return socketPort;
    }

    public String getType() {
        return type;
    }

    @Override
    public void execute() {

    }
}
