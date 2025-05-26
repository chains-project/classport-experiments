package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.h2.tools.Server;

public class Main {
    public static void main(String[] args) throws Exception {
        // Start the H2 Web Console on port 8082
        Server webServer = Server.createWebServer(
            "-webPort", "8082", "-webAllowOthers"
        ).start();
        System.out.println("Web console started at: " + webServer.getURL());

        // Start the TCP server on port 9092
        Server tcpServer = Server.createTcpServer(
            "-tcpPort", "9092", "-tcpAllowOthers"
        ).start();
        System.out.println("TCP server started at: " + tcpServer.getURL());

        // JDBC connection to an in-memory H2 database
        String jdbcUrl = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"; // Keep in-memory DB alive
        try (Connection conn = DriverManager.getConnection(jdbcUrl, "sa", "");
             Statement stmt = conn.createStatement()) {

            // Run some SQL
            stmt.execute("CREATE TABLE test(id INT PRIMARY KEY, name VARCHAR(255))");
            stmt.execute("INSERT INTO test VALUES(1, 'Alice'), (2, 'Bob')");

            // Query and print results
            ResultSet rs = stmt.executeQuery("SELECT * FROM test");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + ": " + rs.getString("name"));
            }
            conn.close();
            stmt.close();

             }

    }
}