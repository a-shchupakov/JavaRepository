package web_server;

import managment.Manager;
import perfomance.CommandFactory;
import perfomance.instances.processors.Repo;
import thread_dispatcher.ThreadDispatcher;
import thread_dispatcher.ThreadedTask;
import utils.data.IDataProvider;
import utils.data.NetDataTransporter;
import utils.encrypt.IEncryptor;
import utils.encrypt.XorEncryptor;
import utils.serializers.Serializer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class VersionControlServer extends WebServer {
    private ThreadDispatcher threadDispatcher;
    private VersionControl versionControl;
    public static int[] portPull;
    static {
        portPull = new int[] {12345, 23456, 54422, 32456, 42376, 55867, 44333, 33444 };
    }

    public VersionControlServer(int port, IDataProvider dataProvider, String repoDirectory){
        super(port);
        versionControl = new VersionControl(dataProvider, repoDirectory);
        threadDispatcher = ThreadDispatcher.getInstance();
    }
    @Override
    protected void handleClient(Socket client) {
        ClientServant servant = new ClientServant(versionControl, client);
        threadDispatcher.add(new ThreadedTask() {
            @Override
            public void run() {
                servant.run();
            }
        });
    }

    public static void closeStream(Closeable stream) {
        if (stream != null)
        {
            try
            {
                stream.close();
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    public static ServerSocket createSocket() throws IOException{
        for (int port : portPull) {
            try {
                return new ServerSocket(port);
            } catch (IOException ex) {
                continue; // try next port
            }
        }

        throw new IOException("No free port available");
    }

    static class ClientServant implements Runnable {
        private Socket m_socket;
        private VersionControl versionControl;

        public ClientServant(VersionControl versionControl, Socket socket) {
            m_socket = socket;
            this.versionControl = versionControl;
        }

        public void run()
        {
            try
            {
                InputStream is;
                OutputStream os;
                try
                {
                    is = m_socket.getInputStream();
                    os = m_socket.getOutputStream();

                    CommandFactory factory = new CommandFactory();
                    IEncryptor encryptor = new XorEncryptor();
                    NetDataTransporter transporter = new NetDataTransporter(encryptor, is, os);
                    Manager manager = new Manager(new Serializer(), transporter, factory);
                    Repo user = new Repo(manager, versionControl, null, null);
                    manager.setCommandProcessor(user);

                }
                finally
                {
//                    closeStream(os);
//                    closeStream(is);
//                    m_socket.close();
                }
            }
            catch (Exception ex)
            {
                System.err.println("client terminated with error: {1}" + ex);
            }
        }
    }
}

