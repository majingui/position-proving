import com.alibaba.fastjson.JSON;

import java.io.PrintStream;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class test {
    public static void main(String[] args) {
        ZeroKReq zeroKReq = new ZeroKReq();
        zeroKReq.setDelta_x(1);
        zeroKReq.setDelta_y(2);
        zeroKReq.setDelta_z(3);
        zeroKReq.setR(60);
        /*以上为测试数据*/
        String zeroKReqJSON = JSON.toJSONString(zeroKReq); //将对象转化为JSON字符串
        String serverName = "39.108.177.132"; // 主机
        int port = 8848; // 端口号
        try {
            long StartTime = System.currentTimeMillis(); //开始时间
            Socket witness = new Socket(serverName, port); // 连接远程主机
            System.out.println("远程主机地址：" + witness.getRemoteSocketAddress());
            //Socket.getOutputStream类：得到一个输出流，客户端的Socket对象上的getOutputStream方法得到的输出流是发送给服务端的数据。
            PrintStream SendToServer = new PrintStream(witness.getOutputStream());
            //client.getInputStream类：到一个输入流，客户端的Socket对象上的getInputStream方法得到输入流是从服务端发回的数据。
            Scanner RecieveFromServer = new Scanner(witness.getInputStream());
            while(true){
                System.out.println(zeroKReqJSON);
                SendToServer.println(zeroKReqJSON); //向服务器发送zk请求
                if(RecieveFromServer.hasNext()){ //当服务器返回结果
                    String zkResponseJSON = RecieveFromServer.next(); //从服务器返回的零知识证明的JSON
                    //System.out.println(zkResponseJSON);
                    break;
                }
            }
            long EndTime = System.currentTimeMillis(); //开始时间
            System.out.println( ((double)EndTime-(double)StartTime)/1000 );
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
