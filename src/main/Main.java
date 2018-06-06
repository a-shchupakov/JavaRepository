package main;

import utils.data.FolderProvider;
import web_server.RepoClient;
import web_server.VersionControlServer;

import java.lang.reflect.Array;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        try {
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

            TempRepoUser autoClient = new TempRepoUser(new String[]{"add repo", "clone D:\\IT\\ООП\\практика\\Репозиторий\\tests\\local repo", "commit"});
            RepoClient manualClient = new RepoClient();

            //new Thread(() -> autoClient.start(port, ipAddress)).start();
            //new Thread(() -> autoClient.start(port, ipAddress)).start();
            //manualClient.start(port, ipAddress);

        } catch (Exception e) {

        }
    }
}
