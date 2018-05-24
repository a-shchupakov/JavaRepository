package perfomance.instances.processors;

import perfomance.ICommand;
import perfomance.ICommandPacket;
import perfomance.ICommandProcessor;
import utils.data.TransporterException;

@SuppressWarnings("Duplicates")
public class Repo implements ICommandProcessor {
    @Override
    public ICommandPacket process(ICommand command) {
        return null;
    }

    @Override
    public ICommandPacket createPacket(String identifier) {
        return null;
    }

    @Override
    public void send(ICommandPacket packet) throws TransporterException {

    }

    @Override
    public ICommandPacket get() throws TransporterException {
        return null;
    }
}
