package web_server;

import thread_dispatcher.ThreadDispatcher;
import thread_dispatcher.ThreadedTask;
import java.io.*;
import java.net.Socket;

public class VersionControl extends WebServer {
    private ThreadDispatcher threadDispatcher;

    public VersionControl(int port){
        super(port);
        threadDispatcher = ThreadDispatcher.getInstance();
    }

    @Override
    protected void handleClient(Socket client) {
        ClientServant servant = new ClientServant(client);
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

        public ClientServant(Socket socket) {
            m_socket = socket;
        }

        public void run()
        {
            try
            {
                InputStream is = null;
                OutputStream os = null;
                try
                {
                    // тут надо взять еще System.in и из него читать комманды
                    is = m_socket.getInputStream();
                    os = m_socket.getOutputStream();
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

