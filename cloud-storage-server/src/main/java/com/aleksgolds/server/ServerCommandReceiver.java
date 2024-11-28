package com.aleksgolds.server;

import com.aleksgolds.common.CommandReceiver;
import com.aleksgolds.common.ProtoFileSender;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.file.Paths;

public class ServerCommandReceiver extends CommandReceiver {
    private String rootDir;
    public ServerCommandReceiver(String rootDir) {
        this.rootDir = rootDir;
    }

    @Override
    public void parseCommand(ChannelHandlerContext ctx, String cmd) throws IOException {
        if(cmd.startsWith("/request ")) {
            String fileToClientName = cmd.split("\\s")[1];
            ProtoFileSender.sendFile(Paths.get(rootDir, fileToClientName), ctx.channel(), null);
        }
        if(cmd.equals("/serverFilesList")) {
            ProtoFileSender.sendServerListFile(rootDir, ctx.channel());
        }
    }
}
