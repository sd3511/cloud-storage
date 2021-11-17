package commonclasses.messages;

import commonclasses.utils.FileInfo;
import lombok.Data;

import java.util.List;

@Data
public class ListRequest implements Message {
    private List<FileInfo> files;
    private boolean isHead;

    public ListRequest() {
    }

    public ListRequest(List<FileInfo> files) {
        this.files = files;
    }
    public ListRequest(List<FileInfo> files, boolean isHead) {
        this.files = files;
        this.isHead = isHead;
    }
    @Override
    public MessageType getType() {
        return MessageType.LIST_REQUEST;
    }
}
