package main;

import web_server.RepoClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SuppressWarnings("Duplicates")
public class ManualRepoUser {
    public static void main(String[] args){
        try {
            InetAddress ipAddress = InetAddress.getByName("127.0.0.1");
            int port = 55557;

            RepoClient manualClient = new RepoClient();
            new Thread(() -> manualClient.start(port, ipAddress)).start();

        } catch (UnknownHostException e) { }
    }
}
