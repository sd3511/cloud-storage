package commonclasses.messages;

public class DeleteRequest implements Message {
    private String fileName;

    public DeleteRequest(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public MessageType getType() {
        return MessageType.DELETE_REQUEST;
    }
}
