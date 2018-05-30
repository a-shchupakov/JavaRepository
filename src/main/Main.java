package main;

import javafx.util.Pair;
import perfomance.CommandFactory;
import managment.Manager;
import utils.data.FolderProvider;
import utils.data.NetDataTransporter;
import utils.encrypt.IEncryptor;
import utils.serializers.Serializer;
import utils.encrypt.XorEncryptor;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {
        try{
//            CommandFactory factory = new CommandFactory();
//
//            IEncryptor encryptor = new XorEncryptor("same super secret phrase".getBytes());
//
//            String root1 = "D:\\IT\\ООП\\практика\\Репозиторий\\tests\\junk_to_del1";
//            String root2 = "D:\\IT\\ООП\\практика\\Репозиторий\\tests\\junk_to_del2";
//
//            //StraightEncryptingDataTransporter transporter1 = new StraightEncryptingDataTransporter(encryptor);
//            //StraightEncryptingDataTransporter transporter2 = new StraightEncryptingDataTransporter(encryptor);
//
//            InetAddress ipAddress = InetAddress.getByName("127.0.0.1");
//            int port = 12345;
//
//            ServerSocket server = new ServerSocket(port);
//            Socket clientSocket = new Socket(ipAddress, port);
//            Socket client = server.accept();
//
//            NetDataTransporter transporter1 = new NetDataTransporter(encryptor, clientSocket.getInputStream(), clientSocket.getOutputStream());
//            NetDataTransporter transporter2 = new NetDataTransporter(encryptor, client.getInputStream(), client.getOutputStream());
//
//            Manager manager1 = new Manager(new Serializer(), transporter1, factory);
//            SimpleUser user1 = new SimpleUser(root1, "client", manager1);
//            manager1.setCommandProcessor(user1);
//
//            Manager manager2 = new Manager(new Serializer(), transporter2, factory);
//            SimpleUser user2 = new SimpleUser(root2, "server", manager2);
//            manager2.setCommandProcessor(user2);
//
//            System.out.println("Finished constructing");
////            user1.send(user1.createWritePacket("text.txt", "1.txt", "35.txt"));
////            user2.get();
////
////            user2.send(user2.createDeletePacket("text.txt"));
////            user1.get();


            FolderProvider provider = new FolderProvider();
            for (Pair<String, byte[]> pair: provider.walkThrough("D:\\IT\\ООП\\практика\\Репозиторий\\tests\\junk_to_del1"))
                System.out.println(pair.getKey());
        }

        catch (Exception e){
            System.out.println(e.getMessage());
        }

    }
}
