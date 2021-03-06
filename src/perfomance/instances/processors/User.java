package perfomance.instances.processors;

import javafx.util.Pair;
import managment.Manager;
import perfomance.ICommand;
import perfomance.ICommandPacket;
import perfomance.ICommandProcessor;
import perfomance.instances.commands.Md5Command;
import perfomance.instances.commands.ResponseCommand;
import perfomance.instances.commands.SocketCommand;
import perfomance.instances.packets.*;
import utils.Md5Hash;
import utils.Zipper;
import utils.data.IDataProvider;
import utils.data.IDataTransporter;
import utils.data.NetDataTransporter;
import utils.data.TransporterException;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;


public class User implements ICommandProcessor {
    private final Manager manager;
    private IDataProvider dataProvider;
    private IDataTransporter dataTransporter;
    private byte[] tempBytesToSend;
    private boolean tempHard;
    private InetAddress address;
    private final PrintStream printStream;

    public User(Manager manager, IDataProvider dataProvider, InetAddress address, PrintStream printStream) {
        this.manager = manager;
        this.dataProvider = dataProvider;
        this.dataTransporter = null;
        this.address = address;
        this.printStream = printStream;
    }

    @Override
    public ICommandPacket process(ICommand command) {
        if (command instanceof EmptyPacket)
            return null;
        else if (command instanceof ResponseCommand)
            System.out.println(((ResponseCommand) command).getError() + ": " + ((ResponseCommand) command).getErrorInfo());
        else if (command instanceof SocketCommand){
            int port = ((SocketCommand) command).getSocketPort();
            String type = ((SocketCommand) command).getType();
            Socket socket = createSocket(port);
            if (socket == null)
                return null;
            operateWithSocket(socket, type);

            try {
                ICommand responseCommand = get();
                process(responseCommand);
            } catch (TransporterException e) { }
        }

        return null;
    }

    private Socket createSocket(int port){
        try {
            return new Socket(address, port);
        } catch (IOException e) { }
        return null;
    }

