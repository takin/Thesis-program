package SemanticQA.models.nlp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import SemanticQA.constant.Token;

public class Parser {

	public static final String TOKEN_LIST = "tokenList";
	public static final String PARSE_RESULT = "parseResult";
	
	private List<QATokenModel> parseResult;
	Map<String,List<QATokenModel>> result;
	
	public static interface ParserListener{
		public void onParseSuccess(TreeMap<String, String> parseTree);
	}
	
	public Parser() {
		parseResult = new ArrayList<QATokenModel>();
		result = new HashMap<>();
	}
	
	/**
	 * Entry method untuk melakukan proses analisa fungsi sintaksis kalimat tanya
	 * @param array list QATokenModel dari hasil proses tokenisasi
	 * @return HashMap yang berisi arraylist QATokenModel hasil tokenisasi dan hasil 
	 * proses analisa fungsi sintaksis.
	 */
	public Map<String,List<QATokenModel>> parse(List<QATokenModel> taggedToken){
		
		createPhrase(null, taggedToken);
		analyzeSyntacticFunction();
		analyzeSemanticRole();
		
		result.put(TOKEN_LIST, taggedToken);
		result.put(PARSE_RESULT, parseResult);
		
		return result;
	}
	
	/**
	 * Method untuk membentuk frasa dari token-token kata.
	 * Proses pembentukan frasa dilakukan secara rekursi hingga rray list data habis
	 * 
	 * @param Token yang diproses sebelumnya
	 * @param Sisa Array List token yang akan diproses
	 * @param Arraylist frasa yang sudah berhasil dibentuk
	 * @return
	 */
	private boolean createPhrase(QATokenModel previousToken, List<QATokenModel> data){
		
		/**
		 * Flag untuk menentukan apakah latestProcessedPhrase harus diupdate atau tidak.
		 * 
		 * Nilai true menandakan bahwa token yang di proses saat ini memiliki tipe yang 
		 * memenuhi syarat sebagai bagian dari frasa saat ini sehingga prosesnya adalah 
		 * cukup menambahkan token yang sedang di proses menjadi bagian dari frasa yang ada saat ini. 
		 */
		boolean justUpdate = false;
		
		/**
		 * Temporary variable untuk menyimpan tipe frasa yang baru.
		 * Dalam proses pengecekan token ada kemungkinan terjadiny perubahan tipe frasa
		 * yang disebabkan oleh tipe kata yang sedang di proses saat ini.
		 */
		String temporaryPhraseType = null;
		
		/**
		 * Ambil nilai index terakhir dari array list parseResult.
		 * Index ini dibutuhkan untuk melakukan update terhadap objek frasa yang sedang di proses
		 */
		int latestPhraseIndex = this.parseResult.isEmpty() ? -1 : this.parseResult.size() - 1;
		
		/**
		 * Ambil objek dari array list token untuk di proses.
		 * Pengambilan dilakukan dengan cara memotong langsung current object segingga tidak perlu dilakukan 
		 * pemotongan pada saat akan dilakukan proses rekursi di akhir.
		 */
		QATokenModel currentToken = data.remove(0);
		
		/**
		 * Ambil objek frasa yang terkahir sehingga memudahkan proses pembandingan dengan 
		 * objek token yang sedang di proses.
		 */
		QATokenModel latestProcessedPhrase = latestPhraseIndex == -1 ? null : this.parseResult.get(latestPhraseIndex);
		
		/**
		 * jika sudah ada token yang di proses sebelumnya
		 * maka lakukan proses pembentukan frasa dengan mempertimbangkan token yang di proses sebelumnya
		 */
		if ( latestPhraseIndex != -1 ) {
		
			/**------------------------------------------------------------------------------------*
			 * Pembentukan FRASA PRONOMINAL														   *
			 * ------------------------------------------------------------------------------------*
			 * Aturan:																			   *
			 * 1. [ Pron + Num Kolektif ] 														   *
			 * 2. [ Pron + Pron Penjunjuk ]														   *
			 * 3. [ Pron + <sendiri> ]															   *
			 *-------------------------------------------------------------------------------------*/
			if ( latestProcessedPhrase.getWordType().equals(Token.TYPE_PRONOMINA) || 
					previousToken.getWordType().equals(Token.TYPE_PRONOMINA) && 
					currentToken.getWord().matches("(berdua|sekalian|semua|ini|itu|sendiri)") ) {
				
				justUpdate = true;
				temporaryPhraseType = Token.TYPE_FRASA_PRONOMINAL;
				
			}
			
			/**------------------------------------------------------------------------------------*
			 * Pembentukan FRASA NOMINAL														   *
			 * ------------------------------------------------------------------------------------*
			 * Aturan:																			   *
			 * 1. [ N + N* + Pron Persona + Pron Penunjuk ]										   *
			 * 2. [ N + Adj + Pron Persona + Pron Penunjuk ]									   *
			 * 3. [ N + Pron Persona + <yang> + Adj + Pron Penunjuk ]							   *
			 *-------------------------------------------------------------------------------------*/
			if ( latestProcessedPhrase.getWordType().equals( Token.TYPE_NOMINA ) || 
					latestProcessedPhrase.getWordType().equals( Token.TYPE_FRASA_NOMINAL ) ) {
				
				if ( previousToken.getWordType().equals( Token.TYPE_NOMINA ) && 
						( currentToken.getWordType().equals( Token.TYPE_NOMINA ) || 
								currentToken.getWordType().equals( Token.TYPE_ADJEKTIVA ) || 
								currentToken.getWord().matches("(saya|aku|anda|kau|dia|ia|beliau|kami|kita)") ) ) {
					
					justUpdate = true;
					temporaryPhraseType = Token.TYPE_FRASA_NOMINAL;
					
				}
				
			}
			
			/**------------------------------------------------------------------------------------*
			 * Pembentukn FRASA NUMERALIA														   *
			 * ------------------------------------------------------------------------------------*
			 * Aturan:																			   *
			 * 1. [ Num + N Penggolong ]														   *
			 *-------------------------------------------------------------------------------------*/
			if ( ( latestProcessedPhrase.getWordType().equals( Token.TYPE_NUMERALIA ) || 
					latestProcessedPhrase.getWordType().equals( Token.TYPE_FRASA_NUMERALIA ) ) && 
					currentToken.getWord().matches("(buah|ekor)") ) {
				
				justUpdate = true;
				temporaryPhraseType = Token.TYPE_FRASA_NUMERALIA;
				
			}
			
			/**
			 * Jika currentToken = "yang", maka perlu pengecekan lebih kompleks.
			 * Treatment khusus untuk kata "yang"
			 * 1. Frasa akan menjadi FN jika "yang" hadir diantara FN dan Adj -> Anak saya (FN) + yang + pintar(Adj)
			 */
			
			
		}
		
		if ( justUpdate ) {
			
			latestProcessedPhrase.setWord( latestProcessedPhrase.getWord() + " " + currentToken.getWord() );
			
			if ( temporaryPhraseType != null ) {
				latestProcessedPhrase.setWordType(temporaryPhraseType);
			}
			
			this.parseResult.set(latestPhraseIndex, latestProcessedPhrase);
			
		} else {
			this.parseResult.add( currentToken );
		}
		
		// lakukan proses secara rekursi hingga data habis
		if ( data.size() > 0 ) {
			createPhrase( currentToken, data );
		}
		return true;
	}
	
