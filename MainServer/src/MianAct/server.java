package MianAct;

import com.alibaba.fastjson.JSON;
import java.sql.*;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import crypto.*;

public class server {
    static String PublicKey;
    static String PrivateKey;
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
    static class ExeClientThread implements  Runnable{ //每一个用户对应一个可执行线程
        private Socket ClientSocket; //该用户对应的Socket类
        private String id; //用户的id
        ExeClientThread(Socket ClientSocket){
            this.ClientSocket=ClientSocket;
        }
        @Override
        public void run() {
            try {
                System.out.println("new thread");
                PrintStream SendToClient = new PrintStream(ClientSocket.getOutputStream());//Socket.getOutputStream类：得到的是一个输出流，服务端的Socket对象上的getOutputStream方法得到的输出流其实就是发送给客户端的数据。
                Scanner ReceiveFromClient = new Scanner(ClientSocket.getInputStream());//client.getInputStream类：到一个输入流，服务端的Socket对象上的getInputStream方法得到输入流其实就是从客户端发回的数据。
                if(ReceiveFromClient.hasNext()) { //等待用户发来的注册信息
                    String RegisterText = ReceiveFromClient.next(); //包含有用户id、公钥的注册信息
                    if(isJSON(RegisterText)) {
                        UserData userData = JSON.parseObject(RegisterText, UserData.class); //将客户端传来的JSON转化为类
                        id = userData.getId();
                        if(!UserRegister(userData)) //注册用户
                            System.out.println(id+"RegisterError"); //注册错误
                    }else{
                        System.out.println(id+"RegisterJSONError"); //JSON解析错误
                        SendToClient.close();
                        ReceiveFromClient.close();
                        if(ClientSocket!=null)
                            ClientSocket.close();
                        return;
                    }
                    SendToClient.println(PublicKey); //向客户端返回服务器公钥
                }

                System.out.println(id+"Register End");

                if(ReceiveFromClient.hasNext()) { //等待发来的CA2
                    String DataText = ReceiveFromClient.next(); //用服务器公钥加密的JSON字符串
                    if(isJSON(DataText)) { //检验是否为JSON
                        CA2package ca2package = JSON.parseObject(DataText, CA2package.class); //将JSON字符串转换为CA2包
                        String EnCodedAESkey = ca2package.getAESkey_Encode_ByRSA(); //得到RSA加密后的AES密钥
                        String AESkey = RSA.decrypt(EnCodedAESkey, PrivateKey); //解密出AES密钥
                        String EncodedCA2 = ca2package.getCA2(); //得到AES加密后的CA2证书
                        String ca2JSON = AES.decryptFromBase64(EncodedCA2,AESkey);
                        CA2 ca2 = JSON.parseObject(ca2JSON,CA2.class);
                        System.out.println(ca2);

                        //todo
                        SendToClient.println("OK,your_Health_Code_is_");
                    }
                    else{
                        System.out.println("CA2JSONError"); //JSON解析错误
                    }
                }
                /*关闭输入输出流以及Socket，断开连接*/
                SendToClient.close();
                ReceiveFromClient.close();
                if(ClientSocket!=null)
                    ClientSocket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    private static String UserSelect(Integer id) throws ClassNotFoundException, SQLException {
        String Color = "";
        Connection ConToSql;//jdbc驱动
        String driver="com.mysql.cj.jdbc.Driver";
        String url="jdbc:mysql://sh-cynosdbmysql-grp-d6edasty.sql.tencentcdb.com:28905/XinAnCompetition?&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user="root";
        String password="Yu13807816736";
        Class.forName(driver);//注册JDBC驱动程序
        ConToSql = DriverManager.getConnection(url, user, password);//建立连接
        if (!ConToSql.isClosed()) {
            System.out.println(id+"数据库连接成功(查询)\n");
        }
        String SELECT_STATUS = "SELECT Status FROM Users WHERE id = \'"+id+"\'";
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
        String url="jdbc:mysql://sh-cynosdbmysql-grp-d6edasty.sql.tencentcdb.com:28905/XinAnCompetition?&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user="root";
        String password="Yu13807816736";
        Class.forName(driver);//注册JDBC驱动程序
        ConToSql = DriverManager.getConnection(url, user, password);//建立连接
        String id=userData.getId();
        if (!ConToSql.isClosed()) {
            System.out.println(id+"数据库连接成功(注册)\n");
        }
        String SELECT = "SELECT id FROM Users WHERE id = '"+id+"'";
        ConToSql.setAutoCommit(true);
        PreparedStatement preparedStatement = ConToSql.prepareCall(SELECT); //执行查询结果并且返回结果集
        ResultSet resultSet = preparedStatement.executeQuery();
        if(!resultSet.next()){ //如果用户不存在
            String PublicKey = userData.getPublicKey();
            System.out.println(id+"Regist");
            String INSERT = "INSERT INTO Users (id, PublicKey, Status) VALUES('" +id+ "','" +PublicKey+ "','green')"; //插入语句
            preparedStatement = ConToSql.prepareStatement(INSERT);
            preparedStatement.executeUpdate();
            return false;
        }else { //如果用户存在
            return  true;
        }
    }

    public static void main(String[] args) {
        try{
            System.out.println("MainSever Start");
            Map<String,String> KeyPair=RSA.generateKeyPair();
            PublicKey=KeyPair.get("publicKey");
            PrivateKey=KeyPair.get("privateKey");
            ExecutorService executorService = Executors.newFixedThreadPool(100); //最多同时执行100线程
            ServerSocket serverSocket = new ServerSocket(8850);
            while(true){
                Socket client = serverSocket.accept(); //服务器开启请求监听，该语句将阻塞，直到有用户请求连接
                System.out.println("New user:"+client.getInetAddress()+":"+client.getPort()+"\n");
                client.setSoTimeout(1000);
                executorService.execute(new ExeClientThread(client)); //进入新的用户进程
            }
        }catch(Exception e){
            e.printStackTrace();;
        }
    }
}
