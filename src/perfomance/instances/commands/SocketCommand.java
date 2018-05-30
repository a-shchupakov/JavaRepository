package perfomance.instances.commands;

import perfomance.ICommand;

public class SocketCommand implements ICommand {
    private final int socketPort;

    public SocketCommand(int port){
        socketPort = port;
    }

    public int getSocketPort() {
        return socketPort;
    }

    @Override
    public void execute() {

    }
}
