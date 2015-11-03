package SemanticQA.models.nlp;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import SemanticQA.constant.Token;

public class Parser {

	public static final String TOKEN_LIST = "tokenList";
	public static final String PARSE_RESULT = "parseResult";
	
	private static final String PRONOMINA_PERSONA_PATTERN = "(saya|aku|anda|kau|dia|ia|beliau|kami|kita)";
	private static final String NUMERALIA_KOLEKTIF_PATTERN = "(berdua|sekalian|semua|ini|itu|sendiri)";
	private static final String NOMINA_PENGGOLONG_PATTERN = "(buah|ekor)";
	private static final String PREPOSISI_PENANYA_PATTERN = "(di|ke|dari|bagai)";
	private static final String PRONOMINA_PENJUNJUK_PATTERN = "(ini|itu)";
	private static final String KONJUNGTOR_KLAUSA_PATTERN = "(dan|atau)";
	private static final String PRONOMINA_PENANYA_PATTERN = "(apa|siapa|kapan)";
	
	private List<QuestionModel> parseResult;
	
	public static interface ParserListener{
		public void onParseSuccess(TreeMap<String, String> parseTree);
	}
	
	public Parser() {
		parseResult = new ArrayList<QuestionModel>();
	}
	
	/**
	 * Entry method untuk melakukan proses analisa fungsi sintaksis kalimat tanya
	 * @param array list QATokenModel dari hasil proses tokenisasi
	 * @return HashMap yang berisi arraylist QATokenModel hasil tokenisasi dan hasil 
	 * proses analisa fungsi sintaksis.
	 */
	public List<QuestionModel> parse(List<TokenModel> taggedToken){
		
		createPhrase(null, taggedToken);
		analyzeSyntacticFunction();
//		analyzeSemanticRole();
		
		return parseResult;
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
	private boolean createPhrase(TokenModel previousToken, List<TokenModel> data){
		
		/**
		 * Temporary variable untuk menyimpan tipe frasa yang baru.
		 * Dalam proses pengecekan token ada kemungkinan terjadinya perubahan tipe frasa
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
		TokenModel currentToken = data.remove(0);
		
		/**
		 * Ambil objek frasa yang terkahir sehingga memudahkan proses pembandingan dengan 
		 * objek token yang sedang di proses.
		 */
		QuestionModel latestProcessedPhrase = latestPhraseIndex == -1 ? null : this.parseResult.get(latestPhraseIndex);
		
		/**
		 * jika sudah ada token yang di proses sebelumnya
		 * maka lakukan proses pembentukan frasa dengan mempertimbangkan token yang di proses sebelumnya
		 */
		if ( latestPhraseIndex != -1 && latestProcessedPhrase.getType() != null && !currentToken.getToken().matches(KONJUNGTOR_KLAUSA_PATTERN) ) {
		
			switch (currentToken.getTokenType()) {
			
			// currentToken
			case Token.TYPE_PREPOSISI:
				
				if ( latestProcessedPhrase.getType().equals( Token.TYPE_PREPOSISI ) || 
						latestProcessedPhrase.getType().equals( Token.TYPE_FRASA_PREPOSISIONAL ) || 
						latestProcessedPhrase.getType().equals( Token.TYPE_ADJEKTIVA ) ||
						latestProcessedPhrase.getType().equals( Token.TYPE_ADVERBIA ) ) {
					
//					justUpdate = true;
					temporaryPhraseType = Token.TYPE_FRASA_PREPOSISIONAL;
					
				}
				
				break;
			// currentToken
			case Token.TYPE_NOMINA:
				
				switch (latestProcessedPhrase.getType()) {
				
				/**-----------------------------------------------------------------------*
				 * Pembentukan FV dengan aturan [ PREP + N ] 					  *
				 *------------------------------------------------------------------------*/
				// latestProcessedPhrase
				case Token.TYPE_PREPOSISI:
					// no break here
				case Token.TYPE_FRASA_PREPOSISIONAL:
					
//					justUpdate = true;
					temporaryPhraseType = Token.TYPE_FRASA_PREPOSISIONAL;
					
					break;
					
				/**-----------------------------------------------------------------------*
				 * Pembentukan FN dengan aturan [ N + N ]		    					  *
				 *------------------------------------------------------------------------*/
				// latestProcessedPhrase
				case Token.TYPE_NOMINA:
					// no break here
				case Token.TYPE_FRASA_NOMINAL:
					
//					justUpdate = true;
					temporaryPhraseType = Token.TYPE_FRASA_NOMINAL;
					
					break;
				// latestProcessedPhrase
				case Token.TYPE_NUMERALIA:
					
					if ( currentToken.getToken().matches(NOMINA_PENGGOLONG_PATTERN) ) {
//						justUpdate = true;
						temporaryPhraseType = Token.TYPE_FRASA_NUMERALIA;
					}
					
					break;
				}
				
				break;
			// currentToken
			case Token.TYPE_VERBA:
				
				/**-----------------------------------------------------------------------*
				 * Pembentukan Frasa verbal dengan aturan [ ADJ + V ] 					  *
				 *------------------------------------------------------------------------*/
				if ( latestProcessedPhrase.getType().equals( Token.TYPE_ADVERBIA ) ) {
//					justUpdate = true;
					temporaryPhraseType = Token.TYPE_FRASA_VERBAL;
				}
				
				break;
			// currentToken
			case Token.TYPE_PRONOMINA:
				
				/**-----------------------------------------------------------------------*
				 * Pembentukan FPRON dengan aturan [ PRON + PRON Penunjuk ]				  *
				 *------------------------------------------------------------------------*/
				if ( ( ( latestProcessedPhrase.getType().equals( Token.TYPE_PRONOMINA ) || 
						latestProcessedPhrase.getType().equals( Token.TYPE_FRASA_PRONOMINAL ) ) && 
						currentToken.getToken().matches( PRONOMINA_PENJUNJUK_PATTERN ) ) ||
						previousToken.getToken().matches( PREPOSISI_PENANYA_PATTERN )) {
					
					if ( ( previousToken.getToken().matches("(di|ke|dari)") && 
							currentToken.getToken().equals("apa") ) ) {
						return false;
					}
					
//					justUpdate = true;
					temporaryPhraseType = Token.TYPE_FRASA_PRONOMINAL;
					
				}
				/**-----------------------------------------------------------------------*
				 * Pembentukan FN dengan aturan [ N + PRON Persona + PRON penunjuk ]	  *
				 *------------------------------------------------------------------------*/
				if ( (currentToken.getToken().matches(PRONOMINA_PERSONA_PATTERN) || 
						currentToken.getToken().matches( PRONOMINA_PENJUNJUK_PATTERN )) && 
						(latestProcessedPhrase.getType().equals(Token.TYPE_NOMINA) || 
								latestProcessedPhrase.getType().equals(Token.TYPE_FRASA_NOMINAL)) ){
					
//					justUpdate = true;
					temporaryPhraseType = Token.TYPE_FRASA_NOMINAL;
					
				}
				
				break;
			// currentToken
			case Token.TYPE_ADJEKTIVA:
				
				/**-----------------------------------------------------------------------*
				 * Pembentukan FN dengan aturan [ N + ADJ ]								  *
				 * -----------------------------------------------------------------------*
				 * proses pengecekan nya adalah:										  *
				 * 1. cek apakah latestProcessed tipenya adalah N, jika iya maka langsung *
				 *    tambahkan ADJ saat ini.											  *
				 * 2. Jika tipe latestProcessed = FN, maka cek terlebih dahulu apakah	  *
				 *    token sebelumnya adalah N, jika tidak maka fail!					  *
				 * 3. Jika token sebelumnya adalah "yang", maka tambahkan				  *
				 *------------------------------------------------------------------------*/
				if ( latestProcessedPhrase.getType().equals(Token.TYPE_NOMINA) || 
						( latestProcessedPhrase.getType().equals(Token.TYPE_FRASA_NOMINAL) && 
								previousToken.getToken().equals(Token.TYPE_NOMINA) ) || 
						previousToken.getToken().equals("yang") ){
					
//					justUpdate = true;
					temporaryPhraseType = Token.TYPE_FRASA_NOMINAL;
					
				}
				
				break;
			// currentToken
			case Token.TYPE_ADVERBIA:
				
				if ( currentToken.getToken().equals("saja") && 
						previousToken.getToken().matches(PRONOMINA_PENANYA_PATTERN) ) {
					
//					justUpdate = true;
					temporaryPhraseType = Token.TYPE_FRASA_PRONOMINAL;
					
				}
				
				break;
			// currentToken
			case Token.TYPE_KONJUNGSI:
				
				if ( currentToken.getToken().equals("yang") &&
						previousToken.getTokenType().equals(Token.TYPE_PRONOMINA) ) {
					
//					justUpdate = true;
					temporaryPhraseType = Token.TYPE_FRASA_NOMINAL;
					
				}
				
				break;
			// currentToken
			case Token.TYPE_NUMERALIA:
				
				if ( ( latestProcessedPhrase.getType().equals(Token.TYPE_FRASA_PRONOMINAL) ||
						latestProcessedPhrase.getType().equals(Token.TYPE_PRONOMINA) ) &&
						currentToken.getToken().matches(NUMERALIA_KOLEKTIF_PATTERN) ) {
					
//					justUpdate = true;
					temporaryPhraseType = Token.TYPE_FRASA_PRONOMINAL;
					
				}
				
				break;
			}
			
			/**------------------------------------------------------------------------------------*
			 * Pembentukan FRASA PREPOSISIONAL													   *
			 *-------------------------------------------------------------------------------------*
			 * Aturan:																			   *
			 * 1. [ PREP + N ]																	   *
			 *-------------------------------------------------------------------------------------*/
			/*if ( latestProcessedPhrase.getWordType().equals( Token.TYPE_PREPOSISI ) || 
					latestProcessedPhrase.getWordType().equals( Token.TYPE_FRASA_PREPOSISIONAL ) ) {
				
				justUpdate = true;
				temporaryPhraseType = Token.TYPE_FRASA_PREPOSISIONAL;
				
			}*/
			
			/**------------------------------------------------------------------------------------*
			 * Pembentukan FRASA PRONOMINAL														   *
			 * ------------------------------------------------------------------------------------*
			 * Aturan:																			   *
			 * 1. [ Pron + Num Kolektif ] 														   *
			 * 2. [ Pron + Pron Penjunjuk ]														   *
			 * 3. [ Pron + <sendiri> ]															   *
			 *-------------------------------------------------------------------------------------*/
			/*if ( ( ( latestProcessedPhrase.getWordType().equals( Token.TYPE_PRONOMINA ) || 
					previousToken.getWordType().equals( Token.TYPE_PRONOMINA ) ) && 
					currentToken.getWord().matches( NUMERALIA_KOLEKTIF_PATTERN ) ) || 
					latestProcessedPhrase.getWordType().matches( PREPOSISI_PENANYA_PATTERN ) ) {
				
				// Jika pola kalimat tanya di + apa maka tolak!
				if ( latestProcessedPhrase.getWord().matches("(di|ke|dari)") && currentToken.getWord().equals("apa") ) {
					return false;
				}
				justUpdate = true;
				temporaryPhraseType = Token.TYPE_FRASA_PRONOMINAL;
				
			}*/
			
			/**------------------------------------------------------------------------------------*
			 * Pembentukan FRASA NOMINAL														   *
			 * ------------------------------------------------------------------------------------*
			 * Aturan:																			   *
			 * 1. [ N + N* + Pron Persona + Pron Penunjuk ]										   *
			 * 2. [ N + Adj + Pron Persona + Pron Penunjuk ]									   *
			 * 3. [ N + Pron Persona + <yang> + Adj + Pron Penunjuk ]							   *
			 *-------------------------------------------------------------------------------------*/
			/*if ( latestProcessedPhrase.getWordType().equals( Token.TYPE_NOMINA ) || 
					latestProcessedPhrase.getWordType().equals( Token.TYPE_FRASA_NOMINAL ) ) {
				
				if ( previousToken.getWordType().equals( Token.TYPE_NOMINA ) && 
						( currentToken.getWordType().equals( Token.TYPE_NOMINA ) || 
								currentToken.getWordType().equals( Token.TYPE_ADJEKTIVA ) || 
								currentToken.getWord().matches( PRONOMINA_PERSONA_PATTERN ) ) ) {
					
					justUpdate = true;
					temporaryPhraseType = Token.TYPE_FRASA_NOMINAL;
					
				}
				
			}*/
			
			/**------------------------------------------------------------------------------------*
			 * Pembentukn FRASA NUMERALIA														   *
			 * ------------------------------------------------------------------------------------*
			 * Aturan:																			   *
			 * 1. [ Num + N Penggolong ]														   *
			 *-------------------------------------------------------------------------------------*/
			/*if ( ( latestProcessedPhrase.getWordType().equals( Token.TYPE_NUMERALIA ) || 
					latestProcessedPhrase.getWordType().equals( Token.TYPE_FRASA_NUMERALIA ) ) && 
					currentToken.getWord().matches( NOMINA_PENGGOLONG_PATTERN ) ) {
				
				justUpdate = true;
				temporaryPhraseType = Token.TYPE_FRASA_NUMERALIA;
				
			}*/
			
			/**
			 * Jika currentToken = "yang", maka perlu pengecekan lebih kompleks.
			 * Treatment khusus untuk kata "yang"
			 * 1. Frasa akan menjadi FN jika "yang" hadir diantara FN dan Adj -> Anak saya (FN) + yang + pintar(Adj)
			 */
			
			
		}
		
		/**
		 * Jika temporaryPhraseType != null, artinya currentToken 
		 * memenuhi syarat sebagai bagian dari latestProcessedPhrase
		 * 
		 * sehingga tidak perlu membuat objek QuestionModel yang baru
		 * cukup melakukan update terhadap objek yang lama
		 */
		if ( temporaryPhraseType != null ) {
			latestProcessedPhrase.addConstituent(currentToken);
			latestProcessedPhrase.setType(temporaryPhraseType);
			
			this.parseResult.set(latestPhraseIndex, latestProcessedPhrase);	
			
		} else {
			/**
			 * Jika currentToken merupakan token yang pertama kali di proses
			 * atau token yang bersangkutan tidak memenuhi kriteria sebagai 
			 * bagian dari frasa yang ada saat ini, maka buat objek QuestionModel
			 * yang baru dan masukkan tipe currentToken dan objek token 
			 * ke dalam objek QuestionModel
			 */
			QuestionModel m = new QuestionModel(currentToken);
			m.setType(currentToken.getTokenType());
			
			this.parseResult.add( m );
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
		 * Jika jumlah frasa hanya dua, maka bentuk fungsi sintaksis hanya
		 * P-S atau S-P.
		 */
		if ( this.parseResult.size() == 2 ) {
			
			QuestionModel first = this.parseResult.get(0);
			QuestionModel second = this.parseResult.get(1);
			
			if ( first.getType().equals(Token.TYPE_PRONOMINA) || 
					first.getType().equals(Token.TYPE_FRASA_PREPOSISIONAL) ) {
				
				first.setSyntacticFunction(Token.TYPE_PREDIKAT);
				second.setSyntacticFunction(Token.TYPE_SUBJEK);
				
			} else {
				first.setSyntacticFunction(Token.TYPE_SUBJEK);
				second.setSyntacticFunction(Token.TYPE_PREDIKAT);
			}
			
			this.parseResult.set(0, first);
			this.parseResult.set(1, second);
		} else {
			/**
			 * Jika jumlah frasa lebih dari dua, maka langkah untuk menentukan predikat adalah:
			 * 1. cek tipe masing-masing frasa, jika ada FV atau FAdj maka itulah Predikat.
			 * 2. Jika tidak ada, maka cek 
			 */
			
			
		}
		
		return false;
	}
	
}
