package SemanticQA.module.nlp;

import java.util.ArrayList;
import java.util.List;

import SemanticQA.constant.Type;
import SemanticQA.model.SemanticToken;
import SemanticQA.model.Sentence;

public class Parser {

	private static final String PRONOMINA_PERSONA_PATTERN = "(saya|aku|anda|kau|dia|ia|beliau|kami|kita)";
	private static final String NUMERALIA_KOLEKTIF_PATTERN = "(berdua|sekalian|semua|ini|itu|sendiri)";
	private static final String NOMINA_PENGGOLONG_PATTERN = "(buah|ekor)";
	private static final String PREPOSISI_PENANYA_PATTERN = "(di|ke|dari|bagai)";
	private static final String PRONOMINA_PENJUNJUK_PATTERN = "(ini|itu)";
	private static final String KONJUNGTOR_KLAUSA_PATTERN = "(dan|atau)";
	private static final String PRONOMINA_PENANYA_PATTERN = "(apa|siapa|kapan)";
	
	public List<Sentence> parse(List<SemanticToken> taggedToken){
		
		List<Sentence> phrases = createPhrase(null, taggedToken, new ArrayList<Sentence>());
		List<Sentence> clauses = createClause(null, phrases, new ArrayList<Sentence>());
		List<Sentence> result = analyzeSyntacticFunction(clauses);
		return result;
	}
	
