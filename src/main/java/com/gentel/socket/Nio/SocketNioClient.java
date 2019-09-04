package com.gentel.socket.Nio;

import com.sun.org.apache.bcel.internal.generic.Select;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class SocketNioClient {

    public void start(Integer port) throws IOException {

        /**
         * 创建socket channel 并绑定IP端口
         */
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8000));

        /**
         * 创建一个selector
         */
        Selector selector = Selector.open();

        /**
         * 配置socketchannel 为非阻塞模式
         */
        socketChannel.configureBlocking(false);

        /**
         * socketChannel 注册到Selector 响应可读事件
         */
        socketChannel.register(selector, SelectionKey.OP_READ);

        /**
         * 开启发送线程
         */
        new Thread(new ClientSendHandler(socketChannel)).start();

        /**
         * 开启接收线程
         */
        new Thread(new ClientReadHandler(selector)).start();

    }

    class ClientSendHandler implements Runnable {

        private SocketChannel socketClientChannel;
        private static final String HELLO = "hello i am client";

        public ClientSendHandler(SocketChannel socketClientChannel) {
            this.socketClientChannel = socketClientChannel;
        }

        @Override
        public void run() {
            for (; ; ) {
                /**
                 * 循环发送客户端信息
                 */
                try {
                    Thread.sleep(1000);
                    socketClientChannel.write(Charset.forName("UTF-8").encode(HELLO));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class ClientReadHandler implements Runnable {
        private Selector selector;

        public ClientReadHandler(Selector selector) {
            this.selector = selector;
        }

        @Override
        public void run() {
            for (; ; ) {
                int readChannels = 0;
                try {
                    readChannels = selector.select();
                    if (readChannels == 0) continue;
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator iterator = selectionKeys.iterator();
                    while(iterator.hasNext()){
                        /**
                         * 储存SelectionKey实例
                         */
                        SelectionKey selectionKey = (SelectionKey) iterator.next();
                        iterator.remove();

                        /**
                         * 可读事件处理
                         */
                        if (selectionKey.isReadable()){
                            readHandler(selectionKey, selector);
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void readHandler(SelectionKey selectionKey, Selector selector) throws IOException {
            /**
             * 从selectKey获取已经准备继续的channel
             */
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

            /**
             * 创建BUFFER
             */
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

            /**
             * 循环读取数据
             */
            String readString = "";
            while (socketChannel.read(byteBuffer)> 0){
                /**
                 * 切换byteBuffer为可读模式
                 */
                byteBuffer.flip();

                /**
                 * 读取BUFFER中内容
                 */
                readString += Charset.forName("UTF-8").decode(byteBuffer);
            }

            if (readString.length() > 0){
                System.out.println("Receive server [" + ((SocketChannel) selectionKey.channel()).getRemoteAddress() + "] message:[" + readString + "]");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        SocketNioClient socketNioClient = new SocketNioClient();
        socketNioClient.start(8000);
    }
}
