/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SemanticQA.models.nlp;

import java.util.ArrayList;
import java.util.List;

import SemanticQA.constant.Token;

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
public class Tokenizer extends MorphologicalAnalyzer {
	
	public interface TokenizerListener {
		public void onTokenizeSuccess(List<TokenModel> TOKEN);
	}
    
    public List<TokenModel> tokenize(String sentence){
        
    	// split kalimat menjadi token-token kata
    	String[] splittedSentence = sentence.split(" ");
    	
    	///////////////////////////////////////////////////////////////
    	// siapkan objek array list TokenModel yang akan digunakan   //
    	// untuk menyimpan data hasil pengecekan kelas kata          //
    	///////////////////////////////////////////////////////////////
    	List<TokenModel> token = new ArrayList<TokenModel>();
    	
    	for(String word: splittedSentence){
    		int num = -1;
    		String wordType = null;
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
    		} catch (NumberFormatException e) {
    			e.getMessage();
    		} 
    		
    		wordType = ( num == -1 ) ? super.getWordType(word) : Token.TYPE_NUMERALIA; 
    		
    		TokenModel tm = new TokenModel();
    		// masukkan kata (token) dan tipe katanya
    		// ke dalam objek tm
    		tm.setTokenType(wordType);
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
