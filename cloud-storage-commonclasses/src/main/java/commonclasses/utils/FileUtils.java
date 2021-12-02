package commonclasses.utils;


import commonclasses.messages.FullFile;
import commonclasses.messages.NextFilePart;
import io.netty.channel.socket.SocketChannel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;


@Slf4j
public class FileUtils {

    public final long SIZE_LIMIT = 1024 * 1024;
    private static FileUtils fileUtils;


    public static FileUtils getInstance() {
        if (fileUtils == null) {
            fileUtils = new FileUtils();
            return fileUtils;
        }
        return fileUtils;
    }


    @SneakyThrows
    public void sendFile(Path path, SocketChannel sc) {
        if (Files.size(path) > SIZE_LIMIT) {
            sendFileInParts(path, sc);
        } else {
            sc.writeAndFlush(new FullFile(path.getFileName().toString(), Files.readAllBytes(path)));
        }
    }

    @SneakyThrows
    public void saveFile(Path path, byte[] data) {
        Files.write(path, data, StandardOpenOption.CREATE);
    }

    @SneakyThrows
    public boolean savePart(Path path, byte[] data) {
        Files.write(path, data, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        return true;

    }

    @SneakyThrows
    private void sendFileInParts(Path path, SocketChannel sc) {

        int iter = (int) ((Files.size(path) + SIZE_LIMIT) / SIZE_LIMIT);
        int lastPartSize = (int) (Files.size(path) % SIZE_LIMIT);
        String fileName = path.getFileName().toString();
        byte[] data = readBytes(path,0, (int) SIZE_LIMIT);
        NextFilePart part = new NextFilePart(fileName,iter,1,0,lastPartSize,data);
        sc.writeAndFlush(part);



    }

    private byte[] readBytes(Path path, long startByte, int buf_size) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(path.toString(),"r");
        byte[] buf = new byte[buf_size];
        raf.seek(startByte);
        raf.read(buf);
        raf.close();
        return buf;

    }

    @SneakyThrows
    public NextFilePart sendNextPart(NextFilePart msg, Path path) {
        int currentPart = msg.getPart()+1;
        long currentStartByte = msg.getStartByte()+SIZE_LIMIT;
        if (currentPart==msg.getAllParts()){
            byte[] lastPart = readBytes(path, currentStartByte, msg.getLastPartSize());
          return new NextFilePart(msg.getFileName(),msg.getAllParts(), currentPart,currentStartByte,msg.getLastPartSize(), lastPart);
        }
        else {
            byte[] part = readBytes(path, currentStartByte, (int) SIZE_LIMIT);
            return new NextFilePart(msg.getFileName(),msg.getAllParts(), currentPart,currentStartByte,msg.getLastPartSize(), part);
        }


    }
}

