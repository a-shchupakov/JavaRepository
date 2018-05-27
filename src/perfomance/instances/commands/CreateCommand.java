package perfomance.instances.commands;

import utils.data.EmptyDataProvider;

public class CreateCommand extends DataCommand{
    private final String toCreate;

    public CreateCommand(String names){
        toCreate = names;
    }

    @Override
    public void execute() {
        if (dataProvider == null)
            dataProvider = EmptyDataProvider.INSTANCE;
        dataProvider.createDirectory(toCreate);
    }

    public String getToCreate(){
        return toCreate;
    }
}
