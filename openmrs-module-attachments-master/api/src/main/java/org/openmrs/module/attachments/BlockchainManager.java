package org.openmrs.module.attachments;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.ClosedByInterruptException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BlockchainManager {
	
	static Connection addresscon;
	
	final String commoncli = "multichain-cli chain4 ";
	
	public int getuserpermission(String stream, String address, String typeof) {
		int status = 2;
		int curtotalitems = 0;
		int flag = 0;
		String cmd = commoncli + "liststreamkeys " + stream + "access " + typeof;
		System.out.println(cmd);
		Process p1;
		try {
			p1 = Runtime.getRuntime().exec(cmd);
			p1.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p1.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				
				if (line.contains("items")) {
					System.out.println(line);
					String Str1 = line.replace(" ", "");
					System.out.println(Str1);
					String[] words = Str1.split(":|,", 4);
					System.out.println(words[1]);
					curtotalitems = Integer.parseInt(words[1]);
					System.out.println(curtotalitems);
				}
			}
			
			for (int i = curtotalitems; i > -1; i--) {
				cmd = commoncli + "liststreamkeyitems " + stream + "access " + typeof + ' ' + "false " + "1 "
				        + Integer.toString(i);
				p1 = Runtime.getRuntime().exec(cmd);
				p1.waitFor();
				reader = new BufferedReader(new InputStreamReader(p1.getInputStream()));
				while ((line = reader.readLine()) != null) {
					if (line.contains("data")) {
						System.out.println(line);
						String Str1 = line.replace(" ", "");
						System.out.println(Str1);
						String[] words = Str1.split(":|,", 10);
						System.out.println(words[1]);
						words[1] = words[1].replace("\"", "");
						System.out.println(words[1]);
						String hospaddr = convertHexToString(words[1]);
						System.out.println(hospaddr);
						if (address.equals(hospaddr.substring(1))) {
							if (hospaddr.substring(0, 1).equals("0")) {
								System.out.println("deny");
								status = 0;
								flag = 1;
								break;
							}
							if (hospaddr.substring(0, 1).equals("1")) {
								System.out.println("success");
								status = 1;
								flag = 1;
								break;
							}
							
						}
					}
				}
				if (flag == 1) {
					break;
				}
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return status;
	}
	
	public String queryaddr(String hosp) {
		connectdb();
		String query = "SELECT pubaddress FROM address WHERE name='" + hosp + "'";
		System.out.println(query);
		String hospaddr = "";
		try {
			Statement statement = addresscon.createStatement();
			ResultSet rs = statement.executeQuery(query);
			while (rs.next()) {
				hospaddr = rs.getString("pubaddress");
			}
			System.out.println(hospaddr);
			
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		closedb();
		return hospaddr;
	}
	
	public String convertHexToString(String hex) {
		
		StringBuilder sb = new StringBuilder();
		StringBuilder temp = new StringBuilder();
		
		// 49204c6f7665204a617661 split into two characters 49, 20, 4c...
		for (int i = 0; i < hex.length() - 1; i += 2) {
			
			// grab the hex in pairs
			String output = hex.substring(i, (i + 2));
			// convert hex to decimal
			int decimal = Integer.parseInt(output, 16);
			// convert the decimal to character
			sb.append((char) decimal);
			
			temp.append(decimal);
		}
		System.out.println("Decimal : " + temp.toString());
		
		return sb.toString();
	}
	
	public void connectdb() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		}
		catch (ClassNotFoundException e) {
			System.out.println("Where is your MySQL JDBC Driver?");
			e.printStackTrace();
			return;
		}
		// String urlhosp = "jdbc:mysql://10.114.56.240:3306/hosp1";
		String urladr = "jdbc:mysql://10.114.56.240:3306/addressdb";
		String username = "root";
		String password = "password";
		System.out.println("Connecting database...");
		try {
			
			addresscon = DriverManager.getConnection(urladr, username, password);
			System.out.println("Address Database connected!");
		}
		catch (SQLException e) {
			throw new IllegalStateException("Cannot connect the database!", e);
		}
		
	}
	
	public void publishtostream(String stream, String key, String value) {
		
		String hexstring = asciiToHex(value);
		System.out.println(hexstring);
		String cmd = commoncli + "publish " + stream + " " + key + " " + hexstring;
		System.out.println(cmd);
		Process p1;
		
		try {
			p1 = Runtime.getRuntime().exec(cmd);
			p1.waitFor();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private static String asciiToHex(String asciiStr) {
		char[] chars = asciiStr.toCharArray();
		StringBuilder hex = new StringBuilder();
		for (char ch : chars) {
			hex.append(Integer.toHexString((int) ch));
		}
		
		return hex.toString();
	}
	
	private static String hexToAscii(String hexStr) {
		StringBuilder output = new StringBuilder("");
		
		for (int i = 0; i < hexStr.length(); i += 2) {
			String str = hexStr.substring(i, i + 2);
			output.append((char) Integer.parseInt(str, 16));
		}
		
		return output.toString();
	}
	
	public void closedb() {
		try {
			addresscon.close();
			System.out.println("Address Database disconnected!");
			
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