    private void operateWithSocket(Socket socket, String command){
        InputStream is = null;
        OutputStream os = null;
        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();
            dataTransporter = new NetDataTransporter(is, os);
            if ("write".equals(command)) {
                System.out.println("Sending data");
                if (tempBytesToSend != null)
                    dataTransporter.send(tempBytesToSend);
                tempBytesToSend = null;
                printStream.println("Sending files...");
            } else if ("read".equals(command)) {
                byte[] dataBytes = dataTransporter.get();
                printStream.println("Getting files");
                writeBytes(dataBytes);
            }
            else if ("notify".equals(command)){
                byte[] bytes = dataTransporter.get();
                printStream.print(new String(Zipper.unzipOne(bytes), "UTF-8"));
            }
        }
        catch (TransporterException | IOException e){ }
        finally {
            close(is);
            close(os);
            close(socket);
            tempHard = false;
            tempBytesToSend = null;
        }
    }

    private void writeBytes(byte[] bytes){
        try {
            printStream.println("Saving files");
            if (tempHard)
                dataProvider.clearDirectory(dataProvider.getOrigin());
            List<Pair<String, byte[]>> files = Zipper.unzipMultiple(bytes);
            for (Pair<String, byte[]> pair: files){
                dataProvider.write(pair.getKey(), pair.getValue());
            }
        } catch (IOException e) { }
    }

    private boolean isValid(String[] command){
        try {
            if ("add".equals(command[0])) {
                return !command[1].isEmpty() && command.length == 2;
            } else if ("clone".equals(command[0])) {
                if (command.length == 3) {
                    return !command[1].isEmpty() && !command[2].isEmpty();
                } else if (command.length == 4) {
                    return !command[1].isEmpty() && !command[2].isEmpty() && ".".equals(command[3]);
                }
            } else if ("update".equals(command[0]) || "commit".equals(command[0]) || "log".equals(command[0])) {
                return command.length == 1;
            } else if ("revert".equals(command[0])) {
                if (command.length == 2) {
                    return !command[1].isEmpty();
                } else if (command.length == 3) {
                    return !command[1].isEmpty() && "-hard".equals(command[2]);
                }
            }
        }
        catch (IndexOutOfBoundsException e){
            return false;
        }
        return false;
    }

    @Override
    public void sendPacket(String identifier) {
        String[] command = identifier.split(" ");
        ICommandPacket packet = null;
        if (isValid(command)) {
            if (identifier.toLowerCase().startsWith("add")) {
                packet = sendAddPacket(command);
            } else if (identifier.toLowerCase().startsWith("clone")) {
                packet = sendClonePacket(command);
            } else if (identifier.toLowerCase().startsWith("update")) {
                packet = sendUpdatePacket(command);
            } else if (identifier.toLowerCase().startsWith("commit")) {
                packet = sendCommitPacket(command);
            } else if (identifier.toLowerCase().startsWith("revert")) {
                packet = sendRevertPacket(command);
            } else if (identifier.toLowerCase().startsWith("log")) {
                packet = sendLogPacket(command);
            }
        }
        try {
            if (packet == null)
                packet = EmptyPacket.INSTANCE;
            send(packet);
        } catch (TransporterException e) { }
    }

    private ICommandPacket sendAddPacket(String[] command){
        return new CreatePacket(command[1]);
    }

    private ICommandPacket sendClonePacket(String[] command){
        boolean straight = false;
        if (command.length == 4) {
            straight = true;
        }
        String path = command[1];
        String name = command[2];
        if (straight)
            dataProvider.setOrigin(path);
        else
            dataProvider.setOrigin(dataProvider.resolve(path, name));
        dataProvider.clearDirectory(dataProvider.getOrigin());
        return new ClonePacket(name);
    }

    private ICommandPacket sendCommitPacket(String[] command){
        ICommandPacket md5Packet = new Md5Packet("query", null, null);
        String[] names = null;
        byte[][] contents = null;
        try {
            send(md5Packet);
            ICommand respCommand = get();
            if (respCommand instanceof ResponseCommand)
                System.out.println(((ResponseCommand) respCommand).getError() + ": " +((ResponseCommand) respCommand).getErrorInfo());
            else if (respCommand instanceof Md5Command){
                names = ((Md5Command) respCommand).getNames();
                contents = ((Md5Command) respCommand).getMd5Bytes();
            }
        } catch (TransporterException e) {
            return null;
        }
        List<Pair<String, byte[]>> dirContents;
        try {
            dirContents = dataProvider.walkThrough(dataProvider.getCurrentRoot());
        } catch (IOException e) {
            return null;
        }
        if (isSameVersions(names, contents, dirContents))
            return null;

        Pair<String[], byte[][]> filesToCommit = getFilesToCommit(names, contents, dirContents);

        if (filesToCommit.getKey().length == 0 || filesToCommit.getValue().length == 0)
            tempBytesToSend = new byte[0];
        try {
            tempBytesToSend = Zipper.zipMultiple(filesToCommit.getKey(), filesToCommit.getValue());
        } catch (IOException e) {
            return null;
        }
        String[] dirContentsArray = new String[dirContents.size()];
        for (int i = 0; i < dirContents.size(); i++)
            dirContentsArray[i] = dirContents.get(i).getKey();
        return new CommitPacket(dirContentsArray);
    }

    private boolean isSameVersions(String[] oldNames, byte[][] oldContents, List<Pair<String, byte[]>> dirContents){
        Map<String, byte[]> contentMap = dirContents.stream().collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        if (oldContents.length != dirContents.size())
            return false;
        byte[] tempContent;
        for (int i = 0; i < oldContents.length; i++){
            tempContent = contentMap.get(oldNames[i]);
            if (tempContent == null || !Arrays.equals(Md5Hash.getMd5Hash(tempContent), oldContents[i])){
                return false;
            }
        }
        return true;
    }

    private Pair<String[], byte[][]> getFilesToCommit(String[] md5Names, byte[][] md5Contents, List<Pair<String, byte[]>> dirContents){
        List<String> names = new ArrayList<>();
        List<byte[]> data = new ArrayList<>();
        boolean[] removed = new boolean[dirContents.size()];
        Map<String, byte[]> hashesMap = new HashMap<>();
        for (Pair<String, byte[]> pair: dirContents)
            hashesMap.put(pair.getKey(), Md5Hash.getMd5Hash(pair.getValue()));

        for (int j = 0; j < dirContents.size(); j++){
            Pair<String, byte[]> pair = dirContents.get(j);
            for (int i = 0; i < md5Names.length; i++){
                if (pair.getKey().equals(md5Names[i]))
                    if (Arrays.equals(hashesMap.get(pair.getKey()), md5Contents[i])){
                        removed[j] = true;
                        break;
                    }
                    else
                        break;
            }
        }
        for (int j = 0; j < dirContents.size(); j++) {
            if (removed[j])
                continue;

            Pair<String, byte[]> pair = dirContents.get(j);
            names.add(pair.getKey());
            data.add(pair.getValue());
        }
        String[] namesArray = new String[names.size()];
        byte[][] bytesArray = new byte[data.size()][];
        return new Pair<>(names.toArray(namesArray), data.toArray(bytesArray));
    }

    private ICommandPacket sendUpdatePacket(String[] command){
        tempHard = true;
        return new RevertPacket("", true);
    }

    private ICommandPacket sendRevertPacket(String[] command){
        String version = command[1];
        if (command.length == 3)
            if (command[2].equals("-hard")) {
                tempHard = true;
            }
        return new RevertPacket(version, tempHard);
    }

    private ICommandPacket sendLogPacket(String[] command){
        return new LogPacket("query");
    }

    private ICommandPacket sendEncryptPacket(String[] command){
        return new EncryptionPacket(command[1].getBytes(), command[0]);
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
