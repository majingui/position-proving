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

public class server {
    static String PublicKey;
    static String PrivateKey;
    static class ExeClientThread implements  Runnable{ //每一个用户对应一个可执行线程
        private Socket ClientSocket; //该用户对应的Socket类
        private Integer id; //用户的id
        ExeClientThread(Socket ClientSocket){
            this.ClientSocket=ClientSocket;
        }
        @Override
        public void run() {
            try {
                PrintStream SendToClient = new PrintStream(ClientSocket.getOutputStream());//Socket.getOutputStream类：得到的是一个输出流，服务端的Socket对象上的getOutputStream方法得到的输出流其实就是发送给客户端的数据。
                Scanner RecieveFromClient = new Scanner(ClientSocket.getInputStream());//client.getInputStream类：到一个输入流，服务端的Socket对象上的getInputStream方法得到输入流其实就是从客户端发回的数据。
                while(true) { //等待客户端发来的注册消息
                    if(RecieveFromClient.hasNext()) {
                        String RegisterText = RecieveFromClient.next(); //包含有用户id、公钥、用户名的注册信息
                        UserData userData = JSON.parseObject(RegisterText, UserData.class); //将客户端传来的JSON转化为类
                        id=userData.getId();
                        Boolean RegisterRes = UserRegister(userData); //注册用户
                        SendToClient.println(PublicKey); //向客户端返回服务器公钥
                        break;
                    }
                }
                while(true) { //等待客户端发来的CA2
                    if(RecieveFromClient.hasNext()) {
                        String DataText = RecieveFromClient.next(); //用服务器公钥加密的JSON字符串
                        CA2package ca2package = JSON.parseObject(DataText,CA2package.class); //将JSON字符串转换为CA2包
                        String EnCodedAESkey = ca2package.getAESkey_Encode_ByRSA(); //得到RSA加密后的AES密钥
                        String AESkey = RSA.decrypt(EnCodedAESkey,PrivateKey); //解密出AES密钥
                        String EncodedCA2 = ca2package.getCA2(); //得到AES加密后的CA2证书
                        String CA2 = AES.decryptFromBase64(EncodedCA2,AESkey); //解密出CA2证书
                        System.out.println(CA2);
                        String Color = UserSelect(id);
                        SendToClient.println("OK,your_Health_Code_is"+Color);
                        break;
                    }
                }
                /*关闭输入输出流以及Socket，断开连接*/
                if(SendToClient!=null)
                    SendToClient.close();
                if(RecieveFromClient!=null)
                    RecieveFromClient.close();
                if(ClientSocket!=null)
                    ClientSocket.close();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    private static String UserSelect(Integer id) throws ClassNotFoundException, SQLException {
        String Color = "";
        Connection ConToSql;//jdbc驱动
        String driver="com.mysql.cj.jdbc.Driver";
        String url="jdbc:mysql://127.0.0.1:3306/JavaTest?&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user="root";
        String password="mima";
        Class.forName(driver);//注册JDBC驱动程序
        ConToSql = DriverManager.getConnection(url, user, password);//建立连接
        if (!ConToSql.isClosed()) {
            System.out.println(id+"数据库连接成功(查询)\n");
        }
        String SELECT_STATUS = "SELECT Status FROM Users WHERE id = "+id;
        ConToSql.setAutoCommit(true);
        PreparedStatement preparedStatement = ConToSql.prepareCall(SELECT_STATUS); //执行查询结果并且返回结果集
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()){
            Color=resultSet.getString("Status");
        }
        return Color;
    }

    private static boolean UserRegister(UserData userData) throws SQLException, ClassNotFoundException { //用户首次登陆时注册
        Connection ConToSql;//jdbc驱动
        String driver="com.mysql.cj.jdbc.Driver";
        String url="jdbc:mysql://127.0.0.1:3306/JavaTest?&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user="root";
        String password="mima";
        Class.forName(driver);//注册JDBC驱动程序
        ConToSql = DriverManager.getConnection(url, user, password);//建立连接
        int id=userData.getId();
        if (!ConToSql.isClosed()) {
            System.out.println(id+"数据库连接成功(注册)\n");
        }
        String SELECT = "SELECT * FROM Users WHERE id = "+id;
        ConToSql.setAutoCommit(true);
        PreparedStatement preparedStatement = ConToSql.prepareCall(SELECT); //执行查询结果并且返回结果集
        ResultSet resultSet = preparedStatement.executeQuery();
        if(!resultSet.next()){ //如果用户不存在
            String UserName = userData.getName();
            String PublicKey = userData.getPublicKey();
            System.out.println(id+"Regist");
            String INSERT = "INSERT INTO Users (UserName, id, PublicKey, Status) VALUES(\'"+UserName+"\',"+id+",\'"+PublicKey+"\',\'green\')"; //插入语句
            preparedStatement = ConToSql.prepareStatement(INSERT);
            preparedStatement.executeUpdate();
            return false;
        }else { //如果用户存在
            return  true;
        }
    }

    public static void main(String[] args) {
        try{
            System.out.println("Sever Start");
            Map<String,String> KeyPair=RSA.generateKeyPair();
            PublicKey=KeyPair.get("publicKey");
            PrivateKey=KeyPair.get("privateKey");
            ExecutorService executorService = Executors.newFixedThreadPool(100); //最多同时执行100线程
            ServerSocket serverSocket = new ServerSocket(8080);
            while(true){
                Socket client = serverSocket.accept(); //服务器开启请求监听，该语句将阻塞，直到有用户请求连接
                System.out.println("New user:"+client.getInetAddress()+":"+client.getPort()+"\n");
                executorService.execute(new ExeClientThread(client)); //进入新的用户进程
            }
        }catch(Exception e){
            e.printStackTrace();;
        }
    }
}
