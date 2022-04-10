package mapster.messages;

import java.io.Serializable;
import java.util.List;

public class ResultMessage implements Serializable {
    public static class Result {
        String ipAddress;
        int port;
        String fileName;

        public Result(String ipAddress, int port, String fileName) {
            this.ipAddress = ipAddress;
            this.port = port;
            this.fileName = fileName;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public int getPort() {
            return port;
        }

        public String getFileName() {
            return fileName;
        }
    }

    List<Result> results;

    public ResultMessage(List<Result> results) {
        this.results = results;
    }
}
