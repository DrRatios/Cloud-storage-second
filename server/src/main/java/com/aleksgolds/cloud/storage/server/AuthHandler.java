package com.geekbrains;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    private DBAuthService authService;

    public enum State {
        LOGIN_LENGTH, LOGIN, PASSWORD_LENGTH, PASSWORD
    }

    private int loginLength;
    private int passwordLength;
    private String login;
    private String password;
    private AuthHandler.State currentState = State.LOGIN_LENGTH;
    private Logger logger = LogManager.getLogger();

    public AuthHandler(DBAuthService authService) {
        this.authService = authService;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        try {
            getLoginAndPassword(buf);
        } catch (UnsupportedEncodingException e) {
            logger.error(e);
        }
        String nick = DBAuthService.getNickByLoginAndPassword(login, password);

        Path serverStorage = Paths.get("server_storage", nick);
        if (nick == null) {
            ProtocolFileSender.sendCommand(ctx.channel(), DataType.AUTH_ERR);
            logger.debug("Неверный логин {}или пароль{}", login, password);
            return;
        } else if (!Files.exists(serverStorage)) {
            try {
                Files.createDirectory(serverStorage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ProtocolFileSender.sendCommand(ctx.channel(), DataType.AUTH_OK);
        ctx.pipeline().addLast(new MainHandler(serverStorage));
        ctx.pipeline().remove(this);
        logger.info("Пользователь {} успешно прошёл авторизацию", nick);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        logger.error(cause);
        ctx.close();
    }

    private void getLoginAndPassword(ByteBuf buf) throws UnsupportedEncodingException {
        while (buf.readableBytes() > 0) {
            if (currentState == State.LOGIN_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    loginLength = buf.readInt();
                    logger.info("Current State: get login length {}", loginLength);
                    currentState = State.LOGIN;
                }
            }
            if (currentState == State.LOGIN) {
                if (buf.readableBytes() >= loginLength) {
                    byte[] loginBytes = new byte[loginLength];
                    buf.readBytes(loginBytes);
                    login = new String(loginBytes, "UTF-8");
                    logger.info("Current State: get login string {}", login);
                    currentState = State.PASSWORD_LENGTH;
                }
            }
            if (currentState == State.PASSWORD_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    passwordLength = buf.readInt();
                    logger.info("Current State: get password length {}", passwordLength);
                    currentState = State.PASSWORD;
                }
            }
            if (currentState == State.PASSWORD) {
                if (buf.readableBytes() >= passwordLength) {
                    byte[] passwordBytes = new byte[passwordLength];
                    buf.readBytes(passwordBytes);
                    password = new String(passwordBytes, "UTF-8");
                    logger.info("Current State: get password string {}", password);
                }
                currentState = State.PASSWORD;
                break;
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }



}
