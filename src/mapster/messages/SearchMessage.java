package mapster.messages;

import java.io.Serializable;

public class SearchMessage implements Serializable {
    String keyword;

    public SearchMessage(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }

    @Override
    public String toString() {
        return "SearchMessage{" +
                "keyword='" + keyword + '\'' +
                '}';
    }
}
