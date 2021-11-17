package commonclasses.messages;

public class NextFilePartToServer extends NextFilePart implements Message {
    public NextFilePartToServer(String fileName, int allParts, int currentPart, long startByte, int lastPart) {
        super(fileName, allParts, currentPart, startByte, lastPart);
    }

    @Override
    public MessageType getType() {
        return MessageType.NEXT_PART_TO_SERVER;
    }
}
