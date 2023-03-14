package Backend.BackDemo.crypto;
import java.sql.*;

public class test {
    public static void main(String[] args) {
        Connection con;
        //jdbc驱动
        String driver="com.mysql.cj.jdbc.Driver";
        //这里我的数据库是cxxt
        String url="jdbc:mysql://127.0.0.1:3306/JavaTest?&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user="root";
        String password="mima";
        try {
            //注册JDBC驱动程序
            Class.forName(driver);
            //建立连接
            con = DriverManager.getConnection(url, user, password);
            if (!con.isClosed()) {
                System.out.println("数据库连接成功");
            }
            Integer id=0;
            String UserName="Yzx";
            String PublicKey="123456";
            con.setAutoCommit(true);
            String SELECT = "SELECT * FROM Users WHERE id = "+id;
            PreparedStatement preparedStatement = con.prepareStatement(SELECT); //执行查询结果并且返回结果集
            ResultSet resultSet = preparedStatement.executeQuery();
            if(!resultSet.next()){
                System.out.println("Regist");
                String INSERT = "INSERT INTO Users (UserName, id, PublicKey) VALUES(\'"+UserName+"\',"+id+",\'"+PublicKey+"\')";
                System.out.println(INSERT);
                preparedStatement = con.prepareStatement(INSERT);
                preparedStatement.executeUpdate();
            }else {
                System.out.println("OK");
            }


        } catch (ClassNotFoundException e) {
            System.out.println("数据库驱动没有安装");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("数据库连接失败");
        }
    }
}

