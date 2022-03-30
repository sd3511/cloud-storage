package serverclasses;

import commonclasses.authmessages.RegisterSuccessful;
import commonclasses.authmessages.RegistrationMessage;
import commonclasses.authmessages.SignInMessage;
import commonclasses.authmessages.SuccessfulAuth;
import commonclasses.messages.*;
import commonclasses.utils.FileInfo;
import commonclasses.utils.FileUtils;
import commonclasses.warningmessages.WarningMessage;
import dbclasses.AuthUsers;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class ServerObjectHandler extends SimpleChannelInboundHandler<Message> {
    private SocketChannel sc;
    private Path serverDir;
    private Path currentDir;
    private FileUtils fileUtils;
    private AuthUsers authUsers;
    private String login;
    private ChannelHandlerContext ctx;
    private WatchService watchService;
    private WatchKey register;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Client connected");
        sc = (SocketChannel) ctx.channel();
        this.ctx = ctx;
        authUsers = AuthUsers.getInstance();
        fileUtils = FileUtils.getInstance();
        List<String> authorizedUsers = authUsers.getAuthorizedUsers();
        /*for (String authorizedUser : authorizedUsers) {
            log.debug("USERS {}", authorizedUser);
        }*/

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Client disconnected");
        currentDir = null;
        serverDir = null;
        authUsers.disconnect(login);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {

        switch (msg.getType()) {

            case LIST_REQUEST:
                listFiles(ctx, currentDir);
                break;
            case FULL_FILE:
                saveFullFile(msg);
                break;
            case DOWNLOAD_REQUEST:
                sendFile(msg);
                break;
            case NEXT_PART:
                saveFilePart(msg);
                break;
            case NEXT_PART_TO_SERVER:
                sendNextPart(msg);
                break;
            case START_AUTH:
                startAuth(msg, ctx);
                break;
            case FOLDER_REQUEST:
                listFolder(msg);
                break;
            case REGISTRATION_REQUEST:
                registration(msg);
                break;
            case DELETE_REQUEST:
                deleteFile(msg);
                break;
            case CREATE_DIR:
                createDir(msg);
                break;
            case RENAME_REQUEST:
                renameFile(msg);
                break;


        }
    }

    private void renameFile(Message message) throws IOException {
        RenameRequest msg = (RenameRequest)message;
        Path renamePath = Paths.get(currentDir.toAbsolutePath().toString(), msg.getFileNameOld());
        Files.move(renamePath, renamePath.resolveSibling(msg.getFileNameNew()));
    }


    private void createDir(Message message) {
        CreateDirRequest msg = (CreateDirRequest)message;
        Path createDirPath = Paths.get(currentDir.toAbsolutePath().toString(), msg.getDirName());
        try {
            Files.createDirectory(createDirPath);
        } catch (IOException e) {
            sc.writeAndFlush(new WarningMessage("Папка не создана"));
        }
    }


    private void deleteFile(Message message) throws IOException {
        DeleteRequest msg = (DeleteRequest)message;
        Path deletePath = Paths.get(currentDir.toAbsolutePath().toString(),msg.getFileName());
        if (Files.isDirectory(deletePath)){
            Files.walk(deletePath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }else {
            try {
                Files.delete(deletePath);
            } catch (IOException e) {
                sc.writeAndFlush(new WarningMessage("Файл не удален"));
            }
        }

    }

    private void registration(Message message) {
        RegistrationMessage msg = (RegistrationMessage) message;
        if (authUsers.register(msg.getLogin(), msg.getPassword())) {
            sc.writeAndFlush(new RegisterSuccessful());
        } else {
            //TODO
        }

    }

    @SneakyThrows
    private void listFolder(Message msg) {
        register.cancel();
        FolderRequest fr = (FolderRequest) msg;
        if (fr.getFolderName().equals("/up")) {
            currentDir = currentDir.getParent();
        } else {
            currentDir = Paths.get(currentDir.toAbsolutePath().toString(), fr.getFolderName());
        }
        listFiles(ctx, currentDir);
        register = currentDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);


    }


    @SneakyThrows
    private void startAuth(Message message, ChannelHandlerContext ctx) {

        SignInMessage msg = (SignInMessage) message;
        login = msg.getLogin();
        Message resultAuth = authUsers.authorize(msg.getLogin(), msg.getPassword());
        if (resultAuth instanceof SuccessfulAuth) {
            ctx.writeAndFlush(new SuccessfulAuth());
            serverDir = Paths.get(msg.getLogin());
            if (!Files.exists(serverDir)) {
                Files.createDirectory(serverDir);
            }
            currentDir = serverDir;
            listFiles(ctx, currentDir);
            watcherDir();
            register = currentDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        }else {
           ctx.writeAndFlush(resultAuth);
        }


    }


    private void sendNextPart(Message message) {
        NextFilePartToServer msg = (NextFilePartToServer) message;
        Path path = Paths.get(currentDir.toString(), msg.getFileName());
        sc.writeAndFlush(fileUtils.sendNextPart(msg, path));
    }


    private void saveFilePart(Message message) {

        NextFilePart msg = (NextFilePart) message;


        Path savePath = Paths.get(currentDir.toString(), msg.getFileName());
        int part = msg.getPart();
        int allParts = msg.getAllParts();
        long startByte = msg.getStartByte();
        int lastPart = msg.getLastPartSize();

        if (fileUtils.savePart(savePath, msg.getData())) {
            if (part == allParts) {
                return;
            }
            sc.writeAndFlush(new NextFilePartToClient(msg.getFileName(), allParts, part, startByte, lastPart));
        }


    }

    private void sendFile(Message message) {
        DownloadRequest msg = (DownloadRequest) message;
        Path savePath = Paths.get(currentDir.toString(), msg.getFileName());
        if (Files.exists(savePath)) {
            fileUtils.sendFile(savePath, sc);
        }
    }

    private void saveFullFile(Message message) {
        FullFile msg = (FullFile) message;
        Path savePath = Paths.get(currentDir.toString(), msg.getFileName());
        fileUtils.saveFile(savePath, msg.getData());
    }


    private void listFiles(ChannelHandlerContext sc, Path dir) throws IOException {

        List<FileInfo> collect = Files.list(dir).map(FileInfo::new).collect(Collectors.toList());
        if (currentDir.toAbsolutePath().equals(serverDir.toAbsolutePath())) {
            sc.writeAndFlush(new ListRequest(collect, true));
        } else {
            sc.writeAndFlush(new ListRequest(collect));
        }

    }


    private void watcherDir() throws Exception {
        watchService = FileSystems.getDefault().newWatchService();
        Thread watchServe = new Thread(() -> {
            try {
                while (true) {
                    log.debug("Wait events...");
                    WatchKey watchKey = watchService.take();
                    List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
                    for (WatchEvent<?> watchEvent : watchEvents) {
                        log.debug("1 - {}, 2- {}", watchEvent.context(), watchEvent.kind());
                    }
                    listFiles(ctx, currentDir);
                    watchKey.reset();
                }
            } catch (Exception e) {
                log.error("E=", e);
            }
        });
        watchServe.setDaemon(true);
        watchServe.start();

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        log.error("E serv = ", cause);
    }
}


