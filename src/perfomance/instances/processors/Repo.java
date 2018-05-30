package perfomance.instances.processors;

import javafx.util.Pair;
import managment.Manager;
import perfomance.ICommand;
import perfomance.ICommandPacket;
import perfomance.ICommandProcessor;
import perfomance.instances.commands.*;
import perfomance.instances.packets.EmptyPacket;
import perfomance.instances.packets.ResponsePacket;
import perfomance.instances.packets.SocketPacket;
import utils.Zipper;
import utils.IVersionIncrement;
import utils.data.IDataProvider;
import utils.data.TransporterException;
import web_server.VersionControl;
import web_server.VersionControlServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

@SuppressWarnings("Duplicates")
public class Repo implements ICommandProcessor {
    private final Manager manager;
    private final VersionControl versionControl;
    private IDataProvider dataProvider;
    private IVersionIncrement versionIncrement;
    private String currentVersion;
    private Map<String, String> versionMapPaths;
    private String currentRepoName;
    private Map<String, String[]> versionContent;
    private Map<String, String> prevVersionMapNames;

    public Repo(Manager manager, VersionControl versionControl, IDataProvider dataProvider, IVersionIncrement versionIncrement){
        this.versionControl = versionControl;
        this.dataProvider = dataProvider;
        this.versionIncrement = versionIncrement;
        this.manager = manager;
        versionMapPaths = new HashMap<>();
        versionContent = new HashMap<>();
        prevVersionMapNames = new HashMap<>();
        currentVersion = "";
    }

    @Override
    public ICommandPacket process(ICommand command) {
        ICommandPacket response = EmptyPacket.INSTANCE;
        if (command instanceof EncryptionCommand){
            manager.setSecret(((EncryptionCommand) command).getSecret());
            return new ResponsePacket(VersionControl.SUCCESS, "Ok");
        }

        if (dataProvider.getOrigin() == null){
            // Hе можем обработать команды, кроме clone и add.
            if (command instanceof CreateCommand){
                response = processCreateCommand(command);
            }
            else if (command instanceof CloneCommand){
                String name = ((CloneCommand) command).getToClone();
                String pathToRepo = versionControl.getPathToRepo(name);
                if (pathToRepo == null)
                    response = new ResponsePacket(VersionControl.NO_SUCH_REPO_ERROR, "No such repo " + name);
                else {
                    response = cloneDirectory(name);
                    dataProvider.setOrigin(pathToRepo);
                    currentRepoName = name;
                }
            }
            else
                response = new ResponsePacket(VersionControl.NO_REPO_SELECTED_ERROR, "Clone repo first");
        }
        else {
            if (command instanceof CommitCommand){
                response = processCommitCommand((CommitCommand) command);
            }
            else if (command instanceof RevertCommand){
                response = processRevertCommand(((RevertCommand) command).getVersion(), ((RevertCommand) command).isHard());
            }
        }
        return response;
    }

    private ICommandPacket processCreateCommand(ICommand command){
        versionControl.createRepo(((CreateCommand) command).getToCreate());
        return new ResponsePacket(VersionControl.SUCCESS, "Ok");
    }

    private ICommandPacket cloneDirectory(String name){
        String lastVersion = versionControl.getLastVersion(name);
        return processRevertCommand(lastVersion, true);
    }

    private ICommandPacket processRevertCommand(String version, boolean hard){
        if (version.isEmpty())
            version = currentVersion;
        List<Pair<String, byte[]>> filesToSend;
        try {
            filesToSend = collectFiles(version, hard);
            if (filesToSend == null)
                return new ResponsePacket(VersionControl.NO_SUCH_VERSION_ERROR, "No such version (" + version + ") is found");
        }
        catch (IOException e){
            return new ResponsePacket(VersionControl.UNKNOWN_ERROR, "Unknown error occurred");
        }
        Pair<ICommandPacket, ServerSocket> packetAndSocket;
        try {
            packetAndSocket = createSocket();
        } catch (IOException e) {
            return new ResponsePacket(VersionControl.SOCKET_ERROR, "Server is busy, try later");
        }

        ServerSocket serverSocket = packetAndSocket.getValue();
        try {
            send(packetAndSocket.getKey());
        }
        catch (TransporterException e){
            try {
                serverSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
                return new ResponsePacket(VersionControl.TRANSPORT_ERROR, "Cannot send port to connect to");
            }
        }
        try{
            Socket dataSocket = serverSocket.accept();
            OutputStream os = dataSocket.getOutputStream();
            String[] names = new String[filesToSend.size()];
            byte[][] contents = new byte[filesToSend.size()][];
            for (int i = 0; i < filesToSend.size(); i++){
                Pair<String, byte[]> pair = filesToSend.get(i);
                names[i] = pair.getKey();
                contents[i] = pair.getValue();
            }
            os.write(Zipper.zipMultiple(names, contents));
            return new ResponsePacket(VersionControl.SUCCESS, "Ok");
        }
        catch (IOException e){
            e.printStackTrace();
            return new ResponsePacket(VersionControl.CONNECTION_ERROR, "Unknown error occurred");
        }

    }

