package mapster.messages;

import java.io.Serializable;

public class DownloadResponseMessage implements Serializable {
    String fileName;
    byte[] fileContent;

    public DownloadResponseMessage(String fileName, byte[] fileContent) {
        this.fileName = fileName;
        this.fileContent = fileContent;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileContent() {
        return fileContent;
    }
}
