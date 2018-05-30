package web_server;

import managment.Manager;
import perfomance.CommandFactory;
import perfomance.instances.processors.User;
import utils.data.FolderProvider;
import utils.data.NetDataTransporter;
import utils.encrypt.IEncryptor;
import utils.encrypt.XorEncryptor;
import utils.serializers.Serializer;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class RepoClient {

    public RepoClient(){

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
            User user = new User(manager, new FolderProvider(), address, encryptor);
            manager.setCommandProcessor(user);

            BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.println("Type your command");
                String message = consoleIn.readLine();
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