	/**
	 * Method untuk menganalisa fungsi sintaksis dari masing-masing frasa
	 */
	private boolean analyzeSyntacticFunction(){
		
		/**
		 * Jika jumlah frasa hanya dua, maka kemungkinan bentuk fungsi sintaksisnya adalah:
		 * 1. PS
		 * 2. SP
		 * 
		 * Unsur frasa keduanya berupa FN, jika salah satu FN mengandung partikel -lah
		 * maka FN tersebut sebagai Predikat. Jika keduanya tidak mengandung -lah,
		 * maka anggap pola kalimat adalah SP
		 */
		if(this.parseResult.size() == 2){
			
		}

		
		return false;
	}
	
	private boolean analyzeSemanticRole(){
		
		return true;
	}
	
	private static boolean isPrepositional(QATokenModel model){
		return model.getWordType().equals(Token.TYPE_PREPOSISI) || model.getWordType().equals(Token.TYPE_FRASA_PREPOSISIONAL);
	}
	
	private static boolean isPhrasePronominal(QATokenModel model){
		return model.getWordType().equals(Token.TYPE_PRONOMINA) || model.getWordType().equals(Token.TYPE_FRASA_PRONOMINAL);
	}
	
	private static boolean isPhraseNominal(QATokenModel model){
		return model.getWordType().equals(Token.TYPE_NOMINA) || model.getWordType().equals(Token.TYPE_FRASA_NOMINAL);
	}
}
