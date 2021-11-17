package commonclasses.messages;

import lombok.Data;

@Data
public class FolderRequest implements Message {
    private String folderName;

    public FolderRequest(String folderName) {
        this.folderName = folderName;
    }

    @Override
    public MessageType getType() {
        return MessageType.FOLDER_REQUEST;
    }
}
