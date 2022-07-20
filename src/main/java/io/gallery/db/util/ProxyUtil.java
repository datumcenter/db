package io.gallery.db.util;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.Date;

/**
 * 网络代理服务器，隧道模式
 */
public class ProxyUtil {
    private static final int bufferSize = 8092;

    public static void main(String[] args) {
        startService(808);
    }

    public static void startService(int localPort) {
        try {
            //开启一个ServerSocket服务器,监听请求的到来.
            ServerSocket localServerSocket = new ServerSocket(localPort);
            log("Proxy running at " + localServerSocket.getInetAddress().getHostAddress() + ":" + localPort);
            // 一直监听，接收到新连接，则开启新线程去处理
            while (true) {
                new ProxySocketThread(localServerSocket.accept()).start();
            }
        } catch (IOException e) {
            error("exception : %s %s", e.getClass(), e.getLocalizedMessage());
        }
    }

    private static void log(Object message, Object... args) {
        Date dat = new Date();
        String msg = String.format("%1$tF %1$tT %2$-5s %3$s%n", dat, Thread.currentThread().getId(), String.format(message.toString(), args));
        System.out.print(msg);
    }

    private static void error(Object message, Object... args) {
        Date dat = new Date();
        String msg = String.format("%1$tF %1$tT %2$-5s %3$s%n", dat, Thread.currentThread().getId(), String.format(message.toString(), args));
        System.out.print(msg);
    }

    /**
     * IO操作中共同的关闭方法
     *
     * @param socket Socket
     */
    protected static void closeIo(Socket socket) {
        if (null != socket) {
            try {
                socket.close();
            } catch (IOException e) {
                error("exception : %s %s", e.getClass(), e.getLocalizedMessage());
            }
        }
    }

    /**
     * IO操作中共同的关闭方法
     *
     * @param closeable Closeable
     */
    protected static void closeIo(Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (IOException e) {
                error("exception : %s %s", e.getClass(), e.getLocalizedMessage());
            }
        }
    }

    private static class ProxySocketThread extends Thread {
        private Socket localSocket;
        private Socket remoteSocket;
        private InputStream lin;
        private InputStream rin;
        private OutputStream lout;
        private OutputStream rout;

        public ProxySocketThread(Socket socket) {
            this.localSocket = socket;
        }

        public void run() {
            //获取远程socket的地址，然后进行打印
            String addr = localSocket.getRemoteSocketAddress().toString();
            log("------ start socket : %s ------", addr);
            try {
                lin = localSocket.getInputStream();
                lout = localSocket.getOutputStream();

                StringBuilder headStr = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(lin));
                //读取HTTP请求头，并拿到HOST请求头和method
                String line;
                String host = "";
                String proxy_Authorization = "";
                boolean isNeedLogin = false;
                while ((line = br.readLine()) != null) {
                    //打印http协议头
                    log(line);
                    headStr.append(line).append("\r\n");
                    if (line.length() == 0) {
                        break;
                    } else {
                        String[] temp = line.split(" ");
                        if (temp[0].contains("Host")) {
                            host = temp[1];
                        }
                        //如果配置了需要登陆，就解析消息头里面的Proxy-Authorization字段，认证的账号和密码信息是通过base64加密传过来的。
                        if (isNeedLogin && temp[0].contains("Proxy-Authorization")) {//获取认证信息
                            proxy_Authorization = temp[2];
                        }
                    }
                }

                String type = headStr.substring(0, headStr.indexOf(" "));
                //根据host头解析出目标服务器的host和port
                String[] hostTemp = host.split(":");
                host = hostTemp[0];
                int port = 80;
                if (hostTemp.length > 1) {
                    port = Integer.parseInt(hostTemp[1]);
                }

                boolean isLogin = false;
                //如果需要登录，校验登录是否通过
                if (isNeedLogin) {
                    //通过username和password进行base64加密得到一个串，然后和请求里面传过来的Proxy-Authorization比较，一致的话就认证成功。
                    String username = "admin";
                    String password = "nimda";
                    String authenticationEncoding = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
                    if (proxy_Authorization.equals(authenticationEncoding)) {
                        isLogin = true;//登录通过
                        log("login success, basic: %s", proxy_Authorization);
                    } else {
                        log("httpproxy server need login,but login failed .");
                    }
                }

                //不需要登录或已被校验登录成功,才进入代理，否则直接程序结束，关闭连接
                if (!isNeedLogin || isLogin) {
                    //连接到目标服务器
                    remoteSocket = new Socket(host, port);//进行远程连接
                    rin = remoteSocket.getInputStream();
                    rout = remoteSocket.getOutputStream();
                    log("connect target %s %s %s", host, port, type);
                    //根据HTTP method来判断是https还是http请求
                    if ("CONNECT".equalsIgnoreCase(type)) {//https先建立隧道
                        lout.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
                        lout.flush();
                    } else {//http直接将请求头转发
                        rout.write(headStr.toString().getBytes());
                        rout.flush();
                    }
                    new ReadThread().start();
                    //设置超时，超过时间未收到客户端请求，关闭资源
                    //remoteSocket.setSoTimeout(10000);
                    //写数据,负责读取客户端发送过来的数据，转发给远程
                    byte[] data = new byte[bufferSize];
                    int len = 0;
                    while ((len = lin.read(data)) > 0) {
                        if (len == bufferSize) {//读到了缓存大小一致的数据，不需要拷贝，直接使用
                            rout.write(data);
                        } else {//读到了比缓存大小的数据，需要拷贝到新数组然后再使用
                            byte[] dest = new byte[len];
                            System.arraycopy(data, 0, dest, 0, len);
                            rout.write(dest);
                        }
                        rout.flush();
                    }
                }
            } catch (Exception e) {
                error("exception : %s %s", e.getClass(), e.getLocalizedMessage());
            } finally {
                log("------ close socket :  %s ------", addr);
                closeIo(lin);
                closeIo(rin);
                closeIo(lout);
                closeIo(rout);
                closeIo(localSocket);
                closeIo(remoteSocket);
            }
        }

        //读数据线程负责读取远程数据后回写到客户端
        class ReadThread extends Thread {
            @Override
            public void run() {
                try {
                    byte[] data = new byte[bufferSize];
                    int len = 0;
                    while ((len = rin.read(data)) > 0) {
                        if (len == bufferSize) {//读到了缓存大小一致的数据，不需要拷贝，直接使用
                            lout.write(data);
                        } else {//读到了比缓存大小的数据，需要拷贝到新数组然后再使用
                            byte[] dest = new byte[len];
                            System.arraycopy(data, 0, dest, 0, len);
                            lout.write(dest);
                        }
                        lout.flush();
                    }
                } catch (IOException e) {
                    log(remoteSocket.getLocalAddress() + ":" + remoteSocket.getPort() + " remoteSocket InputStream disconnected.");
                }
            }
        }
    }
}
