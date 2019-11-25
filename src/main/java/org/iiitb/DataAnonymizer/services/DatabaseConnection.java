package org.iiitb.DataAnonymizer.services;

import java.sql.DriverManager;
//import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

public class DatabaseConnection {
	Statement statement;
	ResultSet resultSet;
	Connection connection = null;
	String query = null;

	// Constructor for opening the Database Connection

	public DatabaseConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("Driver Found");
		}

		catch (ClassNotFoundException e) {
			System.out.println("Driver Not Found: " + e);
		}

		String url = "jdbc:mysql://localhost/mydata";
		String user = "root";
		String password = "root";
		connection = null;

		try {
			connection = (Connection) DriverManager.getConnection(url, user, password);
			System.out.println("Successfully Connected to Database");
		} catch (SQLException e) {
			System.out.println("SQL Exception: " + e);
		}

	}

	public ResultSet authenticateUser(String username, String password) {

		java.sql.PreparedStatement preparedStatement = null;
		try {
			query = "select * from user_info where username=? and password=?";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, username);
			preparedStatement.setString(2, password);
			resultSet = preparedStatement.executeQuery();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return resultSet;

	}
}
