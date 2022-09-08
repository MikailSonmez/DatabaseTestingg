


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import java.sql.*;

public class SPTesting {

    Connection con=null;
    Statement stmt=null;

    ResultSet rs;

    @BeforeClass
    void setup() throws SQLException {
        con= DriverManager.getConnection("jdbc:mysql://localhost:3306/classicmodels","root","dota483361483");
    }

    @AfterClass
    void tearDown() throws SQLException {
        con.close();
    }

    @Test(priority=1)
    void test_storedProceduresExists() throws SQLException
    {
        stmt=con.createStatement();
        rs=stmt.executeQuery("SHOW PROCEDURE STATUS WHERE Name='SelectAllCustomers'");
        rs.next();

        Assert.assertEquals(rs.getString("Name"),"SelectAllCustomers");
    }

}
