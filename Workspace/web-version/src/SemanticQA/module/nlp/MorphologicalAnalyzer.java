package SemanticQA.module.nlp;

import SemanticQA.constant.Type;
import SemanticQA.interfaces.DBConnector;

public abstract class MorphologicalAnalyzer {
	
	private DBConnector DB;
	
	public MorphologicalAnalyzer(DBConnector db) {
		this.DB = db;
	}

	protected String getWordType(String token) {
		
		String type = DB.query(token);
		
		if ( type == null ) {
			if ( isVerba( token ) ) {
				type = Type.Token.VERBA;
			} else if ( isAdjectiva( token ) ) {
				type = Type.Token.ADJEKTIVA;
			} else if ( isAdverbia( token ) ) {
				type = Type.Token.ADVERBIA;
			} else {
				type = Type.Token.NOMINA;
			}
		}
		return type;
	}
	
	private boolean isAdverbia( String token ) {
		
		if ( token.matches("^(se)?([a-z]*)(nya)+$") ) {
			
			token = token.replaceAll("^(se)?([a-z]*)(nya)+$", "$2");
			
			if ( DB.query(token) != null ) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isAdjectiva(String token) {
		
		if ( token.matches("^(pen?)+.*") ) {
			token = token.replaceAll("^(pen?)", "");
			
			if ( DB.query(token) != null ) {
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
			
			if ( DB.query(token) != null ) {
				return true;
			}
			
		}
		return false;
	}

}
