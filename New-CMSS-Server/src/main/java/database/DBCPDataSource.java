package database;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

import config.Configuration;

public class DBCPDataSource {
	private static BasicDataSource ds = new BasicDataSource();

	static {
		ds.setDriverClassName(Configuration.getProperties("server.databaseDriver"));
        ds.setUrl(Configuration.getProperties("server.urlDatabase"));
        ds.setUsername(Configuration.getProperties("server.usernameMySQL"));
        ds.setPassword(Configuration.getProperties("server.passwordMySQL"));
        ds.setMinIdle(5); // minimum number of idle connections in the pool
        ds.setInitialSize(10);
        ds.setMaxIdle(10); // maximum number of idle connections in the pool
        ds.setMaxOpenPreparedStatements(100);
	}

	public static Connection getConnection() throws SQLException {
		return ds.getConnection();
	}

	private DBCPDataSource() {
	}
}
