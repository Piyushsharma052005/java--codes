package com.sait.statement;

import java.sql.*;
public class InsertData {

	public static void main(String[] args) {
		try {
			Connection c=DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/sait",
					"root","root");
			Statement st=c.createStatement();
			st.executeUpdate(
					"insert into student(name,phone)values('Sukesh','4561237893')");
			System.out.println("Data Inserted!!");
		}catch(Exception e) {
			e.printStackTrace();
		}

	}

}