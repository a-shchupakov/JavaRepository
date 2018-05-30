package perfomance.instances.processors;

import perfomance.ICommand;
import perfomance.ICommandPacket;
import perfomance.ICommandProcessor;
import perfomance.instances.commands.*;
import perfomance.instances.packets.EmptyPacket;
import utils.IVersionIncrement;
import utils.data.IDataProvider;
import utils.data.TransporterException;
import web_server.VersionControl;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("Duplicates")
public class Repo implements ICommandProcessor {
    private final VersionControl versionControl;
    private IDataProvider dataProvider;
    private IVersionIncrement versionIncrement;
    private String currentVersion;
    private Map<String, String> versionMap;
    private String currentRepoName;

    public Repo(VersionControl versionControl, IDataProvider dataProvider, IVersionIncrement versionIncrement){
        this.versionControl = versionControl;
        this.dataProvider = dataProvider;
        this.versionIncrement = versionIncrement;
        versionMap = new HashMap<>();
    }

    @Override
    public ICommandPacket process(ICommand command) { //TODO: finish command processing
        ICommandPacket response = EmptyPacket.INSTANCE;

        if (dataProvider.getOrigin() == null){
            // Hе можем обработать команды, кроме clone и add.
            if (command instanceof CreateCommand){
                command = processCreateCommand(command);
            }
            else if (command instanceof CloneCommand){
                String name = ((CloneCommand) command).getToClone();
                response = cloneDirectory(name);
                dataProvider.setOrigin(versionControl.getPathToRepo(name));
                currentRepoName = name;
            }
            else
                command = EmptyCommand.INSTANCE;
        }
        else {
            if (command instanceof DataCommand) {
                ((DataCommand) command).setDataProvider(dataProvider);
            }
            if (command instanceof CreateCommand){
                command = processCreateCommand(command);
            }
            else if (command instanceof WriteCommand){
                if (currentVersion == null)
                    currentVersion = versionIncrement.getFirst();
                else
                    currentVersion = versionIncrement.increment(currentVersion);
                dataProvider.setCurrentRoot(dataProvider.getOrigin());
                dataProvider.createDirectory(currentVersion); // Create directory in origin folder
                String currentRoot = dataProvider.resolve(dataProvider.getOrigin(), currentVersion);
                dataProvider.setCurrentRoot(currentRoot); // Write in created directory
                updateLastVersion(currentVersion, currentRoot);
            }
            command.execute();
        }

        return response;
    }

    private ICommand processCreateCommand(ICommand command){
        versionControl.createRepo(((CreateCommand) command).getToCreate());
        return EmptyDataCommand.INSTANCE;
    }

    private ICommandPacket cloneDirectory(String name){
        String path = versionControl.resolve(versionControl.getPathToRepo(name), versionControl.getLastVersion(name));
        // TODO: содержимое path запихать в WritePacket
        return null;
    }

    private void updateLastVersion(String currentVersion, String currentRoot){
        versionMap.put(currentVersion, currentRoot);
        versionControl.updateLastVersion(currentRepoName, currentVersion);
    }

    @Override
    public ICommandPacket createPacket(String identifier) {
        return null;
    }

    @Override
    public void send(ICommandPacket packet) throws TransporterException {

    }

    @Override
    public ICommandPacket get() throws TransporterException {
        return null;
    }
}
