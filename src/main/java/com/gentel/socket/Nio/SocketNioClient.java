package com.gentel.socket.Nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class SocketNioClient {

    public void start(Integer port) throws IOException {

        /**
         * 创建socket channel 并绑定IP端口
         */
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8000));

        /**
         * 配置socketchannel 为非阻塞模式
         */
        socketChannel.configureBlocking(false);

        new Thread(new ClientSendHandler()).start();


    }

    class ClientSendHandler implements Runnable{

        SocketChannel socketClientChannel;
        private static final String HELLO = "hello i am client\r\n";

        @Override
        public void run() {
            /**
             * 创建bytebuffer 分配空间
             */
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            byteBuffer.put(HELLO.getBytes());

            /**
             * 切换buf为读模式
             */
            byteBuffer.flip();
            try {
                for (; ; ) {

                    for (;;){
                        int cnt = socketClientChannel.read(byteBuffer);
                        if (cnt > 0){
                            System.out.println("Receive Server:"+byteBuffer.toString());
                            break;
                        }

                    }
                    /**
                     * 循环发送客户端信息
                     */
                    socketClientChannel.write(byteBuffer);
                    Thread.sleep(1000);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        SocketNioClient socketNioClient = new SocketNioClient();
        socketNioClient.start(8000);
    }
}
