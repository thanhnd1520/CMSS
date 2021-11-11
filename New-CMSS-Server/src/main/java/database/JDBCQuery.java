package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import entity.User;

public class JDBCQuery {
	public static User getUser(String userName) {
		try {
			Connection conn = DBCPDataSource.getConnection();
			String sql = "SELECT * FROM cmss.user where username = ?";
			PreparedStatement pre = conn.prepareStatement(sql);
			pre.setString(1,userName);
			ResultSet result = pre.executeQuery();
			if(result.next()) {
				String userNameResult = result.getString(1);
				String passwordResult = result.getString(2);
				return new User(userNameResult, passwordResult);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
