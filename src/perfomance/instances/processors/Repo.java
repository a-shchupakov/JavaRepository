package perfomance.instances.processors;

import javafx.util.Pair;
import managment.Manager;
import perfomance.ICommand;
import perfomance.ICommandPacket;
import perfomance.ICommandProcessor;
import perfomance.instances.commands.*;
import perfomance.instances.packets.EmptyPacket;
import perfomance.instances.packets.Md5Packet;
import perfomance.instances.packets.ResponsePacket;
import perfomance.instances.packets.SocketPacket;
import utils.Md5Hash;
import utils.Zipper;
import utils.IVersionIncrement;
import utils.data.IDataProvider;
import utils.data.TransporterException;
import utils.encrypt.IEncryptor;
import web_server.VersionControl;
import web_server.VersionControlServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;


public class Repo implements ICommandProcessor {
    private final Manager manager;
    private final VersionControl versionControl;
    private IDataProvider dataProvider;
    private IVersionIncrement versionIncrement;
    private String currentVersion;
    private String lastVersion;
    private Map<String, String> versionMapPaths;
    private String currentRepoName;
    private Map<String, String[]> versionContent;
    private Map<String, String> prevVersionMapNames;
    private int socketTimeOut;
    private IEncryptor encryptor;

    public Repo(Manager manager, VersionControl versionControl, IDataProvider dataProvider, IVersionIncrement versionIncrement, IEncryptor encryptor){
        this.versionControl = versionControl;
        this.dataProvider = dataProvider;
        this.versionIncrement = versionIncrement;
        this.manager = manager;
        this.encryptor = encryptor;
        versionMapPaths = new HashMap<>();
        versionContent = new HashMap<>();
        prevVersionMapNames = new HashMap<>();
        currentVersion = "";
        lastVersion = "";
        socketTimeOut = 5000 * 3;
    }

    @Override
    public ICommandPacket process(ICommand command) {
        ICommandPacket response = EmptyPacket.INSTANCE;
        if (command instanceof EncryptionCommand){
            encryptor.setSecret(((EncryptionCommand) command).getSecret());
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
            else if (command instanceof Md5Command){
                response = processMd5Command((Md5Command) command);
            }
        }

        return response;
    }

    private ICommandPacket processMd5Command(Md5Command command){
        if (!command.getType().toLowerCase().equals("query"))
            return new ResponsePacket(VersionControl.COMMAND_NOT_ALLOWED, "Command not allowed");
        List<Pair<String, byte[]>> versionFiles;
        try {
            versionFiles = collectVersion(currentVersion, true);
        }
        catch (IOException e){
            return new ResponsePacket(VersionControl.UNKNOWN_ERROR, "Unknown error occurred");
        }

        if (versionFiles == null)
            return new Md5Packet("response", new String[0], new byte[0][]);

        String names[] = new String[versionFiles.size()];
        byte[][] hashes = new byte[versionFiles.size()][];

        Pair<String, byte[]> pair;
        for (int i = 0; i < versionFiles.size(); i++){
            pair = versionFiles.get(i);
            names[i] = pair.getKey();
            hashes[i] = Md5Hash.getMd5Hash(pair.getValue());
        }
        return new Md5Packet("response", names, hashes);
    }

    private ICommandPacket processCreateCommand(ICommand command){
        versionControl.createRepo(((CreateCommand) command).getToCreate());
        return new ResponsePacket(VersionControl.SUCCESS, "Ok");
    }

    private ICommandPacket cloneDirectory(String name){
        String lastVersion = versionControl.getLastVersion(name);
        if (lastVersion.isEmpty())
            return new ResponsePacket(VersionControl.SUCCESS, "Cloned");
        return processRevertCommand(lastVersion, true);
    }

