package dbclasses;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
public class Database {
    private static final String DATABASE_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String CONNECTION_URL = "jdbc:mysql://localhost/cloud_storage_db?serverTimezone=Asia/Sakhalin&useSSL=false";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "20021996";
    private Connection connection;



    public Connection connect(){
        if (connection == null) {
            try {
                Class.forName(DATABASE_DRIVER);
                connection = DriverManager.getConnection(CONNECTION_URL, USERNAME, PASSWORD);
            } catch (ClassNotFoundException | SQLException e) {
               log.error("e= ",e);
            }
        }
        return connection;
    }







}
