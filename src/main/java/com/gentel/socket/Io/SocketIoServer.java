package com.gentel.socket.Io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketIoServer {

    /**
     * 启动客户端
     */
    public void start() throws IOException {

        /**
         * 创建socket 服务端 绑定8000端口
         */
        ServerSocket serverSocket = new ServerSocket(8000);

        for (;;) {
            /**
             * 创建socket 客户端 服务端阻塞获取连接
             */
            Socket socketClient = serverSocket.accept();
            Thread thread = new Thread(new SocketServerHandler(socketClient));
            thread.start();
            System.out.println("新的客户端:"+thread.getName());
        }
    }


    public static void main(String[] args) throws IOException {
        SocketIoServer socketIoServer = new SocketIoServer();
        socketIoServer.start();
    }

    class SocketServerHandler implements Runnable {

        Socket socket;

        public SocketServerHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                /**
                 * 1. 注册输入流
                 */
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                /**
                 * 2. 注册输出流
                 */
                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                String readBuf;
                for (; ; ) {
                    /**
                     * 阻塞读取数据
                     */
                    if (null != (readBuf = reader.readLine())) {
                        System.out.println("接收到客户端数据："+readBuf);
                        /**
                         * 回显数据
                         */
                        writer.println("hello i am server!");
                        writer.flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