    private List<Pair<String, byte[]>> collectFiles(String version, boolean hard) throws IOException{
        String[] names = versionContent.get(version);
        if (names == null)
            return null;
        List<Pair<String, byte[]>> contents = new ArrayList<>();
        Set<String> collecting = new HashSet<>();
        Collections.addAll(collecting, names);
        if (collecting.isEmpty())
            return null;
        String currentVersion = version;
        while (!(currentVersion == null) && !currentVersion.isEmpty()){
            List<Pair<String, byte[]>> folderContents = dataProvider.walkThrough(versionMapPaths.get(currentVersion));
            for (Pair<String, byte[]> nameAndData: folderContents){
                if (collecting.contains(nameAndData.getKey())){
                    contents.add(new Pair<>(nameAndData.getKey(), nameAndData.getValue()));
                    collecting.remove(nameAndData.getKey());
                }
            }
            currentVersion = prevVersionMapNames.get(currentVersion);
            if (!hard)
                break;
        }
        if (hard) {
            if (collecting.isEmpty())
                return contents;
            else
                return null;
        }
        if (!contents.isEmpty())
            return contents;
        return null;
    }

    private ICommandPacket processCommitCommand(CommitCommand command){
        String newVersion = (currentVersion.isEmpty()) ? versionIncrement.getFirst() : versionIncrement.increment(currentVersion);
        Pair<ICommandPacket, ServerSocket> packetAndSocket;
        try {
            packetAndSocket = createSocket();
        } catch (IOException e) {
            return new ResponsePacket(VersionControl.SOCKET_ERROR, "Server is busy, try later");
        }
        ICommandPacket socketPacket = packetAndSocket.getKey();
        ServerSocket serverSocket = packetAndSocket.getValue();
        versionContent.put(newVersion, command.getFiles());
        try {
            send(socketPacket);
        }
        catch (TransporterException e){
            try {
                serverSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
                return new ResponsePacket(VersionControl.TRANSPORT_ERROR, "Cannot send port to connect to");
            }
        }
        try {
            Socket dataSocket = serverSocket.accept();
            InputStream is = dataSocket.getInputStream();
            byte[] data = readFromStream(is);
            List<Pair<String, byte[]>> files = Zipper.unzipMultiple(data);
            boolean success = writeToVersion(newVersion, files);
            if (success) {
                versionControl.updateLastVersion(currentRepoName, newVersion);
                prevVersionMapNames.put(newVersion, currentVersion);
                currentVersion = newVersion;
            }
            return (success) ? new ResponsePacket(VersionControl.SUCCESS, "Ok") : new ResponsePacket(VersionControl.WRITE_ERROR, "Cannot save file");
        }
        catch (IOException e){
            e.printStackTrace();
            return new ResponsePacket(VersionControl.CONNECTION_ERROR, "Unknown error occurred");
        }
    }

    private boolean writeToVersion(String version, List<Pair<String, byte[]>> files){
        String pathToVersion = dataProvider.resolve(dataProvider.getOrigin(), version);
        versionMapPaths.put(version, pathToVersion);
        dataProvider.setCurrentRoot(pathToVersion);
        for (Pair<String, byte[]> nameAndData: files){
            try {
                dataProvider.write(nameAndData.getKey(), nameAndData.getValue());
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private byte[] readFromStream(InputStream inputStream){
        byte[] bytes = new byte[0];
        try {
            ByteArrayOutputStream tempStream = new ByteArrayOutputStream();
            int count;
            byte[] buffer = new byte[4096];
            count = inputStream.read(buffer);
            tempStream.write(buffer, 0, count);
            bytes = tempStream.toByteArray();
            tempStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    private Pair<ICommandPacket, ServerSocket> createSocket() throws IOException {
        ICommandPacket response;
        ServerSocket dataSocket;
        dataSocket = VersionControlServer.createSocket();
        response = new SocketPacket(dataSocket.getLocalPort());
        return new Pair<>(response, dataSocket);
    }

    @Override
    public ICommandPacket createPacket(String identifier) {
        return null;
    }

    @Override
    public void send(ICommandPacket packet) throws TransporterException {
        manager.sendToAnotherProcessor(packet);
    }

    @Override
    public ICommandPacket get() throws TransporterException {
        return manager.getFromAnotherManager();
    }
}
