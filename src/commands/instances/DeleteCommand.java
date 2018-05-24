package commands.instances;

import utils.data.EmptyDataProvider;

public class DeleteCommand extends DataCommand {
    private final String toDelete;

    public DeleteCommand(String name){
        this.toDelete = name;
    }

    @Override
    public void execute() {
        if (path == null)
            path = "";
        if (dataProvider == null)
            dataProvider = EmptyDataProvider.INSTANCE;
        dataProvider.delete(dataProvider.resolvePath(path, toDelete));
    }
}
