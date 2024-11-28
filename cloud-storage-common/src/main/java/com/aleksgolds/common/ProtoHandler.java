package com.aleksgolds.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

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

    public ProtoHandler(String rootDir, CommandReceiver commandReceiver) {
        this.fileReceiver = new FileReceiver(rootDir);
        this.commandReceiver = commandReceiver;
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
