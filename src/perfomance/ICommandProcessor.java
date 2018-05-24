package perfomance;

import utils.data.TransporterException;

public interface ICommandProcessor {
    ICommandPacket process(ICommand command);
    ICommandPacket createPacket(String identifier);
    void send(ICommandPacket packet) throws TransporterException;
    ICommandPacket get() throws TransporterException;
}
