package commonclasses.messages;

import lombok.Data;

@Data
public class NextFilePart implements Message {
    private int part;
    private long startByte;
    private int lastPartSize;
    private int allParts;
    private String fileName;
    private byte[] data;

    public NextFilePart(String fileName, int allParts, int currentPart, long startByte, int lastPart) {
        this.fileName = fileName;
        this.allParts = allParts;
        this.part = currentPart;
        this.startByte = startByte;
        this.lastPartSize = lastPart;
    }

    public NextFilePart(String fileName, int allParts, int currentPart, long startByte, int lastPart, byte[] data) {
        this.part = currentPart;
        this.startByte = startByte;
        this.lastPartSize = lastPart;
        this.allParts = allParts;
        this.fileName = fileName;
        this.data = data;
    }

    @Override
    public MessageType getType() {
        return MessageType.NEXT_PART;
    }
}
