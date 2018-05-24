package web_server;

import java.net.ServerSocket;
import java.net.Socket;

public abstract class WebServer {
    protected final int port;

    public WebServer(int port){
        this.port = port;
    }

    public void start(){
        try {
            ServerSocket server = new ServerSocket(port);
            try {
                while (true) {
                    Socket client = server.accept();
                    handleClient(client);
                }
            } finally {
                server.close();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    protected abstract void handleClient(Socket client);
}
