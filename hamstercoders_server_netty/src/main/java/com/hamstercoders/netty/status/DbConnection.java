package com.hamstercoders.netty.status;

import javax.sql.ConnectionPoolDataSource;
import java.sql.*;
import java.util.Properties;

public class DbConnection {
    private String host;
    private String root;
    private String password;
    private String nameDb;
    private String url;

    private Properties properties = new Properties();
    private Connection mysqlConnect = null;

    public DbConnection(String host, String root, String password, String nameDb) {
        this.host = host;
        this.root = root;
        this.password = password;
        this.nameDb = nameDb;
    }

    public void initProperties() {
        url = "jdbc:mysql://" + host + "/" + nameDb;

        properties.setProperty("user", root);
        properties.setProperty("password", password);
        properties.setProperty("characterEncoding", "UTF-8");
        properties.setProperty("useUnicode", "true");
    }

    public void init() {
        if (mysqlConnect == null) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                mysqlConnect = DriverManager.getConnection(url, root, password);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public ResultSet query(String query) {
        ResultSet result = null;
        Statement st = null;
        try {
            st = mysqlConnect.createStatement();
            result = st.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void updateQuery(String query) {
        Statement st = null;
        try {
            st = mysqlConnect.createStatement();
            st.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            mysqlConnect.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
