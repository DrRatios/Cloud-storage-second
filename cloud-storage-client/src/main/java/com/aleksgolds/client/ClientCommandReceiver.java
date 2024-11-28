package com.aleksgolds.client;

import com.aleksgolds.common.CommandReceiver;
import com.aleksgolds.common.CommandsList;
import com.aleksgolds.common.ProtoFileSender;
import io.netty.channel.ChannelHandlerContext;

import javax.naming.OperationNotSupportedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClientCommandReceiver extends CommandReceiver {
    @Override
    public void parseCommand(ChannelHandlerContext ctx, String cmd) throws OperationNotSupportedException {

        if (cmd.startsWith("/serverFilesList ")) {
//            List<String> s = Arrays.asList(cmd.split(" "));
//            List<String> q = s.stream().skip(1).distinct()
//                    .collect(Collectors.toList());
            String x = cmd.replace("/serverFilesList ","");
            System.out.println("Имеющиеся файлы в хранилище сервера:\n" + x);
        }
    }
}
