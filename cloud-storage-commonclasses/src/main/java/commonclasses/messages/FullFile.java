package commonclasses.messages;

import lombok.Data;

@Data
public class FullFile implements Message {
    private String fileName;
    private byte[] data;
    public FullFile(String fileName, byte[] data) {
        this.fileName = fileName;
        this.data = data;
    }

    @Override
    public MessageType getType() {
        return MessageType.FULL_FILE;
    }
}
