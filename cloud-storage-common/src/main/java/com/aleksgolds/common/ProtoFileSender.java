package com.aleksgolds.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ProtoFileSender {
    public static void sendFile(Path path, Channel channel, ChannelFutureListener finishListener) throws IOException {
        FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));
        byte[] filenameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);
        // 1 + 4 + filenameBytes.length + 8 -> SIGNAL_BYTE FILENAME_LENGTH(int) + FILENAME + FILE_LENGTH(long)
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + filenameBytes.length + 8);
        buf.writeByte(CommandsList.FILE_SIGNAL_BYTE);
        buf.writeInt(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        buf.writeLong(Files.size(path));
        channel.writeAndFlush(buf);

        ChannelFuture transferOperationFuture = channel.writeAndFlush(region);
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
    }

    public static void sendCommand(Channel channel, byte command) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte(command);
        channel.writeAndFlush(buf);
    }
    public static void sendFileRequest(String filename, Channel outChannel) {
        byte[] filenameBytes = ("/request " + filename).getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + filenameBytes.length);
        buf.writeByte(CommandsList.CMD_SIGNAL_BYTE);
        buf.writeInt(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        outChannel.writeAndFlush(buf);
    }
    public static void sendServerFilesRequest(Channel outChannel) {
        byte[] filenameBytes = ("/serverFilesList").getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + filenameBytes.length);
        buf.writeByte(CommandsList.CMD_SIGNAL_BYTE);
        buf.writeInt(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        outChannel.writeAndFlush(buf);
    }
    public static void sendServerListFile(String path, Channel outChannel) throws IOException {
        List <String> serverFilesList = FileList.fileNameList(path);
        byte[] serverFilesNameListBytes = ("/serverFilesList " + serverFilesList).getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + serverFilesNameListBytes.length);
        buf.writeByte(CommandsList.CMD_SIGNAL_BYTE);
        buf.writeInt(serverFilesNameListBytes.length);
        buf.writeBytes(serverFilesNameListBytes);
        outChannel.writeAndFlush(buf);
    }

    public static void sendAuthInfo(Channel channel, String login, String password) {
        byte[] loginBytes = login.getBytes(StandardCharsets.UTF_8);
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        int bufLength = 4 + loginBytes.length + 4 + passwordBytes.length;
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(bufLength);
//        buf.writeByte(CommandsList.CMD_SIGNAL_BYTE);
        buf.writeInt(loginBytes.length);
        buf.writeBytes(loginBytes);
        buf.writeInt(passwordBytes.length);
        buf.writeBytes(passwordBytes);
        channel.writeAndFlush(buf);
    }
}