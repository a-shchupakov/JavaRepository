package main;

import utils.data.FolderProvider;
import web_server.RepoClient;
import web_server.VersionControlServer;

import java.net.InetAddress;

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

            TempRepoUser client = new TempRepoUser(new String[]{"add repo", "clone D:\\IT\\ООП\\практика\\Репозиторий\\tests\\local repo", "commit", "commit"});
            RepoClient client1 = new RepoClient();
            //client.start(port, ipAddress);
            client1.start(port, ipAddress);
            //client1.start(port, ipAddress);
        } catch (Exception e) {

        }
    }
}
