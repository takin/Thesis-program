package SemanticQA.models.nlp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import SemanticQA.constant.Database;
import SemanticQA.constant.Token;
import SemanticQA.helpers.DBInterface;

public class MorphologicalAnalyzer implements DBInterface {
	
	private Connection DB;
	
	public MorphologicalAnalyzer() {
		this.connect();
	}

	protected String getWordType(String token) {
		
		String type = query(token);
		
		if ( type == null ) {
			
			if ( isVerba( token ) ) {
				type = Token.TYPE_VERBA;
			} else if ( isAdjectiva( token ) ) {
				type = Token.TYPE_ADJEKTIVA;
			} else if ( isAdverbia( token ) ) {
				type = Token.TYPE_ADVERBIA;
			} else {
				type = Token.TYPE_NOMINA;
			}
			
		}
		
		return type;
	}
	
	private boolean isAdverbia( String token ) {
		
		if ( token.matches("^(se)?([a-z]*)(nya)+$") ) {
			
			token = token.replaceAll("^(se)?([a-z]*)(nya)+$", "$2");
			
			if ( query(token) != null ) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isAdjectiva(String token) {
		
		if ( token.matches("^(pen?)+.*") ) {
			token = token.replaceAll("^(pen?)", "");
			
			if ( query(token) != null ) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isVerba(String token) {

		if ( token.matches("^(me[mng?]?|ter)+.*") ){
			
			token = token.replaceAll("^(me[mng?]?|ter)", "");
			
			if ( token.matches("^g.*") ) {
				token = token.replaceAll("^g", "");
			}
			
			if ( query(token) != null ) {
				return true;
			}
			
		}
		return false;
	}
	
	@Override
	public void connect() {
		try{
            Class.forName(Database.DB_DRIVER).newInstance();
            DB = DriverManager.getConnection(Database.DB_URL + Database.DB_NAME, Database.DB_USER, Database.DB_PASS);
        }
        catch( IllegalAccessException | ClassNotFoundException | InstantiationException | SQLException e ){
           e.printStackTrace(); 
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
	public String query(String word) {
		System.out.print(word + " -> ");
		String result = null;
    	String sql = "SELECT kata,kode FROM katadasar WHERE kata='"+word+"'";
    	
		try {
			Statement stmt = DB.createStatement();
			ResultSet queryResult = stmt.executeQuery(sql);
			
			if(queryResult.isBeforeFirst()){
				queryResult.absolute(1);
				result = queryResult.getString("kode");
			}
			
			queryResult.close();
		    stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println(result);
		return result;
	}
}
