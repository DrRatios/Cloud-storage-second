package com.aleksgolds.cloud.storage.server;


import com.aleksgolds.cloud.storage.common.ProtoFileSender;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class CommandReceiver {

    public enum State {
        IDLE, COMMAND_LENGTH, COMMAND
    }

    private State currentState = State.IDLE;
    private int commandLength;
    private int receivedCommandLength;
    private StringBuilder cmd;

    public void startReceive() {
        currentState = State.COMMAND_LENGTH;
        cmd = new StringBuilder();
    }


    public void receive(ChannelHandlerContext ctx, ByteBuf buf, Runnable finishOperation) throws Exception {
        // 16 14 /request 1.txt
        if (currentState == State.COMMAND_LENGTH) {
            if (buf.readableBytes() >= 4) {
                System.out.println("STATE: Get command length");
                commandLength = buf.readInt();
                currentState = State.COMMAND;
                receivedCommandLength = 0;
                cmd.setLength(0);
            }
        }
        if (currentState == State.COMMAND) {
            while (buf.readableBytes() >= 0) {
                cmd.append((char)buf.readByte());
                receivedCommandLength++;
                if (receivedCommandLength == commandLength) {
                    parseCommand(ctx, cmd.toString());
                    currentState = State.IDLE;
                    finishOperation.run();
                    return;
                }
            }
        }
    }

    public void parseCommand(ChannelHandlerContext ctx, String cmd) throws IOException {
        if (cmd.startsWith("/request")) {
            String fileToClientName = cmd.split(" ")[1];
            ProtoFileSender.sendFile(Paths.get("server_repository", fileToClientName), ctx.channel(), null);
        }
    }
}