    private ICommandPacket processRevertCommand(String version, boolean hard){
        if (version.isEmpty())
            version = lastVersion;
        List<Pair<String, byte[]>> filesToSend;
        try {
            filesToSend = collectVersion(version, hard);
            if (filesToSend == null)
                return new ResponsePacket(VersionControl.NO_SUCH_VERSION_ERROR, "No such version (" + version + ") is found");
        }
        catch (IOException e){
            return new ResponsePacket(VersionControl.UNKNOWN_ERROR, "Unknown error occurred");
        }
        Pair<ICommandPacket, ServerSocket> packetAndSocket;
        try {
            packetAndSocket = createSocket("read");
        } catch (IOException e) {
            return new ResponsePacket(VersionControl.SOCKET_ERROR, "Server is busy, try later");
        }

        ServerSocket serverSocket = packetAndSocket.getValue();
        OutputStream os = null;
        Socket dataSocket = null;
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
        try {
            serverSocket.setSoTimeout(socketTimeOut);
            dataSocket = serverSocket.accept();
            os = dataSocket.getOutputStream();
            String[] names = new String[filesToSend.size()];
            byte[][] contents = new byte[filesToSend.size()][];
            for (int i = 0; i < filesToSend.size(); i++){
                Pair<String, byte[]> pair = filesToSend.get(i);
                names[i] = pair.getKey();
                contents[i] = pair.getValue();
            }
            currentVersion = version;
            os.write(encryptor.encrypt(Zipper.zipMultiple(names, contents)));
            return new ResponsePacket(VersionControl.SUCCESS, "Ok");
        }
        catch (SocketTimeoutException e){
            return new ResponsePacket(VersionControl.CONNECTION_ERROR, "No connection was accepted");
        }
        catch (IOException e){
            e.printStackTrace();
            return new ResponsePacket(VersionControl.CONNECTION_ERROR, "Unknown error occurred");
        }
        finally {
            close(serverSocket);
            close(dataSocket);
            close(os);
        }
    }

    private List<Pair<String, byte[]>> collectVersion(String version, boolean hard) throws IOException{
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
        String newVersion = (lastVersion.isEmpty()) ? versionIncrement.getFirst() : versionIncrement.increment(lastVersion);
        System.out.println("New version on server " + newVersion);
        Pair<ICommandPacket, ServerSocket> packetAndSocket;
        try {
            packetAndSocket = createSocket("write");
        } catch (IOException e) {
            return new ResponsePacket(VersionControl.SOCKET_ERROR, "Server is busy, try later");
        }
        ICommandPacket socketPacket = packetAndSocket.getKey();
        ServerSocket serverSocket = packetAndSocket.getValue();
        Socket dataSocket = null;
        InputStream is = null;
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
            serverSocket.setSoTimeout(socketTimeOut);
            dataSocket = serverSocket.accept();
            is = dataSocket.getInputStream();
            byte[] data = readFromStream(is);
            List<Pair<String, byte[]>> files = Zipper.unzipMultiple(encryptor.decrypt(data));
            boolean success = writeToVersion(newVersion, files);
            if (success) {
                versionControl.updateLastVersion(currentRepoName, newVersion);
                prevVersionMapNames.put(newVersion, currentVersion);
                currentVersion = newVersion;
                lastVersion = newVersion;
                versionContent.put(newVersion, command.getFiles());

                System.out.println("Contents:");
                for (Map.Entry<String, String[]> entry: versionContent.entrySet()){
                    System.out.println(entry.getKey() + ": " + Arrays.toString(entry.getValue()));
                }
                System.out.println(Arrays.toString(versionMapPaths.entrySet().toArray()));
                System.out.println(Arrays.toString(prevVersionMapNames.entrySet().toArray()));
                System.out.println("Commit succeeded");
            }
            return (success)
                    ? new ResponsePacket(VersionControl.SUCCESS, "Commit was pushed to " + currentVersion + "version" )
                    : new ResponsePacket(VersionControl.WRITE_ERROR, "Cannot save file");
        }
        catch (SocketTimeoutException e){
            return new ResponsePacket(VersionControl.CONNECTION_ERROR, "No connection was accepted");
        }
        catch (IOException e){
            e.printStackTrace();
            return new ResponsePacket(VersionControl.CONNECTION_ERROR, "Unknown error occurred");
        }
        finally {
            close(serverSocket);
            close(dataSocket);
            close(is);
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

    private byte[] readFromStream(InputStream inputStream) throws IOException{
        byte[] bytes;
        ByteArrayOutputStream tempStream = new ByteArrayOutputStream();
        int count;
        byte[] buffer = new byte[4096];
        count = inputStream.read(buffer);
        tempStream.write(buffer, 0, count);
        bytes = tempStream.toByteArray();
        tempStream.close();
        return bytes;
    }

    private Pair<ICommandPacket, ServerSocket> createSocket(String type) throws IOException {
        ICommandPacket response;
        ServerSocket dataSocket;
        dataSocket = VersionControlServer.createSocket();
        response = new SocketPacket(dataSocket.getLocalPort(), type);
        return new Pair<>(response, dataSocket);
    }

    @Override
    public void sendPacket(String identifier) {
    }

    @Override
    public void send(ICommandPacket packet) throws TransporterException {
        manager.sendPacket(packet);
    }

    @Override
    public ICommand get() throws TransporterException {
        return manager.getCommand();
    }

    private static void close(Closeable closeable){
        try {
            if (closeable != null)
                closeable.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
