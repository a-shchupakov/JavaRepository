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
import java.net.Socket;

public class VersionControlServer extends WebServer {
    private ThreadDispatcher threadDispatcher;
    private VersionControl versionControl;

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
                    IEncryptor encryptor = new XorEncryptor("same super secret phrase".getBytes());
                    NetDataTransporter transporter = new NetDataTransporter(encryptor, is, os);
                    Manager manager = new Manager(new Serializer(), transporter, factory);
                    Repo user = new Repo(versionControl, null, null);
                    manager.setCommandProcessor(user);

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
                System.err.println("client terminated with error: {1}" + ex);
            }
        }
    }
}

