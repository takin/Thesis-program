package SemanticQA.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import SemanticQA.interfaces.DBConnector;

public class MySQLDatabase implements DBConnector {

	private Connection DB;
	
	public MySQLDatabase() throws Exception {
		this( "com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/kamuskata", "root", "root");
	}
	
	public MySQLDatabase(String driver, String url, String username, String password) throws Exception {
		connect(driver, url, username, password);
	}

	@Override
	public void connect(String driver, String url, String username,
			String password) throws Exception {
		try{
            Class.forName(driver).newInstance();
             DB = DriverManager.getConnection(url, username, password);
        }
        catch( IllegalAccessException | ClassNotFoundException | InstantiationException | SQLException e ){
           throw new Exception("Unable to connect to word type database"); 
        }
	}

	@Override
	public void close() {
		try {
			DB.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String query(String token) throws Exception {
		String result = null;
    	String sql = "SELECT tipe FROM katadasar WHERE kata='"+token+"'";
    	
		try {
			Statement stmt = DB.createStatement();
			ResultSet queryResult = stmt.executeQuery(sql);
			
			if(queryResult.isBeforeFirst()){
				queryResult.absolute(1);
				result = queryResult.getString("tipe");
			}
			
			queryResult.close();
		    stmt.close();
		} catch (SQLException e) {
			throw new Exception("Word type checking failed!");
		}
		
		return result;
	}
}
