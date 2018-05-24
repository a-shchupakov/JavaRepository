package commands.instances;

import utils.data.EmptyDataProvider;

public class WriteCommand extends DataCommand {
    private String[] names;
    private byte[][] bytes;

    public WriteCommand(String[] names, byte[][] bytes){
        this.names = names;
        this.bytes = bytes;
    }

    @Override
    public void execute() {
        if (path == null)
            path = "";
        if (dataProvider == null)
            dataProvider = EmptyDataProvider.INSTANCE;
        for (int i = 0; i < bytes.length; i++){
            dataProvider.write(dataProvider.resolvePath(path, names[i]), bytes[i]);
        }
    }
}
