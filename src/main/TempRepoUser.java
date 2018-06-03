package main;

import managment.Manager;
import perfomance.CommandFactory;
import perfomance.instances.processors.User;
import utils.data.FolderProvider;
import utils.data.NetDataTransporter;
import utils.encrypt.IEncryptor;
import utils.encrypt.XorEncryptor;
import utils.serializers.Serializer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("Duplicates")
public class TempRepoUser {
    List<String> commands = new ArrayList<>();

    public TempRepoUser(String[] commands){
        Collections.addAll(this.commands, commands);
    }

    public void start(int port, InetAddress address){
        Socket socket = null;
        try {
            socket = new Socket(address, port);

            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            CommandFactory factory = new CommandFactory();
            IEncryptor encryptor = new XorEncryptor();
            NetDataTransporter transporter = new NetDataTransporter(encryptor, inputStream, outputStream);
            Manager manager = new Manager(new Serializer(), transporter, factory);
            User user = new User(manager, new FolderProvider(), address, encryptor, System.out);
            manager.setCommandProcessor(user);

            BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
            for (String command: commands) {
                String message = command;
                if (message.startsWith("commit")){
                    FolderProvider folderProvider = new FolderProvider();
                    folderProvider.setOrigin("D:\\IT\\ООП\\практика\\Репозиторий\\tests\\local\\repo");
                    folderProvider.write("1.txt", new byte[] {32, 54, 48, 39});
                    folderProvider.write("2.txt", "Im here now".getBytes());
                }
                if ("exit".equals(message.toLowerCase()))
                    break;
                user.sendPacket(message);
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
