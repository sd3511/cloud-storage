package commonclasses.messages;

public class CreateDirRequest implements Message {
    private String dirName;

    public CreateDirRequest(String dirName) {
        this.dirName = dirName;
    }

    public String getDirName() {
        return dirName;
    }

    @Override
    public MessageType getType() {
        return MessageType.CREATE_DIR;
    }
}
