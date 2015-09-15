/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SemanticQA.models.nlp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import SemanticQA.constant.Database;
import SemanticQA.helpers.QATokenModel;

/* ==============================================================================
 POSTagger merupakan bagian dari pre-process NLP
 fungsi kelas ini adalah untuk membentuk token-token dari kata yang diinputkan
 
 Token yang telah dibentuk selanjutnya akan di cek kelas katanya ke dalam 
 database SQL untuk selanjutnya digunakan untuk memberikan TAG (POS TAG)
 sehingga nantinya token tersebut dapat di proses lebih lanjut oleh parser
 
 Apabila sebuah token tidak diketahui kelas katanya, maka kata/token tersebut 
 akan diberikan tag UN (unknwon)
 * @author syamsul
 * =============================================================================*/
public class Tokenizer {
	
	private List<QATokenModel> TOKEN;
	private Connection CONNECTION;
	
	public interface TokenizerListener {
		public void onTokenizeSuccess(List<QATokenModel> TOKEN);
	}
    
	public Tokenizer() {
		TOKEN = new ArrayList<QATokenModel>();
		CONNECTION = initDatabase();
	}
	
    public void tokenize(String sentence, TokenizerListener listener){
        	
    	List<String> temporaryList = new ArrayList<>(Arrays.asList(sentence.split(" ")));
    	
    	for(final String word: temporaryList){
    		QATokenModel tm = new QATokenModel();
    		
    		tm.setWord(word);
    		tm.setWordType(checkWordType(word));
    		
    		TOKEN.add(tm);
    	}
    	listener.onTokenizeSuccess(TOKEN);
        
    }
     
    private String checkWordType(String word){
    	
    	String result = null;
    	String sql = "SELECT kata,kode FROM katadasar WHERE kata='"+word+"'";
    	
		try {
			Statement stmt = CONNECTION.createStatement();
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
		return result;
    }
    
    private Connection initDatabase(){
        
        /**
         * Lakukan inisialisasi koneksi ke database lexicon
         * proses ini harus dilakukan setelah proses inisialisasi tokenizerListener
         * agar apabila terjadi error pada tahapan ini, maka notifikasinya
         * dapat di broadcast ke class subscriber
         */
         try{
            Class.forName(Database.DB_DRIVER).newInstance();
            
            return DriverManager.getConnection(Database.DB_URL + Database.DB_NAME, Database.DB_USER, Database.DB_PASS);
        }
        catch( IllegalAccessException | ClassNotFoundException | InstantiationException | SQLException e ){
            
        }
        return null;
    }
    
    
}
