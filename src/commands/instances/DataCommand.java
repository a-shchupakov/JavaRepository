package commands.instances;

import commands.ICommand;
import utils.data.IDataProvider;

public abstract class DataCommand implements ICommand {
    protected IDataProvider dataProvider;

    public final void setDataProvider(IDataProvider dataProvider){
        this.dataProvider = dataProvider;
    }
}
