package web_server;

import managment.Manager;
import perfomance.CommandFactory;
import perfomance.ICommandPacket;
import perfomance.instances.processors.Repo;
import thread_dispatcher.ThreadDispatcher;
import thread_dispatcher.ThreadedTask;
import utils.SimpleVersionIncrement;
import utils.data.FolderProvider;
import utils.data.IDataProvider;
import utils.data.NetDataTransporter;
import utils.encrypt.IEncryptor;
import utils.encrypt.XorEncryptor;
import utils.serializers.Serializer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class VersionControlServer extends WebServer {
    private ThreadDispatcher threadDispatcher;
    private VersionControl versionControl;
    private static final int fromPort;
    private static final int toPort;
    private static final Set<Integer> usedPorts;
    static {
        fromPort = 49152;
        toPort = 65535;
        usedPorts = new HashSet<>();
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

    private static ServerSocket createSocket(int port) throws IOException{
        return new ServerSocket(port);
    }

    static Integer getUnusedFreePort(){
        ServerSocket socket = null;
        for (int i = fromPort; i < toPort; i++){
            try {
                socket = createSocket(i);
                if (usedPorts.contains(i))
                    continue;
                usedPorts.add(i);
                return i;
            } catch (IOException e) {
                continue;
            }
            finally {
                closeStream(socket);
            }
        }
        return -1;
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
                InputStream is = null;
                OutputStream os = null;
                try
                {
                    is = m_socket.getInputStream();
                    os = m_socket.getOutputStream();

                    CommandFactory factory = new CommandFactory();
                    IEncryptor encryptor = new XorEncryptor();
                    NetDataTransporter transporter = new NetDataTransporter(encryptor, is, os);
                    Manager manager = new Manager(new Serializer(), transporter, factory);
                    Repo repo = new Repo(manager, versionControl, new FolderProvider(), new SimpleVersionIncrement(), encryptor);
                    manager.setCommandProcessor(repo);

                    while (true){
                        ICommandPacket response = repo.process(repo.get());
                        repo.send(response);
                    }
                }
                finally
                {
                    closeStream(os);
                    closeStream(is);
                    m_socket.close();
                }
            }
            catch (Exception ex)
            {
                System.err.println("client terminated with error: {1}" + ex); //TODO: del it
                ex.printStackTrace();
            }
        }
    }
}

