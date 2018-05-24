package perfomance.instances.commands;

import utils.data.EmptyDataProvider;

import java.io.IOException;

public class DeleteCommand extends DataCommand {
    private final String[] toDelete;

    public DeleteCommand(String[] names){
        toDelete = names;
    }

    @Override
    public void execute() {
        if (dataProvider == null)
            dataProvider = EmptyDataProvider.INSTANCE;

        for (String name: toDelete) {
            try {
                dataProvider.delete(name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
