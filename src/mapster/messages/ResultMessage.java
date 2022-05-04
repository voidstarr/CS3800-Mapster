package mapster.messages;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class ResultMessage implements Serializable {
    public static class Result implements Serializable {
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

        @Override
        public String toString() {
            return "Result{" +
                    "ipAddress='" + ipAddress + '\'' +
                    ", port=" + port +
                    ", fileName='" + fileName + '\'' +
                    '}';
        }
    }

    List<Result> results;

    public ResultMessage(List<Result> results) {
        this.results = results;
    }

    public List<Result> getResults() {
        return results;
    }

    @Override
    public String toString() {
        return "ResultMessage{" +
                "results=" + results.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", ", "{", "}")) +
                '}';
    }
}
