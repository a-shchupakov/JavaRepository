package commands.instances;

import commands.ICommand;
import utils.data.IDataProvider;

public abstract class DataCommand implements ICommand {
    protected IDataProvider dataProvider;
    protected String path;

    public final void setDataProvider(IDataProvider dataProvider){
        this.dataProvider = dataProvider;
    }
    public final void setPath(String path){
        this.path = path;
    }
}
