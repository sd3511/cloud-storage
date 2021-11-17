package commonclasses.utils;
import lombok.Data;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
public class FileInfo implements Serializable {

    private String filename;
    private boolean isFolder;
    private long size;
    private boolean Chosen;


    @SneakyThrows
    public FileInfo(Path path) {
        this.filename = path.getFileName().toString();
        this.isFolder = Files.isDirectory(path);
        this.size = Files.size(path);
        this.Chosen = false;

    }
}
