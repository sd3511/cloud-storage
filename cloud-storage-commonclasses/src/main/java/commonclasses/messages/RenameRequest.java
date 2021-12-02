package commonclasses.messages;

public class RenameRequest implements Message {
    private String fileNameOld;
    private String fileNameNew;

    public RenameRequest(String fileNameOld, String fileNameNew) {
        this.fileNameOld = fileNameOld;
        this.fileNameNew = fileNameNew;
    }

    public String getFileNameOld() {
        return fileNameOld;
    }

    public String getFileNameNew() {
        return fileNameNew;
    }

    @Override
    public MessageType getType() {
        return MessageType.RENAME_REQUEST;
    }
}
