package mapster.messages;

import java.io.Serializable;

public class PublishMessage implements Serializable {
    String keyword;
    String fileName;

    public PublishMessage(String keyword, String fileName) {
        this.keyword = keyword;
        this.fileName = fileName;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getFileName() {
        return fileName;
    }
}
