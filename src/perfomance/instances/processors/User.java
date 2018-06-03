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
import utils.data.TransporterException;
import utils.encrypt.IEncryptor;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;


public class User implements ICommandProcessor {
    private final Manager manager;
    private IDataProvider dataProvider;
    private byte[] tempBytesToSend;
    private boolean tempHard;
    private InetAddress address;
    private IEncryptor encryptor;
    private final PrintStream printStream;

    public User(Manager manager, IDataProvider dataProvider, InetAddress address, IEncryptor encryptor, PrintStream printStream) {
        this.manager = manager;
        this.dataProvider = dataProvider;
        this.address = address;
        this.encryptor = encryptor;
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
            } catch (TransporterException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private Socket createSocket(int port){
        try {
            return new Socket(address, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void operateWithSocket(Socket socket, String command){
        InputStream inputStream = null;
        OutputStream output = null;
        ByteArrayOutputStream tempStream = null;
        try {
            if ("write".equals(command)) {
                output = socket.getOutputStream();
                if (tempBytesToSend != null)
                    output.write(encryptor.encrypt(tempBytesToSend));
                tempBytesToSend = null;
                printStream.println("Sending files...");
            } else if ("read".equals(command)) {
                inputStream = socket.getInputStream();
                tempStream = new ByteArrayOutputStream();
                int count;
                byte[] buffer = new byte[4096];
                count = inputStream.read(buffer);
                tempStream.write(buffer, 0, count);
                byte[] bytes = tempStream.toByteArray();

                printStream.println("Getting files");
                writeBytes(bytes);
            }
            else if ("notify".equals(command)){
                inputStream = socket.getInputStream();
                tempStream = new ByteArrayOutputStream();
                int count;
                byte[] buffer = new byte[4096];
                count = inputStream.read(buffer);
                tempStream.write(buffer, 0, count);
                byte[] bytes = tempStream.toByteArray();

                printStream.print(new String(Zipper.unzipOne(encryptor.decrypt(bytes)), "UTF-8"));
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        finally {
            close(inputStream);
            close(output);
            close(socket);
            close(tempStream);
            tempHard = false;
            tempBytesToSend = null;
        }
    }

    private void writeBytes(byte[] bytes){
        try {
            printStream.println("Saving files");
            if (tempHard)
                dataProvider.clearDirectory(dataProvider.getOrigin());
            List<Pair<String, byte[]>> files = Zipper.unzipMultiple(encryptor.decrypt(bytes));
            for (Pair<String, byte[]> pair: files){
                dataProvider.write(pair.getKey(), pair.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        String[] command = identifier.split(" "); // TODO: check command for correctness
        ICommandPacket packet = null;
        if (isValid(command)) {
            if (identifier.startsWith("xor")) {
                encryptor.setSecret(command[1]);
                packet = sendEncryptPacket(command);
            } else if (identifier.toLowerCase().startsWith("add")) {
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
        } catch (TransporterException e) {
            e.printStackTrace();
        }
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
            e.printStackTrace();
            return null;
        }
        List<Pair<String, byte[]>> dirContents;
        try {
            dirContents = dataProvider.walkThrough(dataProvider.getCurrentRoot());
        } catch (IOException e) {
            return null;
        }
        Pair<String[], byte[][]> filesToCommit = getFilesToCommit(names, contents, dirContents);

        if (filesToCommit.getKey().length == 0 || filesToCommit.getValue().length == 0)
            return null;
        try {
            tempBytesToSend = Zipper.zipMultiple(filesToCommit.getKey(), filesToCommit.getValue());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        String[] dirContentsArray = new String[dirContents.size()];
        for (int i = 0; i < dirContents.size(); i++)
            dirContentsArray[i] = dirContents.get(i).getKey();
        return new CommitPacket(dirContentsArray);
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
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
