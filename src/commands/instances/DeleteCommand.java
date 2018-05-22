package commands.instances;

import commands.ICommand;
import utils.data.IDataProvider;
import utils.data.EmptyDataProvider;

public class DeleteCommand implements ICommand {
    private IDataProvider dataProvider;
    private final String toDelete;

    public DeleteCommand(String name){
        this.toDelete = name;
    }

    public void setDataProvider(IDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void execute() {
        if (dataProvider == null)
            dataProvider = EmptyDataProvider.INSTANCE;
        dataProvider.delete(toDelete);
    }
}
