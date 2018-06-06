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
import utils.data.IDataTransporter;
import utils.data.NetDataTransporter;
import utils.data.TransporterException;
import web_server.VersionControl;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Repo implements ICommandProcessor {
    private final String userIdentifier;
    private final Manager manager;
    private final VersionControl versionControl;
    private IDataProvider dataProvider;
    private IDataTransporter dataTransporter;
    private IVersionIncrement versionIncrement;
    private String currentVersion;
    private String lastVersion;
    private Map<String, String> versionMapPaths;
    private String currentRepoName;
    private Map<String, String[]> versionContent;
    private Map<String, String> prevVersionMapNames;
    private int socketTimeOut;
    private int socketPort;

    public Repo(String usedIdentifier, Manager manager, VersionControl versionControl, IDataProvider dataProvider, IVersionIncrement versionIncrement){
        this.userIdentifier = usedIdentifier;
        this.versionControl = versionControl;
        this.dataProvider = dataProvider;
        this.dataTransporter = null;
        this.versionIncrement = versionIncrement;
        this.manager = manager;
        currentVersion = "";
        lastVersion = "";
        socketTimeOut = 5000 * 3;
    }

    @Override
    public ICommandPacket process(ICommand command) {
        ICommandPacket response = EmptyPacket.INSTANCE;
        if (command instanceof CreateCommand)
            return processCreateCommand(command);
        else if (command instanceof CloneCommand){
            String name = ((CloneCommand) command).getToClone();
            String pathToRepo = versionControl.getPathToRepo(name);
            if (pathToRepo == null)
                return new ResponsePacket(VersionControl.NO_SUCH_REPO_ERROR, "No such repo " + name);
            else {
                versionMapPaths = versionControl.getVersionMapPaths(name);
                versionContent = versionControl.getVersionContent(name);
                prevVersionMapNames = versionControl.getPrevVersionMapNames(name);
                currentVersion = versionControl.getLastVersion(name);
                socketPort = versionControl.getRepoPort(name);
                lastVersion = currentVersion;
                response = cloneDirectory(name);
                dataProvider.setOrigin(pathToRepo);
                currentRepoName = name;
                return response;
            }
        }
        if (dataProvider.getOrigin() == null) // Hе можем обработать команды, кроме clone и add.
            return new ResponsePacket(VersionControl.NO_REPO_SELECTED_ERROR, "Clone repo first");
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
            else if (command instanceof LogCommand){
                response = processLogCommand((LogCommand) command);
            }
            return response;
        }
    }

    private boolean isSameFiles(String file1, String file2){
        byte[] hash1, hash2;
        try {
            hash1 = Md5Hash.getMd5Hash(dataProvider.read(file1));
            hash2 = Md5Hash.getMd5Hash(dataProvider.read(file2));
        }
        catch (IOException e){
            return true;
        }
        return (Arrays.equals(hash1, hash2));
    }

    private StringBuilder getFormattedFileNames(String version, String[] fileNames, StringBuilder sb){
        String prevVersion = prevVersionMapNames.get(version);
        Set<String> prevVersionContent = new HashSet<>(Arrays.asList(versionContent.get(prevVersion)));
        Set<String> currentVersionContent = new HashSet<>(Arrays.asList(fileNames));

        String pathToPrev = (prevVersion.isEmpty()) ? null : dataProvider.resolve(dataProvider.getOrigin(), versionMapPaths.get(prevVersion));
        String pathToCurrent = dataProvider.resolve(dataProvider.getOrigin(), versionMapPaths.get(version));
        for (String fileName: fileNames){
            if (prevVersionContent.contains(fileName)) {
                if (!isSameFiles(dataProvider.resolve(pathToPrev, fileName), dataProvider.resolve(pathToCurrent, fileName)))
                    sb.append("\t").append("^ ").append(fileName).append("\r\n");
            }
            else
                sb.append("\t").append("+ ").append(fileName).append("\r\n");
        }

        for (String name: prevVersionContent){
            if (!currentVersionContent.contains(name))
                sb.append("\t").append("- ").append(name).append("\r\n");
        }
        return sb;
    }

    private ICommandPacket updateLog(String version, String[] files){
        String pathToLog = versionControl.getRepoLogFile(currentRepoName);
        String date = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date());
        StringBuilder sb = new StringBuilder();
        sb.append(userIdentifier).append(" commits following changes at ").append(date).append(" (version ").append(version).append("):\r\n");
        sb = getFormattedFileNames(version, files, sb);
        sb.append("\r\n");
        byte[] log = sb.toString().getBytes();
        try {
            dataProvider.append(pathToLog, log);
        } catch (IOException e) {
            return new ResponsePacket(VersionControl.CANNOT_SAVE_LOG, "Can not update log file");
        }
        return new ResponsePacket(VersionControl.SUCCESS, "Ok");
    }

    private ICommandPacket sendToSocket( Pair<ICommandPacket, ServerSocket> packetAndSocket, byte[] bytes){
        ServerSocket serverSocket = packetAndSocket.getValue();
        Socket dataSocket = null;
        try {
            send(packetAndSocket.getKey());
        }
        catch (TransporterException e){
            return new ResponsePacket(VersionControl.TRANSPORT_ERROR, "Cannot send port to connect to");
        }
        InputStream is = null;
        OutputStream os = null;
        try {
            serverSocket.setSoTimeout(socketTimeOut);
            dataSocket = serverSocket.accept();
            is = dataSocket.getInputStream();
            os = dataSocket.getOutputStream();
            dataTransporter = new NetDataTransporter(is, os);
            dataTransporter.send(bytes);
            return new ResponsePacket(VersionControl.SUCCESS, "Ok");
        }
        catch (SocketTimeoutException e){
            return new ResponsePacket(VersionControl.CONNECTION_ERROR, "No connection was accepted");
        }
        catch (IOException | TransporterException e){
            return new ResponsePacket(VersionControl.CONNECTION_ERROR, "Unknown error occurred");
        }
        finally {
            close(is);
            close(os);
            close(serverSocket);
            close(dataSocket);
        }
    }

    private ICommandPacket processLogCommand(LogCommand command){
        if (!"query".equals(command.getType()))
            return new ResponsePacket(VersionControl.COMMAND_NOT_ALLOWED, "Unsupported command");
        Pair<ICommandPacket, ServerSocket> packetAndSocket;
        try {
            packetAndSocket = createSocket("notify");
        } catch (IOException e) {
            return new ResponsePacket(VersionControl.SOCKET_ERROR, "Server is busy, try later");
        }

        try {
            byte[] logBytes = dataProvider.read(versionControl.getRepoLogFile(currentRepoName));
            return sendToSocket(packetAndSocket, Zipper.zipOne(logBytes, ""));
        } catch (IOException e) {
            return new ResponsePacket(VersionControl.UNKNOWN_ERROR, "Unknown error occurred");
        }
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

        String[] names = new String[filesToSend.size()];
        byte[][] contents = new byte[filesToSend.size()][];
        for (int i = 0; i < filesToSend.size(); i++){
            Pair<String, byte[]> pair = filesToSend.get(i);
            names[i] = pair.getKey();
            contents[i] = pair.getValue();
        }
        currentVersion = version;
        try {
            byte[] bytesToSend = Zipper.zipMultiple(names, contents);
            return sendToSocket(packetAndSocket, bytesToSend);
        } catch (IOException e) {
            return new ResponsePacket(VersionControl.UNKNOWN_ERROR, "Unknown error occurred");
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
        Pair<ICommandPacket, ServerSocket> packetAndSocket;
        try {
            packetAndSocket = createSocket("write");
        } catch (IOException e) {
            return new ResponsePacket(VersionControl.SOCKET_ERROR, "Server is busy, try later");
        }
        ICommandPacket socketPacket = packetAndSocket.getKey();
        ServerSocket serverSocket = packetAndSocket.getValue();
        Socket dataSocket = null;
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
        InputStream is = null;
        OutputStream os = null;
        try {
            serverSocket.setSoTimeout(socketTimeOut);
            dataSocket = serverSocket.accept();
            is = dataSocket.getInputStream();
            os = dataSocket.getOutputStream();
            dataTransporter = new NetDataTransporter(is, os);
            System.out.println("Getting data");
            byte[] data = dataTransporter.get();
            List<Pair<String, byte[]>> files = Zipper.unzipMultiple(data);
            boolean success = writeToVersion(newVersion, files);
            ICommandPacket logPacket = null;
            if (success) {
                versionControl.updateLastVersion(currentRepoName, newVersion);
                prevVersionMapNames.put(newVersion, currentVersion);
                currentVersion = newVersion;
                lastVersion = newVersion;
                versionContent.put(newVersion, command.getFiles());

                System.out.println("New version on server " + newVersion);
                System.out.println("Contents:");
                for (Map.Entry<String, String[]> entry: versionContent.entrySet()){
                    System.out.println(entry.getKey() + ": " + Arrays.toString(entry.getValue()));
                }
                System.out.println(Arrays.toString(versionMapPaths.entrySet().toArray()));
                System.out.println(Arrays.toString(prevVersionMapNames.entrySet().toArray()));
                System.out.println("Commit succeeded");
                logPacket = updateLog(currentVersion, command.getFiles());
            }
            if (success){
                if (((ResponsePacket) logPacket).error == VersionControl.SUCCESS)
                    return new ResponsePacket(VersionControl.SUCCESS, "Commit was pushed to " + currentVersion + " version" );
                else
                    return logPacket;
            }
            else return new ResponsePacket(VersionControl.WRITE_ERROR, "Can not save file");
        }
        catch (SocketTimeoutException e){
            return new ResponsePacket(VersionControl.CONNECTION_ERROR, "No connection was accepted");
        }
        catch (IOException | TransporterException e){
            return new ResponsePacket(VersionControl.CONNECTION_ERROR, "Unknown error occurred");
        }
        finally {
            close(is);
            close(os);
            close(serverSocket);
            close(dataSocket);
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
                return false;
            }
        }
        return true;
    }

    private Pair<ICommandPacket, ServerSocket> createSocket(String type) throws IOException {
        ICommandPacket response;
        ServerSocket dataSocket;
        if (socketPort == -1)
            throw new IOException("No port available");
        dataSocket = new ServerSocket(socketPort);
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
        catch (IOException e){ }
    }
}
