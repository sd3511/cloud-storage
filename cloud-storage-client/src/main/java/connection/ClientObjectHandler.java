package connection;


import commonclasses.messages.*;
import commonclasses.warningmessages.WarningMessageAuth;
import gui.AuthController;
import gui.CloudAreaController;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class ClientObjectHandler extends SimpleChannelInboundHandler<Message> {


    private final AuthController authController;
    private CloudAreaController cl;
    private SocketChannel sc;



    public ClientObjectHandler(AuthController authController) {
        this.authController = authController;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        sc = (SocketChannel) ctx.channel();




    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        switch (msg.getType()) {

            case LIST_REQUEST:
                ListRequest lr = (ListRequest) msg;
                cl.refreshServerList(lr);
                break;
            case FULL_FILE:
                saveFile(msg);
                break;
            case NEXT_PART_TO_CLIENT:
                sendNextPart(msg);
                break;
            case NEXT_PART:
                saveFilePart(msg);
                break;
            case SUCCESSFUL_AUTH:
                authOk(ctx);
                break;
            case REGISTRATION_SUCCESSFUL:
                registerOk();
                break;
            case WARNING_AUTH:
                warningHandle(msg);
        }


    }

    private void warningHandle(Message message) {
        WarningMessageAuth msg = (WarningMessageAuth)message;
        authController.alertWarning(msg.getWarningMessage());
    }

    private void registerOk() {
        authController.registerOk();
    }


    private void authOk(ChannelHandlerContext ctx) {
        authController.switchWindow();
        cl = authController.getCl();


    }

    private void sendNextPart(Message message) {
        NextFilePartToClient msg = (NextFilePartToClient) message;

        Path path = Paths.get(cl.getCurrentDir().toString(), msg.getFileName());
        sc.writeAndFlush(cl.getNetwork().getFileUtils().sendNextPart(msg, path));
    }


    private void saveFilePart(Message message) {
        NextFilePart msg = (NextFilePart) message;

        Path savePath = Paths.get(cl.getCurrentDir().toString(), msg.getFileName());
        int part = msg.getPart();
        int allParts = msg.getAllParts();

        long startByte = msg.getStartByte();
        int lastPart = msg.getLastPartSize();

        if (cl.getNetwork().getFileUtils().savePart(savePath, msg.getData())) {
            if (part == allParts) {
                cl.fileInfo(savePath.getParent());
                return;
            }
            sc.writeAndFlush(new NextFilePartToServer(msg.getFileName(), allParts, part, startByte, lastPart));
        }
    }

    private void saveFile(Message message) {
        FullFile msg = (FullFile) message;
        Path savePath = Paths.get(cl.getCurrentDir().toString(), msg.getFileName());
        cl.getNetwork().getFileUtils().saveFile(savePath, msg.getData());
    }


}



