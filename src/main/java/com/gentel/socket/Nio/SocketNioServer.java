package com.gentel.socket.Nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SocketNioServer {

    public void start() throws IOException {
        /**
         * 创建select
         */
        Selector selector = Selector.open();

        /**
         * 通过ServerSocketChannel 创建channel
         */
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        /**
         * 为channel 绑定监听端口
         */
        serverSocketChannel.socket().bind(new InetSocketAddress(8000));

        /**
         * 设置为channel非阻塞模式
         */
        serverSocketChannel.configureBlocking(false);

        /**
         * 为socket channel注册select 监听连接事件
         */
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("服务端开启，等待客户端接入。。。");

        new Thread(new ServerNioHandler(selector, serverSocketChannel)).start();
    }

    class ServerNioHandler implements Runnable {

        private Selector selector;
        private ServerSocketChannel serverSocketChannel;

        public ServerNioHandler(Selector selector, ServerSocketChannel serverSocketChannel) {
            this.selector = selector;
            this.serverSocketChannel = serverSocketChannel;
        }

        @Override
        public void run() {
            try {
                for (; ; ) {
                    /**
                     * 获取可用的channel数量
                     */
                    int readChannels = selector.select();

                    /**
                     * 判断channel数据是否为0
                     */
                    if (readChannels == 0) continue;

                    /**
                     * 获取可用channel集合
                     */
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();

                    Iterator iterator = selectionKeys.iterator();

                    while (iterator.hasNext()) {
                        /**
                         * 获取selectionKey实例
                         */
                        SelectionKey selectionKey = (SelectionKey) iterator.next();

                        /**
                         * 获取数据后移除当前selectkey防止重复调用
                         */
                        iterator.remove();

                        /**
                         * 根据就绪状态调用对应的业务处理逻辑
                         */

                        /**
                         * 接入事件  --考虑后续改造接入事件使用线程池
                         */
                        if (selectionKey.isAcceptable()) {
                            acceptHandler(serverSocketChannel, selector);
                        }

                        /**
                         * 可读事件  --考虑后续改造可读事件使用线程池
                         */
                        if (selectionKey.isReadable()) {
                            readHandler(selectionKey, selector);
                        }

                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 接入事件处理方法
         */
        void acceptHandler(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
            /**
             * 创建socket channel
             */
            SocketChannel socketChannel = serverSocketChannel.accept();

            /**
             * 将SocketChannel设置为非阻塞工作模式
             */
            socketChannel.configureBlocking(false);

            /**
             * 将channel注册到select上 监听可读事件
             */
            socketChannel.register(selector, SelectionKey.OP_READ);

            /**
             * 通知客户端信息
             */
            socketChannel.write(Charset.forName("UTF-8").encode("hello i am server"));
            System.out.println("Get connect:"+socketChannel.getRemoteAddress());
        }

        /**
         * 读事件处理方法
         */
        void readHandler(SelectionKey selectionKey, Selector selector) throws IOException {
            /**
             * 从 selectionKey中获取已经就绪的channel
             */
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

            /**
             * 创建buffer
             */
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

            /**
             * 循环读取客户端请求的内容
             */
            String result = "";
            while (socketChannel.read(byteBuffer) > 0) {
                /**
                 * 切换BYTEBUF为读模式
                 */
                byteBuffer.flip();

                result += Charset.forName("UTF-8").decode(byteBuffer);
            }

            /**
             * 将Channel再次注册到selector上 继续监听可读事件
             */
            socketChannel.register(selector, SelectionKey.OP_READ);

            /**
             * 回复客户端接收到信息
             */
            socketChannel.write(Charset.forName("UTF-8").encode("received client[" + socketChannel.getRemoteAddress() + "]message:[" + result + "]"));
            System.out.println("Receive client[" + socketChannel.getRemoteAddress() + "]message:[" + result + "]");
        }
    }

    public static void main(String[] args) throws IOException {
        SocketNioServer socketNioServer = new SocketNioServer();
        socketNioServer.start();
    }
}
