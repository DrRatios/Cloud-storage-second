package com.aleksgolds.cloud.storage.server;

import com.aleksgolds.cloud.storage.common.CommandsList;
import com.aleksgolds.cloud.storage.common.ProtoFileSender;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class ProtoHandler extends ChannelInboundHandlerAdapter {
    private enum Status {
        IDLE, COMMAND, FILE
    }

    private FileReceiver fileReceiver;
    private CommandReceiver commandReceiver;
    private Status currentStatus;
    private Runnable finishOperation = ()-> {
        currentStatus = Status.IDLE;
        System.out.println("Operation finished");
    };

    public ProtoHandler() {
        this.fileReceiver = new FileReceiver();
        this.commandReceiver = new CommandReceiver();
        this.currentStatus = Status.IDLE;
    }



    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentStatus == Status.IDLE) {
                byte controlByte = buf.readByte();
                if (controlByte == CommandsList.FILE_SIGNAL_BYTE){
                    currentStatus = Status.FILE;
                    fileReceiver.startReceive();
                } else if (controlByte == CommandsList.CMD_SIGNAL_BYTE) {
                    commandReceiver.startReceive();
                    currentStatus = Status.COMMAND;
                }
            }
            if (currentStatus == Status.FILE) {
                fileReceiver.receive(ctx, buf,finishOperation);
            }
            if (currentStatus == Status.COMMAND) {
                commandReceiver.receive(ctx, buf,finishOperation);
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