	/**
	 * Method untuk membentuk frasa dari token-token kata.
	 * Proses pembentukan frasa dilakukan secara rekursi hingga rray list data habis
	 * 
	 * @param Type yang diproses sebelumnya
	 * @param Sisa Array List token yang akan diproses
	 * @param Arraylist frasa yang sudah berhasil dibentuk
	 * @return
	 */
	private List<Sentence> createPhrase(SemanticToken previousToken, List<SemanticToken> data, List<Sentence> result){
		
		/**
		 * Temporary variable untuk menyimpan tipe frasa yang baru.
		 * Dalam proses pengecekan token ada kemungkinan terjadinya perubahan tipe frasa
		 * yang disebabkan oleh tipe kata yang sedang di proses saat ini.
		 */
		String tempPhraseType = null;
		
		/**
		 * Ambil nilai index terakhir dari array list parseResult.
		 * Index ini dibutuhkan untuk melakukan update terhadap objek frasa yang sedang di proses
		 */
		int currentPhraseIndex = result.isEmpty() ? -1 : result.size() - 1;
		
		/**
		 * Ambil objek dari array list token untuk di proses.
		 * Pengambilan dilakukan dengan cara memotong langsung current object segingga tidak perlu dilakukan 
		 * pemotongan pada saat akan dilakukan proses rekursi di akhir.
		 */
		SemanticToken currentToken = data.remove(0);
		
		/**
		 * Ambil objek frasa yang terkahir sehingga memudahkan proses pembandingan dengan 
		 * objek token yang sedang di proses.
		 */
		Sentence currentPhrase = currentPhraseIndex == -1 ? new Sentence() : result.get(currentPhraseIndex);
		
		if ( previousToken != null ) {
			switch (currentToken.getType()) {
			case Type.Token.ADVERBIA:
				if ( previousToken.getToken().matches(PRONOMINA_PENANYA_PATTERN) ) {
					tempPhraseType = Type.Phrase.PRONOMINAL;
				}

				break;
			case Type.Token.VERBA:
				
				if ( previousToken.getType().equals(Type.Token.NOMINA) ||
						previousToken.getType().equals(Type.Token.KONJUNGSI) ) {
					tempPhraseType = Type.Phrase.VERBAL;
				}
				
				break;
			case Type.Token.NOMINA:
				
				if ( previousToken.getType().equals(Type.Token.NOMINA) ) {
					tempPhraseType = (currentPhrase.getType() == null) ? Type.Phrase.NOMINAL : currentPhrase.getType();
				}
				
				if ( previousToken.getType().equals(Type.Token.PREPOSISI) ) {
					tempPhraseType = Type.Phrase.PREPOSISIONAL;
				}
				
				break;
			case Type.Token.PRONOMINA:
				
				if ( previousToken.getType().equals(Type.Token.PREPOSISI) ) {
					tempPhraseType = Type.Phrase.PRONOMINAL;
				}
				
				break;
			}
		}
		
		if ( tempPhraseType != null ) {
			
			currentPhrase.setType(tempPhraseType);
			currentPhrase.putConstituent(currentToken);
			result.set(currentPhraseIndex, currentPhrase);
			
		}
		
		if ( previousToken == null ) {

			currentPhrase.putConstituent(currentToken);
			result.add(currentPhrase);
			
		}
		
		if ( tempPhraseType == null && previousToken != null ) {
			Sentence newPhrase = new Sentence();
			newPhrase.putConstituent(currentToken);
			result.add(newPhrase);
		}
		
		if ( data.size() > 0 ) {
			createPhrase(currentToken, data, result);
		}
		
		return result;
		/**
		 * jika sudah ada token yang di proses sebelumnya
		 * maka lakukan proses pembentukan frasa dengan mempertimbangkan token yang di proses sebelumnya
		 *
		if ( previousToken != null && !currentToken.getToken().matches(KONJUNGTOR_KLAUSA_PATTERN) ) {
		
			switch (currentToken.getTokenType()) {
			
			// currentToken
			case Type.TYPE_PREPOSISI:
				
				if ( previousToken.getTokenType().equals( Type.TYPE_PREPOSISI ) ||  
						previousToken.getTokenType().equals( Type.TYPE_ADJEKTIVA ) ||
						previousToken.getTokenType().equals( Type.TYPE_ADVERBIA ) ) {
					
					temporaryPhraseType = Type.TYPE_FRASA_PREPOSISIONAL;
					
				}
				
				break;
			// currentToken
			case Type.TYPE_NOMINA:
				
				switch (previousToken.getTokenType()) {
				
				/**-----------------------------------------------------------------------*
				 * Pembentukan FV dengan aturan [ PREP + N ] 					  *
				 *------------------------------------------------------------------------*
				// latestProcessedPhrase
				case Type.TYPE_PREPOSISI:
					
					temporaryPhraseType = Type.TYPE_FRASA_PREPOSISIONAL;
					
					break;
					
				/**-----------------------------------------------------------------------*
				 * Pembentukan FN dengan aturan [ N + N ]		    					  *
				 *------------------------------------------------------------------------*
				// latestProcessedPhrase
				case Type.TYPE_NOMINA:
					
					temporaryPhraseType = Type.TYPE_FRASA_NOMINAL;
					
					break;
				// latestProcessedPhrase
				case Type.TYPE_NUMERALIA:
					
					if ( currentToken.getToken().matches(NOMINA_PENGGOLONG_PATTERN) ) {
						temporaryPhraseType = Type.TYPE_FRASA_NUMERALIA;
					}
					
					break;
				// latestProcessedPhrase
				case Type.TYPE_VERBA:
					temporaryPhraseType = Type.TYPE_FRASA_VERBAL;
					break;
				}
				
				break;
			// currentToken
			case Type.TYPE_VERBA:
				
				/**-----------------------------------------------------------------------*
				 * Pembentukan Frasa verbal dengan aturan [ ADJ + V ] 					  *
				 *------------------------------------------------------------------------*
				if ( previousToken.getTokenType().equals( Type.TYPE_ADVERBIA ) ||
						previousToken.getTokenType().equals( Type.TYPE_VERBA ) ||
						previousToken.getTokenType().equals(Type.TYPE_NOMINA) || 
						previousToken.getToken().equals("yang") ) {
					temporaryPhraseType = Type.TYPE_FRASA_VERBAL;
				}
				
				break;
			// currentToken
			case Type.TYPE_PRONOMINA:
				
				/**-----------------------------------------------------------------------*
				 * Pembentukan FPRON dengan aturan [ PRON + PRON Penunjuk ]				  *
				 *------------------------------------------------------------------------*
				if ( ( ( previousToken.getTokenType().equals( Type.TYPE_PRONOMINA ) ) && 
						currentToken.getToken().matches( PRONOMINA_PENJUNJUK_PATTERN ) ) ||
						previousToken.getToken().matches( PREPOSISI_PENANYA_PATTERN )) {
					
					if ( ( previousToken.getToken().matches("(di|ke|dari)") && 
							currentToken.getToken().equals("apa") ) ) {
						break;
					}
					
					temporaryPhraseType = Type.TYPE_FRASA_PRONOMINAL;
					
				}
				/**-----------------------------------------------------------------------*
				 * Pembentukan FN dengan aturan [ N + PRON Persona + PRON penunjuk ]	  *
				 *------------------------------------------------------------------------*
				if ( (currentToken.getToken().matches(PRONOMINA_PERSONA_PATTERN) || 
						currentToken.getToken().matches( PRONOMINA_PENJUNJUK_PATTERN )) && 
						(previousToken.getTokenType().equals(Type.TYPE_NOMINA)) ){
					
					temporaryPhraseType = Type.TYPE_FRASA_NOMINAL;
					
				}
				
				break;
			// currentToken
			case Type.TYPE_ADJEKTIVA:
				
				/**-----------------------------------------------------------------------*
				 * Pembentukan FN dengan aturan [ N + ADJ ]								  *
				 * -----------------------------------------------------------------------*
				 * proses pengecekan nya adalah:										  *
				 * 1. cek apakah latestProcessed tipenya adalah N, jika iya maka langsung *
				 *    tambahkan ADJ saat ini.											  *
				 * 2. Jika tipe latestProcessed = FN, maka cek terlebih dahulu apakah	  *
				 *    token sebelumnya adalah N, jika tidak maka fail!					  *
				 * 3. Jika token sebelumnya adalah "yang", maka tambahkan				  *
				 *------------------------------------------------------------------------*
				if ( previousToken.getTokenType().equals(Type.TYPE_NOMINA) && 
								(previousToken.getToken().equals(Type.TYPE_NOMINA) || 
						previousToken.getToken().equals("yang") ) ){
					
					temporaryPhraseType = Type.TYPE_FRASA_NOMINAL;
					
				}
				
				break;
			// currentToken
			case Type.TYPE_ADVERBIA:
				
				if ( currentToken.getToken().equals("saja") && 
						previousToken.getToken().matches(PRONOMINA_PENANYA_PATTERN) ) {
					temporaryPhraseType = Type.TYPE_FRASA_PRONOMINAL;
				}
				
				break;
			// currentToken
			case Type.TYPE_KONJUNGSI:
				
				if ( currentToken.getToken().equals("yang") ) {
					if ( previousToken.getTokenType().equals(Type.TYPE_PRONOMINA) ) { 
						temporaryPhraseType = Type.TYPE_FRASA_NOMINAL;
					}
				}
				
				break;
			// currentToken
			case Type.TYPE_NUMERALIA:
				
				if ( ( previousToken.getTokenType().equals(Type.TYPE_PRONOMINA) ) &&
						currentToken.getToken().matches(NUMERALIA_KOLEKTIF_PATTERN) ) {
					
					temporaryPhraseType = Type.TYPE_FRASA_PRONOMINAL;
					
				}
				
				break;
			}
		}
		
		/**
		 * Jika temporaryPhraseType != null, artinya currentToken 
		 * memenuhi syarat sebagai bagian dari latestProcessedPhrase
		 * 
		 * sehingga tidak perlu membuat objek QuestionModel yang baru
		 * cukup melakukan update terhadap objek yang lama
		 *
		if ( temporaryPhraseType != null ) {
			res.addConstituent(currentToken);
			latestProcessedPhrase.setType(temporaryPhraseType);
			
			result.set(latestPhraseIndex, latestProcessedPhrase);	
			
		} else {
			/**
			 * Jika currentToken merupakan token yang pertama kali di proses
			 * atau token yang bersangkutan tidak memenuhi kriteria sebagai 
			 * bagian dari frasa yang ada saat ini, maka buat objek QuestionModel
			 * yang baru dan masukkan tipe currentToken dan objek token 
			 * ke dalam objek QuestionModel
			 *
			SentenceModel m = new SentenceModel(currentToken);
			m.setType(currentToken.getTokenType());
			
			result.add( m );
		}
		
		// lakukan proses secara rekursi hingga data habis
		if ( data.size() > 0 ) {
			createPhrase( currentToken, data, result );
		}
		return result;
		*/
	}

