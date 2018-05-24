package perfomance.instances.commands;

public class ResponseCommand extends DataCommand {
    private final int error;
    private final String errorInfo;
    private DataCommand innerCommand;

    public  ResponseCommand(int error, String errorInfo, DataCommand innerCommand){
        this.error = error;
        this.errorInfo = errorInfo;
        this.innerCommand = innerCommand;
    }

    public int getError() {
        return error;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    @Override
    public void execute() {
        innerCommand.setDataProvider(this.dataProvider);
        innerCommand.execute();
    }
}