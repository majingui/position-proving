import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ZkproveServer { //生成零知识证明的服务器
    public static boolean isJSON(String str) { //判断某个字符串是否为JSON
        boolean result = false;
        try {
            Object obj = JSON.parse(str);
            result = true;
        } catch (Exception e) {
            result = false;
        }
        return result;
    }
    static class CreateZKThread implements Runnable{ //每个线程对应一个用户的请求
        private Socket WitnessSocket; //该用户对应的Socket类
        CreateZKThread(Socket WitnessSocket){
            this.WitnessSocket=WitnessSocket;
        }
        @Override
        public void run() {
            try{
                System.out.println("New Thread Start");
                PrintStream SendToWitness = new PrintStream(WitnessSocket.getOutputStream());//Socket.getOutputStream类：得到的是一个输出流，服务端的Socket对象上的getOutputStream方法得到的输出流其实就是发送给客户端的数据。
                Scanner RecieveFromWitness = new Scanner(WitnessSocket.getInputStream());//Witness.getInputStream类：到一个输入流，服务端的Socket对象上的getInp
                if(RecieveFromWitness.hasNext()){ //如果收到发来的ZkReq
                    String ZkReqJSON =  RecieveFromWitness.next();
                    System.out.println("ZkReqJSON:"+ZkReqJSON);
                    if(!isJSON(ZkReqJSON)){
                        System.out.println("__[illegal]__");
                        SendToWitness.println("your request is not a legal JSON"); //将反馈发回给证明者
                    }else {
                        ZeroKReq zeroKReq = JSON.parseObject(ZkReqJSON, ZeroKReq.class); //将客户端传来的JSON转化为类
                        int Dx = zeroKReq.getDelta_x();
                        int Dy = zeroKReq.getDelta_y();
                        int Dz = zeroKReq.getDelta_z();

                        int R = zeroKReq.getR();
                        String response = CreateZKprover.ZKProve(Dx, Dy, Dz, R);
                        System.out.println("ok, R=" + R);
                        SendToWitness.println(response); //将零知识证明字符发回给见证者
                    }
                }
                SendToWitness.close();
                RecieveFromWitness.close();
                if(WitnessSocket !=null)
                    WitnessSocket.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) throws IOException {
        System.out.println("ZkSever Start");
        ExecutorService executorService = Executors.newFixedThreadPool(3); //最多同时执行3线程
        ServerSocket serverSocket = new ServerSocket(8848);
        while(true){
            Socket Witness = serverSocket.accept(); //服务器开启请求监听，该语句将阻塞，直到有用户请求连接
            SimpleDateFormat df = new SimpleDateFormat("\nyyyy-MM-dd HH:mm:ss");
            System.out.println(df.format(new Date())+" New witness:"+Witness.getInetAddress()+":"+Witness.getPort());
            Witness.setSoTimeout(1000);
            executorService.execute(new CreateZKThread(Witness));
        }
    }
}
