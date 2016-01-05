/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SemanticQA.module.nlp;

import java.util.ArrayList;
import java.util.List;

import SemanticQA.constant.Type;
import SemanticQA.interfaces.DBConnector;
import SemanticQA.model.SemanticToken;


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
public class Tokenizer extends MorphologicalAnalyzer  {
    
	public Tokenizer(DBConnector connector) {
		super(connector);
	}
	
    public List<SemanticToken> tokenize(String sentence) throws Exception {
        
    	// split kalimat menjadi token-token kata
    	String[] splittedSentence = sentence.split("\\s+");
    	
    	///////////////////////////////////////////////////////////////
    	// siapkan objek array list TokenModel yang akan digunakan   //
    	// untuk menyimpan data hasil pengecekan kelas kata          //
    	///////////////////////////////////////////////////////////////
    	
    	List<SemanticToken> token = new ArrayList<SemanticToken>();
    	
    	for(String word: splittedSentence){
    		int num = -1;
    		String type = null;
    		//////////////////////////////////////////////////////////////////////
    		// sebelum melakukan proses pengecekan tipe kata ke dalam 			//
    		// database, cek terlebih dahulu apakah token tersebut 				//
    		// berupa angka atau tidak.											//
    		//																	//
    		// jika berupa angka, maka kemungkinan angka tersebut adalah		//
    		// tahun atau tanggal, sehingga tidak perlu dicek ke dalam database //
    		// dan langsung diberikan tipe NUMERALIA.							//
    		//																	//
    		//////////////////////////////////////////////////////////////////////
    		try {
    			num = Integer.parseInt(word);
    		} catch (NumberFormatException e) {} 
    		
    		try {
    			type = ( num == -1 ) ? super.getWordType(word) : Type.Token.NUMERALIA;
    		} catch (Exception e) {
    			throw new Exception("Gagal melakukan proses tokenisasi kalimat");
    		}
    		
    		SemanticToken tm = new SemanticToken();
    		// masukkan kata (token) dan tipe katanya
    		// ke dalam objek tm
    		tm.setType(type);
    		tm.setToken(word);
    		
    		token.add(tm);   
    	}
    	
    	///////////////////////////////////////////////////////////////////
    	//																 //
    	// Pada stage ini, field TokenModel dalam array list yang terisi //
    	// hanya kata dan tipe kata (hasil pengecekan ke dalam database  //
    	//																 //
    	///////////////////////////////////////////////////////////////////
    	return token;
    }
}
