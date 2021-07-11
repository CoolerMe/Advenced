package com.coolme.advanced.servlet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class ShutdownTask {

    public static void main(String[] args) throws IOException {

        Socket socket = new Socket();
        SocketAddress address = new InetSocketAddress("127.0.0.1",8005);
        socket.connect(address);

        String content = "SHUTDOWN";
        ByteBuffer buffer=ByteBuffer.allocate(content.getBytes(StandardCharsets.UTF_8).length);
        buffer.put(content.getBytes(StandardCharsets.UTF_8));
        socket.getOutputStream().write(buffer.array());





    }

}
