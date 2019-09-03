package com.gentel.socket.Io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketIoClient {

    /**
     * 启动方法
     */
    public void start(Integer port) throws IOException, InterruptedException {

        /**
         * 创建socket 客户端
         */
        Socket socketClient = new Socket("127.0.0.1", port);

        /**
         * 绑定读字节流
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));

        /**
         * 绑定写字节流
         */
        PrintWriter writer = new PrintWriter(socketClient.getOutputStream());
        String dataBuf;
        for(;;){
            writer.println("hello i am client");
            writer.flush();
            if (null != (dataBuf = reader.readLine())){
                System.out.println("Server:"+dataBuf);
            }
            Thread.sleep(1000);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        SocketIoClient socketIoClient = new SocketIoClient();
        socketIoClient.start(8000);
    }
}
