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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import SemanticQA.helpers.Constant;
import SemanticQA.helpers.TaskListener;
import SemanticQA.models.ontology.OntologyMapper;

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
	
	private List<Map<String,String>> TOKEN;
	private Connection CONNECTION;
	private OntologyMapper ontoMapper;
	
	public interface TokenizerListener extends TaskListener{
		public void onTokenizeSuccess(List<Map<String,String>> taggedToken);
	}
    
	public Tokenizer() {
		TOKEN = new ArrayList<>();
		CONNECTION = initDatabase();
		ontoMapper = new OntologyMapper();
	}
	
    public void tokenize(String sentence, TokenizerListener listener){
        	
    	List<String> temporaryList = new ArrayList<>(Arrays.asList(sentence.split(" ")));
    	
    	for(String word: temporaryList){
    		Map<String,String> item = new HashMap<>();
    		item.put(Constant.KEY_TOKEN_WORD, word);
    		
    		Thread t1 = new Thread(new Runnable() {
				
				@Override
				public void run() {
					synchronized (item) {
						item.put(Constant.KEY_TOKEN_WORD_TYPE, checkWordType(word));
					}
				}
				
			});
    		
    		Thread t2 = new Thread(new Runnable() {
				
				@Override
				public void run() {
					synchronized (item) {
						item.put(Constant.KEY_TOKEN_SEMANTIC_TYPE, ontoMapper.getType(word));
					}
				}
				
			});
    		
    		t1.start();
    		t2.start();
    		
    		try{
    			t1.join();
    			t2.join();
    			TOKEN.add(item); 
    		} catch(Exception e){
    			
    		}
    		
    	}
    	listener.onTokenizeSuccess(TOKEN);
        
    }
     


    private String checkWordType(String word){
    	
    	String result = null;
    	String sql = "SELECT katadasar,kode_katadasar FROM tb_katadasar WHERE katadasar='"+word+"'";
    	
		try {
			Statement stmt = CONNECTION.createStatement();
			ResultSet queryResult = stmt.executeQuery(sql);
			
			if(queryResult.isBeforeFirst()){
				queryResult.absolute(1);
				result = queryResult.getString("kode_katadasar");
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
            Class.forName(Constant.DB_DRIVER).newInstance();
            
            return DriverManager.getConnection(Constant.DB_URL + Constant.DB_NAME, Constant.DB_USER, Constant.DB_PASS);
        }
        catch( IllegalAccessException | ClassNotFoundException | InstantiationException | SQLException e ){
            
        }
        return null;
    }
    
    
}
