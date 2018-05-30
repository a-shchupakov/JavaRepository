package main;

import javafx.util.Pair;
import perfomance.CommandFactory;
import managment.Manager;
import perfomance.instances.processors.User;
import utils.data.FolderProvider;
import utils.data.NetDataTransporter;
import utils.encrypt.IEncryptor;
import utils.serializers.Serializer;
import utils.encrypt.XorEncryptor;
import web_server.RepoClient;
import web_server.VersionControlServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {
        try {
            String root1 = "D:\\IT\\ООП\\практика\\Репозиторий\\tests\\junk_to_del1";
            String root2 = "D:\\IT\\ООП\\практика\\Репозиторий\\tests\\junk_to_del2";

            InetAddress ipAddress = InetAddress.getByName("127.0.0.1");
            int port = 55557;
            FolderProvider dataProvider = new FolderProvider();
            String repoDir = "D:\\IT\\ООП\\практика\\Репозиторий\\tests\\repos";
            dataProvider.setOrigin(repoDir);

            VersionControlServer server = new VersionControlServer(port, dataProvider, repoDir);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    server.start();
                }
            }).start();

            //TempRepoUser client = new TempRepoUser(new String[] {"add repo", "clone D:\\IT\\ООП\\практика\\Репозиторий\\tests\\local repo", "commit", "commit"});
            RepoClient client = new RepoClient();
            client.start(port, ipAddress);
        }

        catch (Exception e){
            e.printStackTrace();
        }
    }
}
