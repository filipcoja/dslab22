package dslab.threads;

import at.ac.tuwien.dsg.orvell.Shell;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public abstract class ListenerThread extends Thread {
    protected final ServerSocket serverSocket;
    protected final ExecutorService threadPool;
    protected final ArrayList<HandlerThread> threads;
    private final Shell shell;

    public ListenerThread(ServerSocket serverSocket, Shell shell) {
        this.serverSocket = serverSocket;
        this.shell = shell;
        // cached instead of fixed so there is no saturation => creates new threads in extreme circumstances
        threadPool = Executors.newCachedThreadPool();
        threads = new ArrayList<>();
    }

    public void run() {
        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                var thread = getHandlerThread(socket);
                threads.add(thread);
                threadPool.submit(thread);
            } catch (SocketException e) {
                // when the socket is closed, the I/O methods of the Socket will throw a SocketException
                // almost all SocketException cases indicate that the socket was closed
                // dont print anything here
                break;
            } catch (RejectedExecutionException ignored) {
                // ignore, pool was closed
            } catch (IOException e) {
                shell.out().println("Error IOException while creating sockets.");
            }
        }
    }

    public void shutdown() {
        threads.forEach(t -> t.shutdown(false));
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            // Ignored because we cannot handle it
        }
        threadPool.shutdown();
    }

    abstract protected HandlerThread getHandlerThread(Socket socket);
}
