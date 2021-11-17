package connection;

import commonclasses.authmessages.SignInMessage;
import commonclasses.messages.DownloadRequest;
import commonclasses.messages.Message;
import commonclasses.utils.FileUtils;
import gui.AuthController;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;


@Slf4j
public class Network {

    private final FileUtils fileUtils = FileUtils.getInstance();
    private SocketChannel socketChannel;
    private AuthController authController;


    public Network(AuthController authController) {
        this.authController = authController;
        startConnectionAuth();
    }

    public FileUtils getFileUtils() {
        return fileUtils;
    }

    public void startConnectionAuth() {
        Thread connection = new Thread(() -> {
            EventLoopGroup worker = new NioEventLoopGroup();

            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(worker)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                socketChannel = ch;
                                ch.pipeline().addLast(
                                        new ObjectDecoder(1024 * 1024 * 2, ClassResolvers.cacheDisabled(null)),
                                        new ObjectEncoder(),
                                        new ClientObjectHandler(authController)

                                );

                            }
                        });
                ChannelFuture channelFuture = bootstrap.connect("localhost", 8188).sync();
                channelFuture.channel().closeFuture().sync();

            } catch (Exception e) {
                log.debug("E = ", e);
            } finally {

                worker.shutdownGracefully();
            }
        });
        connection.setDaemon(true);
        connection.start();

    }


    public void close() {
        socketChannel.close();
    }

    public void sendFile(Path path) {
        fileUtils.sendFile(path, socketChannel);


    }

    public void sendDownloadRequest(String fileName) {
        socketChannel.writeAndFlush(new DownloadRequest(fileName));

    }

    public void signIn(String text, String text1) {
        socketChannel.writeAndFlush(new SignInMessage(text, text1));
        //  socketChannel.writeAndFlush(new DownloadRequest(text));
    }

    public void send(Message message) {
        socketChannel.writeAndFlush(message);
    }
}
