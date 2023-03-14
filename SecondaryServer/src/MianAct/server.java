package MianAct;

import com.alibaba.fastjson.JSON;
import crypto.RSA;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class server {
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
        ExeClientThread(Socket ClientSocket){
            this.ClientSocket=ClientSocket;
        }
        @Override
        public void run() {
            try {
                System.out.println("new thread");
                PrintStream SendToClient = new PrintStream(ClientSocket.getOutputStream());//Socket.getOutputStream类：得到的是一个输出流，服务端的Socket对象上的getOutputStream方法得到的输出流其实就是发送给客户端的数据。
                Scanner ReceiveFromClient = new Scanner(ClientSocket.getInputStream());//client.getInputStream类：到一个输入流，服务端的Socket对象上的getInputStream方法得到输入流其实就是从客户端发回的数据。
                if(ReceiveFromClient.hasNext()) {
                    String DataText = ReceiveFromClient.next(); //JSON字符串
                    if(isJSON(DataText)) {
                        Pol_Resp2 pol_resp2 = JSON.parseObject(DataText,Pol_Resp2.class); //将JSON字符串转换为CA2包
                        String Zk2 = pol_resp2.getZk2(); //得到Zk2的正文
                        String id_p = pol_resp2.getIdOfProver(); //得到证明者的id
                        String id_w = pol_resp2.getIdOfWitness(); //得到见证者的id
                        String time = pol_resp2.getTime(); //得到Zk2的签名
                        String SignofZk2 = pol_resp2.getSignofZk2(); //得到Zk2的签名
                        String PublicKey_w = GetPublicKey(id_w);

                        if (Zk2.equals("{ }")  || !RSA.checkSign(Zk2, SignofZk2, PublicKey_w)) { //当zk2为空
                            SendToClient.println("Empty");
                            System.out.println(id_p+"-"+id_w+":zk2 Error");
                            InsertZK2(id_p, id_w, time, "{ }");
                        } else {
                            System.out.println(id_p+"-"+id_w+":zk2 OK");
                            InsertZK2(id_p, id_w, time, Zk2);
                        }
                    }else{
                        System.out.println("__[JSON:illegal]__");
                    }
                }
                /*关闭输入输出流以及Socket，断开连接*/
                if(SendToClient!=null)
                    SendToClient.close();
                if(ReceiveFromClient!=null)
                    ReceiveFromClient.close();
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
    private static String GetPublicKey(String id) throws ClassNotFoundException, SQLException { //查询表中对应id的公钥
        String PublicKey = "";
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
        String SELECT_PUBLICKEY = "SELECT PublicKey FROM Users WHERE id = \'"+id+"\'";
        ConToSql.setAutoCommit(true);
        PreparedStatement preparedStatement = ConToSql.prepareCall(SELECT_PUBLICKEY); //执行查询结果并且返回结果集
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()){
            PublicKey=resultSet.getString("Status");
        }
        return PublicKey;
    }
    private static void InsertZK2(String id_p, String id_w, String time, String zk2) throws ClassNotFoundException, SQLException { //查询表中对应id的公钥
        String PublicKey = "";
        Connection ConToSql;//jdbc驱动
        String driver="com.mysql.cj.jdbc.Driver";
        String url="jdbc:mysql://sh-cynosdbmysql-grp-d6edasty.sql.tencentcdb.com:28905/XinAnCompetition?&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user="root";
        String password="Yu13807816736";
        Class.forName(driver);//注册JDBC驱动程序
        ConToSql = DriverManager.getConnection(url, user, password);//建立连接
        if (!ConToSql.isClosed()) {
            System.out.println(id_p+"-"+id_w+":数据库连接成功(插入)\n");
        }
        String INSERT_ZK = "INSERT INTO CA2 (Time, id_prover, id_witness, zk2) VALUES('" +time+ "','" +id_p+ "','" +id_w+ "','" +zk2+ "')";
        PreparedStatement preparedStatement = ConToSql.prepareStatement(INSERT_ZK);
        preparedStatement.executeUpdate();
    }

    public static void main(String[] args) {
        try{
            System.out.println("SecondarySever Start");
            ExecutorService executorService = Executors.newFixedThreadPool(100); //最多同时执行100线程
            ServerSocket serverSocket = new ServerSocket(8849);
            while(true){
                Socket client = serverSocket.accept(); //服务器开启请求监听，该语句将阻塞，直到有用户请求连接
                System.out.println("New witness:"+client.getInetAddress()+":"+client.getPort()+"\n");
                client.setSoTimeout(1000);
                executorService.execute(new ExeClientThread(client)); //进入新的用户进程
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
