package commands.instances;

import utils.data.EmptyDataProvider;

import java.io.IOException;

public class DeleteCommand extends DataCommand {
    private final String toDelete;

    public DeleteCommand(String name){
        this.toDelete = name;
    }

    @Override
    public void execute() {
        if (dataProvider == null)
            dataProvider = EmptyDataProvider.INSTANCE;

        try {
            dataProvider.delete(toDelete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
