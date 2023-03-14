package MianAct;

import java.sql.*;

public class SQLtest {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Connection ConToSql;//jdbc驱动
        String driver="com.mysql.cj.jdbc.Driver";
        String url="jdbc:mysql://sh-cynosdbmysql-grp-d6edasty.sql.tencentcdb.com:28905/XinAnCompetition?&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user="root";
        String password="Yu13807816736";
        Class.forName(driver);//注册JDBC驱动程序
        ConToSql = DriverManager.getConnection(url, user, password);//建立连接
        String id="Test1";
        if (!ConToSql.isClosed()) {
            System.out.println(id+"数据库连接成功(注册)\n");
        }
        String SELECT = "SELECT id FROM Users WHERE id = '"+id+"'";
        ConToSql.setAutoCommit(true);
        PreparedStatement preparedStatement = ConToSql.prepareCall(SELECT); //执行查询结果并且返回结果集
        ResultSet resultSet = preparedStatement.executeQuery();
        if(!resultSet.next()){ //如果用户不存在
            String PublicKey = "1100";
            System.out.println(id+"Regist");
            String INSERT = "INSERT INTO Users (id, PublicKey, Status) VALUES(\'"+id+"\',\'"+PublicKey+"\',\'green\')"; //插入语句
            preparedStatement = ConToSql.prepareStatement(INSERT);
            preparedStatement.executeUpdate();
            System.out.println("Ne");
        }else { //如果用户存在
            System.out.println("e");
        }
    }
}
