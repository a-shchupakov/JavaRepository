package main;

import managment.Manager;
import perfomance.CommandFactory;
import perfomance.instances.processors.User;
import utils.data.FolderProvider;
import utils.data.NetDataTransporter;
import utils.serializers.Serializer;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("Duplicates")
class TempRepoUser {
    List<String> commands = new ArrayList<>();

    TempRepoUser(String[] commands){
        Collections.addAll(this.commands, commands);
    }

    public static void main(String[] args) {
        try {
            InetAddress ipAddress = InetAddress.getByName("127.0.0.1");
            int port = 55557;

            TempRepoUser autoClient = new TempRepoUser(new String[]{"add repo", "clone D:\\IT\\ООП\\практика\\Репозиторий\\tests\\local repo", "commit"});
            new Thread(() -> autoClient.start(port, ipAddress)).start();

        } catch (UnknownHostException e) { }

    }

    void start(int port, InetAddress address){
        Socket socket = null;
        try {
            socket = new Socket(address, port);

            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            CommandFactory factory = new CommandFactory();
            NetDataTransporter transporter = new NetDataTransporter(inputStream, outputStream);
            Manager manager = new Manager(new Serializer(), transporter, factory);
            User user = new User(manager, new FolderProvider(), address, System.out);
            manager.setCommandProcessor(user);

            for (String command: commands) {
                if (command.startsWith("commit")){
                    FolderProvider folderProvider = new FolderProvider();
                    folderProvider.setOrigin("D:\\IT\\ООП\\практика\\Репозиторий\\tests\\local\\repo");
                    folderProvider.write("1.txt", new byte[] {32, 54, 48, 39});
                    folderProvider.write("2.txt", "Im here now".getBytes());
                }
                if ("exit".equals(command.toLowerCase()))
                    break;
                user.sendPacket(command);
                user.process(user.get());
            }
            System.out.println("Exiting");
        }
        catch (Exception e){
            return;
        }
        finally {
            try {
                socket.close();
            }
            catch (Exception e) {
                return;
            }
        }
    }
}