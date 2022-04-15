package mapster.messages;

import java.io.Serializable;

public class JoinMessage implements Serializable {
    private final int port;

    public JoinMessage(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "JoinMessage{" +
                "port=" + port +
                '}';
    }
}

