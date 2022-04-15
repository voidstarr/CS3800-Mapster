package mapster.messages;

import java.io.Serializable;

public class DownloadMessage implements Serializable {
    String fileName;

    public DownloadMessage(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return "DownloadMessage{" +
                "fileName='" + fileName + '\'' +
                '}';
    }
}
