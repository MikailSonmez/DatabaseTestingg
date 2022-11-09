


import io.netty.util.internal.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import java.sql.*;

public class SPTesting {

    Connection con = null;
    Statement stmt = null;

    ResultSet rs;
    CallableStatement cStmt;
    ResultSet rs1;
    ResultSet rs2;

    @BeforeClass
    void setup() throws SQLException {

        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/classicmodels", "root", "********");


    }

    @AfterClass
    void tearDown() throws SQLException {
        con.close();

    }


    @Test(priority = 1)
    void test_storedProceduresExists() throws SQLException {

        stmt = con.createStatement();
        rs = stmt.executeQuery("SHOW PROCEDURE STATUS WHERE Name='SelectAllCustomers'");
        rs.next();

        Assert.assertEquals(rs.getString("Name"), "SelectAllCustomers");

    }


    @Test(priority = 2)
    void test_SelectAllCustomers() throws SQLException {

        cStmt = con.prepareCall("{CALL SelectAllCustomers()}");
        rs1 = cStmt.executeQuery(); // resultset1

        Statement stmt = con.createStatement();
        rs2 = stmt.executeQuery("select * from customers");

        Assert.assertEquals(compareResultSets(rs1, rs2), true);

    }

    @Test(priority = 3)
    void test_SelectAllCustomersByCity() throws SQLException
    {
        cStmt = con.prepareCall("{call SelectAllCustomersByCity(?)}");
        cStmt.setString(1, "Singapore");
        rs1 = cStmt.executeQuery(); // resultset1

        Statement stmt = con.createStatement();
        rs2 = stmt.executeQuery("SELECT * FROM Customers WHERE city = 'Singapore'");

        Assert.assertEquals(compareResultSets(rs1,rs2),true);
    }

    @Test(priority = 4)
    void test_SelectAllCustomersByCityAndPinCode() throws SQLException
    {
        cStmt = con.prepareCall("{call SelectAllCustomersByCityAndPin(?,?)}");
        cStmt.setString(1, "Singapore");
        cStmt.setString(2, "07993");
        rs1 = cStmt.executeQuery(); // resultset1

        Statement stmt = con.createStatement();
        rs2 = stmt.executeQuery("SELECT * FROM Customers WHERE city = 'Singapore' and postalCode='07993'");

        Assert.assertEquals(compareResultSets(rs1,rs2),true);
    }

    @Test(priority = 5)
    void test_get_order_by_cust() throws SQLException {

        cStmt = con.prepareCall("{call get_order_by_cust(?,?,?,?,?)}");
        cStmt.setInt(1,141);

        cStmt.registerOutParameter(2, Types.INTEGER);
        cStmt.registerOutParameter(3, Types.INTEGER);
        cStmt.registerOutParameter(4, Types.INTEGER);
        cStmt.registerOutParameter(5, Types.INTEGER);

        cStmt.executeQuery();

        cStmt.getInt(2);

        int shipped = cStmt.getInt(2);
        int canceled = cStmt.getInt(3);
        int resolved = cStmt.getInt(4);
        int disputed = cStmt.getInt(5);

        // System.out.println(shipped+" "+canceled+" "+resolved+" "+disputed);

        Statement stmt=con.createStatement();
        rs = stmt.executeQuery("select (SELECT count(*) as 'shipped' FROM orders WHERE customerNumber = 141 AND status = 'Shipped') as Shipped,(SELECT count(*) as 'canceled'FROM orders WHERE customerNumber = 141 AND status = 'Canceled') as Canceled,(SELECT count(*) as 'resolved'FROM orders WHERE customerNumber = 141 AND status = 'Resolved') as Resolved,(SELECT count(*) as 'disputed'FROM orders WHERE customerNumber = 141 AND status = 'Disputed') as Disputed");

        rs.next();

        int exp_shipped = rs.getInt("shipped");
        int exp_canceled = rs.getInt("canceled");
        int exp_resolved = rs.getInt("resolved");
        int exp_disputed = rs.getInt("disputed");

        if(shipped==exp_shipped && canceled==exp_canceled && resolved==exp_resolved && disputed== exp_disputed)
            Assert.assertTrue(true);
        else
            Assert.assertTrue(false);
    }

    @Test(priority = 6)
    void test_GetCustomerShipping() throws SQLException {

        cStmt = con.prepareCall("{call GetCustomerShipping(?,?)}");
        cStmt.setInt(1,112);

        cStmt.registerOutParameter(2, Types.VARCHAR);

        cStmt.executeQuery();

        String shippedTime = cStmt.getString(2);

        Statement stmt=con.createStatement();
        rs = stmt.executeQuery("SELECT country,CASE WHEN country='USA' THEN '2-day Shipping' WHEN country='Canada' THEN '3-day Shipping' ELSE '5-day Shipping' END as ShippingTime FROM customers WHERE customerNumber=112");

        rs.next();

        String exp_shippingTime = rs.getString("ShippingTime");

        Assert.assertEquals(shippedTime,exp_shippingTime);

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

