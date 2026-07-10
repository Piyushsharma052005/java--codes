package com.sait.ps;

import java.sql.*;

public class ReadData {

	public static void main(String[] args) {
		try {
			Connection c=DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/sait",
					"root","root");
			PreparedStatement ps=c.prepareStatement("select * from student where name=?");
			ps.setString(1,"Kaliya");
			ResultSet rs=ps.executeQuery();
			int count=0;
			while(rs.next()) {
				count++;
				System.out.println(rs.getInt("id"));
				System.out.println(rs.getString("name"));
				System.out.println(rs.getString("phone"));
				
				System.out.println("------------");
			}
			if(count==0) {
				System.out.println("No record found!!");
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}