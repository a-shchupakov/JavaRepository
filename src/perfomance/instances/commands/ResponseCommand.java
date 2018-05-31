package perfomance.instances.commands;

import perfomance.ICommand;

public class ResponseCommand implements ICommand {
    private final int error;
    private final String errorInfo;

    public  ResponseCommand(int error, String errorInfo){
        this.error = error;
        this.errorInfo = errorInfo;
    }

    public int getError() {
        return error;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    @Override
    public void execute() {

    }
}