	private List<Sentence> createClause(Sentence prevPhrase, List<Sentence> models, List<Sentence> result) {
		
		Sentence currentPhraseToProcess = models.remove(0);
		Sentence currentPhraseResult = result.size() > 0 ? result.get(result.size() - 1) : null;
		
		String tempPhraseType = null;
		
		if ( prevPhrase != null ) {
			switch (currentPhraseToProcess.getType()) {
			case Type.Phrase.VERBAL:
				
				if ( prevPhrase.getType().equals(Type.Phrase.VERBAL) ) {
					
					///////////
					// cek apakah konstituen pertama dari currentPhrase adalah verbal
					// jika tidak maka tidak boleh digabungkan
					//////////
					SemanticToken firstConstituent = currentPhraseToProcess.getContituent(0);
					if (firstConstituent.getType().equals(Type.Token.VERBA)) {
						tempPhraseType = Type.Phrase.VERBAL;
					}
					
				}
				
				break;
			case Type.Phrase.PREPOSISIONAL:
				
				tempPhraseType = Type.Phrase.VERBAL;
				
				break;
			}
		}
		
		if ( tempPhraseType != null ) {
			currentPhraseResult.setType(tempPhraseType);
			currentPhraseResult.putConstituents(currentPhraseToProcess.getConstituents());
		}
		
		if ( prevPhrase == null || tempPhraseType == null ) {
			result.add(currentPhraseToProcess);
		}
		
		if ( models.size() > 0 ) {
			createClause(currentPhraseToProcess, models, result);
		}
		
		return result;
	}
	
