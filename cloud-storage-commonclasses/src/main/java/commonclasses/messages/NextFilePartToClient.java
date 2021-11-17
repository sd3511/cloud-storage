package commonclasses.messages;

import lombok.Data;

@Data
public class NextFilePartToClient extends NextFilePart implements Message {
    private int part;
    private long startByte;
    private int lastPartSize;
    private int allParts;
    private String fileName;

    public NextFilePartToClient(String fileName, int allParts, int currentPart, long startByte, int lastPart) {
        super(fileName, allParts, currentPart, startByte, lastPart);
        this.fileName = fileName;
        this.allParts = allParts;
        this.part = currentPart;
        this.startByte = startByte;
        this.lastPartSize = lastPart;
    }




    @Override
    public MessageType getType() {
        return MessageType.NEXT_PART_TO_CLIENT;
    }
}

