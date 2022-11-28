package dslab.threads;

import dslab.processors.CommandProcessor;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.function.Consumer;

public abstract class HandlerThread extends Thread {
    protected final Socket socket;
    protected final Consumer<HandlerThread> callback;


    public HandlerThread(Socket socket, Consumer<HandlerThread> callback) {
        this.socket = socket;
        this.callback = callback;
    }

    public void run() {
        while (true) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream());

                // set up command processor
                var commandProcessor = getCommandProcessor();
                commandProcessor.setWriter(writer);
                commandProcessor.sendInit();

                String request;
                // read client requests
                while ((request = reader.readLine()) != null) {
                    boolean shouldEnd = commandProcessor.handleInput(request);
                    if (shouldEnd) break;
                }
            } catch (SocketException e) {
                // when the socket is closed, the I/O methods of the Socket will throw a SocketException
                // almost all SocketException cases indicate that the socket was closed
                // dont do anything
                break;
            } catch (IOException e) {
                // you should properly handle all other exceptions
                throw new UncheckedIOException(e);
            } finally {
                shutdown(true);
            }

        }
    }

    protected void shutdown(boolean calledByMyself) {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // Ignored because we cannot handle it
        }
        // call the callback function with this thread so it can be removed from the list
        if (calledByMyself) callback.accept(this);
    }

    protected abstract CommandProcessor getCommandProcessor();
}
