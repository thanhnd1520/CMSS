package database;

import java.sql.Connection;
import java.sql.DriverManager;

import config.Configuration;

public class MyConnection {
    private static Connection connection;
	
	public static Connection getConnection() {
		if(connection == null) {
			try {
				String connectString = Configuration.getProperties("server.urlDatabase");
		        Class.forName(Configuration.getProperties("server.databaseDriver"));
		        connection = (Connection) DriverManager.getConnection(connectString, "root", "123456789");
			}
			catch (Exception e) {
				System.out.println(e.toString());
			}
		}
		return connection;
	}
}