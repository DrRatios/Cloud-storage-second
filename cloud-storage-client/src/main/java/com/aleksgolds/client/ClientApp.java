package com.aleksgolds.client;


import com.aleksgolds.common.CommandsList;
import com.aleksgolds.common.DBAuthService;
import com.aleksgolds.common.ProtoFileSender;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLOutput;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;


public class ClientApp {

    private static boolean auth = false;
    private static String nick;
    private static final Logger log = LogManager.getLogger();

    public static String getNick() {
        return nick;
    }

    public static void main(String[] args) throws Exception {
//        CountDownLatch connectionOpened = new CountDownLatch(1);
//        new Thread(() -> Network.getInstance().start(connectionOpened)).start();
//        connectionOpened.await();
        System.out.println("Введите логин и пароль(/auth login password)");
        Scanner sc = new Scanner(System.in);
        while (true) {
            String cmd = sc.nextLine();
            if (!auth) {
                if (cmd.startsWith("/auth")) {
                    String login = cmd.split(" ")[1];
                    String password = cmd.split(" ")[2];
                    nick = DBAuthService.getNickByLoginAndPassword(login, password);
                    if (nick.isEmpty()) {
                        log.info("Логин и пароль неверны");
                        continue;
                    } else{
                        CountDownLatch connectionOpened = new CountDownLatch(1);
                        new Thread(() -> Network.getInstance().start(connectionOpened)).start();
                        connectionOpened.await();
                        ProtoFileSender.sendAuthInfo(Network.getInstance().getCurrentChannel(), login, password);
                        Path serverStorage = Paths.get("client_repository", nick);
                        if (!Files.exists(serverStorage)) {
                            try {
                                Files.createDirectory(serverStorage);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        auth = true;
                        log.info("authOK");
                        continue;
                    }

                }
            }
            if (auth) {
                if (cmd.equals("/exit")) {
                    break;
                }
                if (cmd.startsWith("/send ")) {
                    String filename = cmd.split("\\s")[1];
                    Path filePath = Paths.get("client_repository/"+nick, filename);
                    if (!Files.exists(filePath)) {
                        System.out.println("Файл для отправки не найден в репозитории");
                        continue;
                    }
                    ProtoFileSender.sendFile(filePath, Network.getInstance().getCurrentChannel(), future -> {
                        if (!future.isSuccess()) {
                            System.out.println("Не удалось отправить файл на сервер");
                            future.cause().printStackTrace();
                        }
                        if (future.isSuccess()) {
                            System.out.println("Файл успешно передан");
                        }
                    });
                    continue;
                }
                if (cmd.startsWith("/download ")) {
                    String filename = cmd.split("\\s")[1];
                    ProtoFileSender.sendFileRequest(filename, Network.getInstance().getCurrentChannel());
                    continue;
                }
                if (cmd.equals("/serverFiles")) {
                    ProtoFileSender.sendServerFilesRequest(Network.getInstance().getCurrentChannel());
                    continue;
                }
                System.out.println("Введена неверная команда, попробуйте снова");
            }
        }
    }
}

//    public static void sendFileRequest(String filename, Channel outChannel) {
//        byte[] filenameBytes = ("/request " + filename).getBytes(StandardCharsets.UTF_8);
//        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + filenameBytes.length);
//        buf.writeByte(CommandsList.CMD_SIGNAL_BYTE);
//        buf.writeInt(filenameBytes.length);
//        buf.writeBytes(filenameBytes);
//        outChannel.writeAndFlush(buf);
//    }
//
//    public static void sendAuthInfo(Channel channel, String login, String password) {
//        byte[] loginBytes = login.getBytes(StandardCharsets.UTF_8);
//        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
//        int bufLength = 4 + loginBytes.length + 4 + passwordBytes.length;
//        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(bufLength);
/// /        buf.writeByte(CommandsList.CMD_SIGNAL_BYTE);
//        buf.writeInt(loginBytes.length);
//        buf.writeBytes(loginBytes);
//        buf.writeInt(passwordBytes.length);
//        buf.writeBytes(passwordBytes);
//        channel.writeAndFlush(buf);
//    }

