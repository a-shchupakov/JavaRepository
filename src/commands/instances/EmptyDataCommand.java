package commands.instances;

import utils.data.EmptyDataProvider;

public class EmptyDataCommand extends DataCommand {
    public static final EmptyDataCommand INSTANCE = new EmptyDataCommand();

    private EmptyDataCommand(){
        dataProvider = EmptyDataProvider.INSTANCE;
        path = "";
    }

    @Override
    public void execute() {

    }
}
