


import io.netty.util.internal.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import java.sql.*;

public class SPTesting {

    Connection con=null;
    Statement stmt=null;

    ResultSet rs;
    CallableStatement cStmt;
    ResultSet rs1;
    ResultSet rs2;

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


    @Test(priority =2)
    void test_SelectAllCustormers() throws SQLException {
        cStmt = con.prepareCall("{CALL SelectAllCustomers()}");
        cStmt.executeQuery(); // resultset1

        Statement stmt= con.createStatement();
        rs2 = stmt.executeQuery("select * from custormers");

        Assert.assertEquals(compareResultSets(rs1,rs2),true);
    }
    public boolean compareResultSets(ResultSet resultSet1, ResultSet resultSet2) throws SQLException {
        while (resultSet1.next()) {

            resultSet2.next();
            int count = resultSet1.getMetaData().getColumnCount();
            for (int i = 1; i <= count; i++) {
                if (!StringUtils.equals(resultSet1.getString(i), resultSet2.getString(i))) {
                    return false;
                }
            }
        }
        return true;
        }

}
