package perfomance.instances.commands;

import utils.data.EmptyDataProvider;

import java.io.IOException;

public class WriteCommand extends DataCommand {
    private String[] names;
    private byte[][] bytes;

    public WriteCommand(String[] names, byte[][] bytes){
        this.names = names;
        this.bytes = bytes;
    }

    @Override
    public void execute() {
        if (dataProvider == null)
            dataProvider = EmptyDataProvider.INSTANCE;
        for (int i = 0; i < bytes.length; i++){
            try {
                dataProvider.write(names[i], bytes[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