	/**
	 * Method untuk menganalisa fungsi sintaksis dari masing-masing frasa
	 */
	private List<Sentence> analyzeSyntacticFunction(List<Sentence> clause){
		
		/**
		 * Jika jumlah frasa hanya dua, maka bentuk fungsi sintaksis hanya
		 * P-S atau S-P.
		 */
		if ( clause.size() == 2 ) {
			
			Sentence first = clause.get(0);
			Sentence second = clause.get(1);
			
			if ( first.getType().equals(Type.Token.PRONOMINA) || 
					first.getType().equals(Type.Phrase.PRONOMINAL) ) {
				
				first.setFunction(Type.Phrase.Function.PREDIKAT);
				second.setFunction(Type.Phrase.Function.SUBJEK);
				
			} else {
				first.setFunction(Type.Phrase.Function.SUBJEK);
				second.setFunction(Type.Phrase.Function.PREDIKAT);
			}
			
			clause.set(0, first);
			clause.set(1, second);
		} else {
			/**
			 * Jika jumlah frasa lebih dari dua, maka langkah untuk menentukan predikat adalah:
			 * 1. cek tipe masing-masing frasa, jika ada FV atau FAdj maka itulah Predikat.
			 * 2. Jika tidak ada, maka cek 
			 */
			
			
		}
		
		return clause;
	}
}
