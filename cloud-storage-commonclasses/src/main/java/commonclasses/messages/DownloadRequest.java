package commonclasses.messages;

import lombok.Data;

@Data
public class DownloadRequest implements Message {
    private String fileName;

    public DownloadRequest(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public MessageType getType() {
        return MessageType.DOWNLOAD_REQUEST;
    }
}
