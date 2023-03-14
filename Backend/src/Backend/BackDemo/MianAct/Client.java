package Backend.BackDemo.MianAct;

import com.alibaba.fastjson.JSON;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.sql.*;
import javax.sound.midi.Receiver;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import Backend.BackDemo.crypto.*;

/** 
    向服务端提交UserData的JSON
    接收服务端返回的公钥字符串
    用一个16个字符组成的AES密钥来加密一个名为CA2的字符串，得到正文密文，赋值给CA2package类里面的CA2
    用服务端的公钥去加密AES密钥，得到加密过的AES密钥，赋值给CA2package类里面的EnCodedAESkey
    把该对象转换为JSON传给服务端
    接收服务端返回的所解密出来的CA2字符串
    结束

    客户端文件存储 用户信息、RSA生产的公钥、私钥
    运行时检查是否存储有该用户信息，没有则要求用户输入，有则直接调用
*/

public class Client {
    static String ServerPublicKey; // 服务端传来的公钥
    static String AESKey = "1234567891234567"; // AES密钥
    static String UserPublicKey; // 用户公钥
    static  String UserPrivateKey; // 用户私钥
    static String Data = "ababababab"; // 明文
    static Integer id = 1111; // ID

    public static void main(String [] args) throws Exception {
        CA2package ca2 = new CA2package();
        UserData user = new UserData(); // 用户信息
        
        user.setId(id); // 用户ID
        user.setName("ElsaT"); // 用户名称
        // user.setPublicKey("123456789"); // 用户公钥
        Map <String,String> KeyPair=RSA.generateKeyPair();
        UserPrivateKey=KeyPair.get("publicKey");
        UserPublicKey=KeyPair.get("privateKey");
        user.setPublicKey(UserPublicKey); // 用户公钥
        String str1 = JSON.toJSONString(user);
        //JSONObject str1 = JSONObject.fromObject(user);
        String serverName = "103.46.128.53"; // 主机
        int port = 22659; // 端口号

        try{
            Socket client = new Socket(serverName, port); // 连接远程主机
            System.out.println("远程主机地址：" + client.getRemoteSocketAddress());

            //Socket.getOutputStream类：得到一个输出流，客户端的Socket对象上的getOutputStream方法得到的输出流是发送给服务端的数据。
            PrintStream SendToServer = new PrintStream(client.getOutputStream());
            //client.getInputStream类：到一个输入流，客户端的Socket对象上的getInputStream方法得到输入流是从服务端发回的数据。
            Scanner RecieveFromServer = new Scanner(client.getInputStream());

            while(true) { //等待服务端发来的消息
                SendToServer.println(str1); //向服务端返回JSON形式的UserData类
                if(RecieveFromServer.hasNext()) {
                    ServerPublicKey = RecieveFromServer.next(); //服务端发来的公钥信息
                    System.out.println(ServerPublicKey);
                    // 用公钥加密AES密钥，得到加密过的AES密钥，赋值给CA2package类里面的EnCodedAESkey
                    String P_PrString = RSA.encrypt(AESKey, ServerPublicKey); // RSA 使用服务端公钥加密后的AES密钥
                    String P_Data = AES.encryptToBase64(Data, AESKey); // AES密钥来加密一个名为CA2的字符串得到正文密文
                    ca2.setAESkey_Encode_ByRSA(P_PrString); // 将加密后的AES密钥赋值给AESkey_Encode_ByRSA
                    ca2.setCA2(P_Data); // 将正文密文赋值给CA2package类里面的CA2
                    String str2 = JSON.toJSONString(ca2);// 将类ca2转化为JSON
                    SendToServer.println(str2); //向服务端返回JSON形式的CA2package类
                    break;
                }
            }
            while(true) { // 等待服务器传来的解密出来的CA2字符串
                if(RecieveFromServer.hasNext()){
                    String R_ca2 = RecieveFromServer.next();
                    System.out.println(R_ca2);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}