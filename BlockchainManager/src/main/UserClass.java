package main;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;


import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;


import java.sql.*;

public class UserClass extends Application{
	 
	static Connection addresscon;
	final String commoncli = "multichain-cli chain4 ";
	String user1 = "user1";
	String user2 = "user2";
	String curuser;
	
	public static void main(String[] args) {
		
	launch(args);
	}
	 
	@Override
	public void start(Stage primaryStage) throws Exception {
	
	primaryStage.setTitle("Login");
	BorderPane bp = new BorderPane();
	bp.setPadding(new Insets(10,50,50,50));
	HBox hb = new HBox();
    hb.setPadding(new Insets(20,20,20,30));
    
    //Adding GridPane
    GridPane gridPane = new GridPane();
    gridPane.setPadding(new Insets(20,20,20,20));
    gridPane.setHgap(5);
    gridPane.setVgap(5);
		
    Label lblUserName = new Label("Username");
    final TextField txtUserName = new TextField();
    Label lblPassword = new Label("Password");
    final PasswordField pf = new PasswordField();
    Button btnLogin = new Button("Login");	
    final Label lblMessage = new Label();
    
    gridPane.add(lblUserName, 0, 0);
    gridPane.add(txtUserName, 1, 0);
    gridPane.add(lblPassword, 0, 1);
    gridPane.add(pf, 1, 1);
    gridPane.add(btnLogin, 2, 1);
    gridPane.add(lblMessage, 1, 2);
    
    Text text = new Text("Login");
    text.setFont(Font.font ("Verdana", 20));
    hb.getChildren().add(text);
    
    bp.setId("bp");
    gridPane.setId("root");
    btnLogin.setId("btnLogin");
    text.setId("text");
    
    btnLogin.setOnAction(new EventHandler<ActionEvent>() {
    	public void handle(ActionEvent event) {
    		String checkUser = txtUserName.getText().toString();
    		String checkPw = pf.getText().toString();
    		if(checkUser.equals(user1) && checkPw.equals(user1)){
    			lblMessage.setText("Congratulations User1!");
    			curuser=user1;
    			lblMessage.setTextFill(Color.GREEN);
    			try {
					loginsuccess(primaryStage);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    		else if(checkUser.equals(user2) && checkPw.equals(user2)){
    			lblMessage.setText("Congratulations User2!");
    			curuser=user2;
    			lblMessage.setTextFill(Color.GREEN);
    			try {
					loginsuccess(primaryStage);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			
    		}
    		else {
    			lblMessage.setText("Incorrect user or pw.");
    			lblMessage.setTextFill(Color.RED);
    		}
    		txtUserName.setText("");
    		pf.setText("");
    	}
});
    
    
    bp.setTop(hb);
    bp.setCenter(gridPane); 

    Scene scene = new Scene(bp);
    primaryStage.setScene(scene);

	primaryStage.show();
	
	}
	
	public void loginsuccess(Stage primaryStage) throws IOException {
		Text t2 = new Text();	
		String line;
		primaryStage.setTitle("Blockchain Control");
		
//		BufferedReader abc = new BufferedReader(streamReader);
//		ClassLoader classLoader = getClass().getClassLoader();
//		File file = new File(classLoader.getResource("/hospitalconfig.txt").getFile());
		BufferedReader abc = new BufferedReader(new FileReader("./hospitalconfig.txt"));
		//InputStream input = getClass().getResourceAsStream("/hospitalconfig.txt");
		ObservableList<String> lines = FXCollections.observableArrayList();
//		try Scanner scanner = new Scanner(file)) {
	//	
//		while (scanner.hasNextLine()) {
		while((line = abc.readLine()) != null) {
//		//	line1 = scanner.nextLine();
			lines.add(line);
		    System.out.println(line);
		}
//		scanner.close();

		
		abc.close();
//		System.out.println(lines);
		final ComboBox<String> comboBox = new ComboBox<String>(lines);
//		TextField textField = new TextField();
		Button btn = new Button("Submit");
		Button access = new Button("List of Accesses");
		access.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					readfromstream(curuser);
				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Clicked Access");
			}
		});
		btn.setOnAction(new EventHandler<ActionEvent>() {
		
		@Override
		public void handle(ActionEvent event) {
		// TODO Auto-generated method stub
		Stage newWindow = new Stage();
		String hosp = comboBox.getValue();
		System.out.println(hosp);
		connectdb();
		String hospaddr = queryaddr(hosp);
		closedb();
		int status = getuserpermission(curuser, hospaddr, "read");
		Button grantread = new Button("Grant Permission to Read");
//		Button grantwrite = new Button("Grant Permission to Write");
		Button revokeread = new Button("Revoke Permission to Read");
//		Button revokewrite = new Button("Revoke Permission to Write");
		Text t1 = new Text();
		t1.setText("Current Status: ");
		if (status == 1) {
			t2.setText("Access Granted!");
		}
		else {
			t2.setText("Access Revoked!");
		}
		BorderPane pane2 = new BorderPane();
		pane2.setPadding(new Insets(70));
		VBox paneCenter2 = new VBox();
		paneCenter2.setSpacing(10);
		pane2.setCenter(paneCenter2);
		paneCenter2.getChildren().add(grantread);
//		paneCenter2.getChildren().add(grantwrite);
		paneCenter2.getChildren().add(revokeread);
		paneCenter2.getChildren().add(t1);
		paneCenter2.getChildren().add(t2);
//		paneCenter2.getChildren().add(revokewrite);
		Scene scene2= new Scene(pane2, 400, 300);
		newWindow.setScene(scene2);
		newWindow.setTitle(hosp);
		newWindow.show();
	   
	    grantread.setOnAction(new EventHandler<ActionEvent>() {
	    	
	    	@Override
	    	public void handle(ActionEvent event) {
	    		grantReadAccess(hosp);
	    		t2.setText("Access Granted! ");
	    	}
	    	});
		
	    revokeread.setOnAction(new EventHandler<ActionEvent>() {
	    	
	    	@Override
	    	public void handle(ActionEvent event) {
	    		revokeReadAccess(hosp);
	    		t2.setText("Access Revoked! ");
	    	}
	    	});
	    
//	    grantwrite.setOnAction((click) ->grantWriteAccess(hosp));
		
//	    revokewrite.setOnAction((click) -> revokeWriteAccess(hosp));
	   
		}

		
		});
		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(70));
		VBox paneCenter = new VBox();
		paneCenter.setSpacing(10);
		pane.setCenter(paneCenter);
		paneCenter.getChildren().add(comboBox);
		//paneCenter.getChildren().add(textField);
		paneCenter.getChildren().add(btn);
		paneCenter.getChildren().add(access);
		Scene scene= new Scene(pane, 400, 200);
		primaryStage.setScene(scene);
		primaryStage.show();
		
	}
	 
	
	public void grantReadAccess(String hosp) {
		connectdb();
		System.out.print("Clicked grant "+hosp+"\n");
		String hospaddr = queryaddr(hosp);
		String tmp = convertStringToHex(hospaddr);
		String tmp1 = convertStringToHex("1");
		tmp = tmp1+tmp;
		String cmd = commoncli + "grant "+hospaddr + " receive,send" ;
		System.out.println(cmd);
		Process p1;
		try {
			p1 = Runtime.getRuntime().exec(cmd);
			p1.waitFor();
			cmd = commoncli + "subscribe "+curuser+"access";
			System.out.println(cmd);
			p1 = Runtime.getRuntime().exec(cmd);
			p1.waitFor();
			cmd = commoncli + "publish " + curuser+"access " + "read" + ' ' + tmp; 
			System.out.println(cmd);
			p1 = Runtime.getRuntime().exec(cmd);
			p1.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		closedb();
	}
	
	
	
	public void revokeReadAccess(String hosp) {
		connectdb();
		System.out.print("Clicked revoke "+hosp+"\n");
		String hospaddr = queryaddr(hosp);
		String tmp = convertStringToHex(hospaddr);
		String tmp1 = convertStringToHex("0");
		tmp = tmp1+tmp;
		String cmd = commoncli + "revoke "+hospaddr + " receive,send" ;
		System.out.println(cmd);
		Process p1;
		try {
			p1 = Runtime.getRuntime().exec(cmd);
			p1.waitFor();
			cmd = commoncli + "subscribe "+curuser+"access";
			System.out.println(cmd);
			p1 = Runtime.getRuntime().exec(cmd);
			p1.waitFor();
			cmd = commoncli + "publish " +curuser+"access " + "read" + ' ' + tmp; 
			System.out.println(cmd);
			p1 = Runtime.getRuntime().exec(cmd);
			p1.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		closedb();
	}
	
	public void revokeWriteAccess(String hosp) {
		String hospaddr = queryaddr(hosp);
		String tmp = convertStringToHex(hospaddr);
		String tmp1 = convertStringToHex("0");
		tmp = tmp1+tmp;
		String cmd = commoncli + "revoke "+hospaddr + " "+curuser+".write" ;
		System.out.println(cmd);
		Process p1;
		try {
			p1 = Runtime.getRuntime().exec(cmd);
			p1.waitFor();
			cmd = commoncli + "subscribe "+curuser+"access";
			System.out.println(cmd);
			p1 = Runtime.getRuntime().exec(cmd);
			p1.waitFor();
			cmd = commoncli + "publish " +curuser+"access " + "write" + ' ' + tmp; 
			System.out.println(cmd);
			p1 = Runtime.getRuntime().exec(cmd);
			p1.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void grantWriteAccess(String hosp) {
		String hospaddr = queryaddr(hosp);
		String tmp = convertStringToHex(hospaddr);
		String tmp1 = convertStringToHex("1");
		tmp = tmp1+tmp;
		String cmd = commoncli + "grant "+hospaddr+ " "+curuser+".write" ;
		System.out.println(cmd);
		Process p1;
		try {
			p1 = Runtime.getRuntime().exec(cmd);
			p1.waitFor();
			cmd = commoncli + "subscribe "+curuser+"access";
			System.out.println(cmd);
			p1 = Runtime.getRuntime().exec(cmd);
			p1.waitFor();
			cmd = commoncli + "publish " +curuser+"access " + "write" + ' ' + tmp; 
			System.out.println(cmd);
			p1 = Runtime.getRuntime().exec(cmd);
			p1.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public String queryaddr(String hosp) {
		String query = "SELECT pubaddress FROM address WHERE name='"+hosp+"'";
		System.out.println(query);
		String hospaddr="";
		try {
			Statement statement = addresscon.createStatement();
			ResultSet rs = statement.executeQuery(query);
			while(rs.next()) {
			hospaddr = rs.getString("pubaddress");	
			}
			System.out.println(hospaddr);
			
				 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hospaddr;
	}
	
	public String convertStringToHex(String str){
		  
		  char[] chars = str.toCharArray();
		  
		  StringBuffer hex = new StringBuffer();
		  for(int i = 0; i < chars.length; i++){
		    hex.append(Integer.toHexString((int)chars[i]));
		  }
		  
		  System.out.println(hex.toString());
		  return hex.toString();
	  }
	
	@Override
	public void stop() {
		try {
			if(addresscon != null) {
			addresscon.close();
			System.out.println("Address Database disconnected!");
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void connectdb() {
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your MySQL JDBC Driver?");
			e.printStackTrace();
			return;
		}
	//	String urlhosp = "jdbc:mysql://10.114.56.240:3306/hosp1";
		String urladr = "jdbc:mysql://10.114.56.240:3306/addressdb";
		String username = "root";
		String password = "password";
		System.out.println("Connecting database...");
		try  {
			
		    addresscon = DriverManager.getConnection(urladr, username, password);
		    System.out.println("Address Database connected!");
		} catch (SQLException e) {
		    throw new IllegalStateException("Cannot connect the database!", e);
		}
	
	}
	
	public void closedb() {
		
		try {
			
			addresscon.close();
			System.out.println("Address Database disconnected!");
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public int getuserpermission(String stream, String address,String typeof) {
		int status=2;
		int curtotalitems = 0;
		int flag = 0;
		String cmd = commoncli + "liststreamkeys " + stream + "access " + typeof;
		System.out.println(cmd);
		Process p1;
		try {
			p1 = Runtime.getRuntime().exec(cmd);
			p1.waitFor();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(p1.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				
				if(line.contains("items")) {
					System.out.println(line);
					String Str1 = line.replace(" ","");
					System.out.println(Str1);
					String[] words = Str1.split(":|,",4);
					System.out.println(words[1]);
					curtotalitems = Integer.parseInt(words[1]);
					System.out.println(curtotalitems);
				}
			}
			
		for(int i = curtotalitems;i>-1;i--) {
			cmd = commoncli + "liststreamkeyitems " + stream + "access " + typeof+' '+"false "+ "1 "+ Integer.toString(i);
			p1 = Runtime.getRuntime().exec(cmd);
			p1.waitFor();
			reader = new BufferedReader(
					new InputStreamReader(p1.getInputStream()));
			while ((line = reader.readLine()) != null) {
				if(line.contains("data")) {
					System.out.println(line);
					String Str1 = line.replace(" ","");
					System.out.println(Str1);
					String[] words = Str1.split(":|,",10);
					System.out.println(words[1]);
					words[1] = words[1].replace("\"","");
					System.out.println(words[1]);
					String hospaddr = convertHexToString(words[1]);
					System.out.println(hospaddr);
					if(address.equals(hospaddr.substring(1))) {
						if(hospaddr.substring(0, 1).equals("0")) {
							System.out.println("deny");
	                           status = 0;
	                           flag = 1;
	                           break; 
						}
						if(hospaddr.substring(0, 1).equals("1")) {
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return status;
	}
	
	public String convertHexToString(String hex){

		  StringBuilder sb = new StringBuilder();
		  StringBuilder temp = new StringBuilder();
		  
		  //49204c6f7665204a617661 split into two characters 49, 20, 4c...
		  for( int i=0; i<hex.length()-1; i+=2 ){
			  
		      //grab the hex in pairs
		      String output = hex.substring(i, (i + 2));
		      //convert hex to decimal
		      int decimal = Integer.parseInt(output, 16);
		      //convert the decimal to character
		      sb.append((char)decimal);
			  
		      temp.append(decimal);
		  }
		 // System.out.println("Decimal : " + temp.toString());
		  
		  return sb.toString();
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
	
	public void readfromstream(String stream) throws IOException, InterruptedException {
		File f;
		if(curuser.equals("user1")) {
		f= new File("/home/sharon/access.txt");
		
		}
		else {
		f= new File("/home/arjun/access.txt");	
		}
		
		
		FileWriter fw =new FileWriter(f);
		BufferedWriter writer = new BufferedWriter(fw);
		
		
		String cmd = commoncli + "liststreamitems " + stream+" false 999999";
		System.out.println(cmd);
		Process p1;
		
			p1 = Runtime.getRuntime().exec(cmd);
			p1.waitFor();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(p1.getInputStream()));
			String line;
			//System.out.println("in try");
			while ((line = reader.readLine()) != null) {
				//System.out.println(line);
				if(line.contains("key")) {
					System.out.println(line);
					String Str1 = line.replace(" ","");
					System.out.println(Str1);
					String[] words = Str1.split(":|,",4);
					System.out.println(words[1]);
					writer.write(words[1]);
				}
				if(line.contains("data")) {
					System.out.println(line);
					String Str1 = line.replace(" ","");
					System.out.println(Str1);
					String[] words = Str1.split(":|,",10);
					System.out.println(words[1]);
					words[1] = words[1].replace("\"","");
					System.out.println(words[1]);
					String accesstxt = hexToAscii(words[1]);
					writer.write(accesstxt);
					writer.newLine();
				}
			}
			
		writer.close();	
		
	}
	
}

