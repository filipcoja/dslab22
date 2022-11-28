package dslab.processors;

import java.io.PrintWriter;

public abstract class CommandProcessor {
    protected PrintWriter writer;

    protected void send(String response) {
        writer.println(response);
        writer.flush();
    }

    protected void sendOk() {
        send("ok");
    }

    protected void sendBye() {
        send("ok bye");
    }

    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }

    public boolean handleInput(String request) {
        String[] commandParts = request.split(" ");
        String type = commandParts[0];

        return handleType(type, commandParts);
    }

    abstract protected boolean handleType(String type, String[] commandParts);
    abstract public void sendInit();
}
