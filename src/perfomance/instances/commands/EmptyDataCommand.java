package perfomance.instances.commands;

import utils.data.EmptyDataProvider;

public class EmptyDataCommand extends DataCommand {
    public static final EmptyDataCommand INSTANCE = new EmptyDataCommand();

    private EmptyDataCommand(){
        dataProvider = EmptyDataProvider.INSTANCE;
    }

    @Override
    public void execute() {

    }
}
