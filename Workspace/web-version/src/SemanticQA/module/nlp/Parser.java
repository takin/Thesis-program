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
	private static final String NOMINA_KETERANGAN = "(tahun|bulan|hari)";
	
	public List<Sentence> parse(List<SemanticToken> taggedToken){
		
		List<Sentence> phrases = createPhrase(null, taggedToken, new ArrayList<Sentence>());
					   phrases = analyzeSyntacticFunction(phrases);
		return phrases;
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
		
		//////////////////////////////////////////////////////////////////////////////////////
		// Temporary variable untuk menyimpan tipe frasa yang baru.							//
		// Dalam proses pengecekan token ada kemungkinan terjadinya perubahan tipe frasa	//
		// yang disebabkan oleh tipe kata yang sedang di proses saat ini.					//
		//////////////////////////////////////////////////////////////////////////////////////
		String tempPhraseType = null;
		
		//////////////////////////////////////////////////////////////////////////////////////
		// Ambil nilai index terakhir dari array list parseResult. Index ini dibutuhkan 	//
		// untuk melakukan update terhadap objek frasa yang sedang di proses				//
		//////////////////////////////////////////////////////////////////////////////////////
		int currentPhraseIndex = result.isEmpty() ? -1 : result.size() - 1;
		
		//////////////////////////////////////////////////////////////////////////////////////
		// Ambil objek dari array list token untuk di proses. Pengambilan dilakukan 		//
		// dengan cara memotong langsung current object segingga tidak perlu dilakukan 		//
		// pemotongan pada saat akan dilakukan proses rekursi di akhir.						//
		//////////////////////////////////////////////////////////////////////////////////////
		SemanticToken currentToken = data.remove(0);
		
		//////////////////////////////////////////////////////////////////////////////////////
		// Ambil objek frasa yang terkahir sehingga memudahkan proses pembandingan dengan 	//
		// objek token yang sedang di proses.												//
		//////////////////////////////////////////////////////////////////////////////////////
		Sentence currentPhrase = currentPhraseIndex == -1 ? new Sentence() : result.get(currentPhraseIndex);
		
		if ( previousToken != null ) {
			switch (currentToken.getType()) {
			case Type.Token.ADVERBIA:
				if ( previousToken.getToken().matches(PRONOMINA_PENANYA_PATTERN) ) {
					tempPhraseType = Type.Phrase.PRONOMINAL;
				}

				break;
			case Type.Token.VERBA:
				
				if ( (previousToken.getType().equals(Type.Token.NOMINA) && 
						(currentPhrase.getType().equals(Type.Phrase.NOMINAL) ||
								currentPhrase.getType().equals(Type.Token.NOMINA) ) ) ||
						previousToken.getType().equals(Type.Token.KONJUNGSI) || 
						previousToken.getType().equals(Type.Token.VERBA) ) {
					
					tempPhraseType = Type.Phrase.VERBAL;
				}
				
				break;
			case Type.Token.NOMINA:
				
				if ( !currentToken.getToken().matches(NOMINA_KETERANGAN) ) {
					
					if ( previousToken.getType().equals(Type.Token.NOMINA) ) {
						
						if ( currentPhrase.getType().equals(Type.Phrase.PREPOSISIONAL) || 
								currentPhrase.getType().equals(Type.Token.PREPOSISI) ) {
							tempPhraseType = Type.Phrase.PREPOSISIONAL;
						} else {
							tempPhraseType = Type.Phrase.NOMINAL;
						}
					}
					
					if ( previousToken.getType().equals(Type.Token.PREPOSISI) ) {
						
						if ( currentPhrase.getType().equals(Type.Phrase.VERBAL) ) {
							tempPhraseType = Type.Phrase.VERBAL;
						} else {
							tempPhraseType = Type.Phrase.PREPOSISIONAL;
						}
					}
					
					if ( previousToken.getType().equals(Type.Token.VERBA) ) {
						
						if ( currentPhrase.getType().equals(Type.Phrase.VERBAL) || 
								currentPhrase.getType().equals(Type.Token.VERBA)) {
							if ( !currentPhrase.getContituent(0).getType().equals(Type.Token.KONJUNGSI) ) {
								tempPhraseType = Type.Phrase.VERBAL;
							}
						}
					}
				}
				
				break;
			case Type.Token.PRONOMINA:
				
				if ( previousToken.getType().matches("(Preposisi|Pronomina)") ) {
					tempPhraseType = Type.Phrase.PRONOMINAL;
				}
				
				break;
			case Type.Token.NUMERALIA:
				
				if ( previousToken.getToken().matches(NOMINA_KETERANGAN) ||
						previousToken.getType().equals(Type.Token.NUMERALIA) ){
					tempPhraseType = Type.Phrase.NUMERALIA;
				}
				
				break;
			case Type.Token.PREPOSISI:
				
				if ( previousToken.getType().equals(Type.Token.VERBA) ) {
					if ( currentPhrase.getType().equals(Type.Phrase.VERBAL) || 
							currentPhrase.getType().equals(Type.Token.VERBA) ) {
						
						if ( currentPhrase.getContituent(0).getType().equals(Type.Token.KONJUNGSI) ) {
							tempPhraseType = Type.Phrase.VERBAL;
						}
						
					}
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
			currentPhrase.setType(currentToken.getType());
			currentPhrase.putConstituent(currentToken);
			result.add(currentPhrase);
			
		}
		
		if ( tempPhraseType == null && previousToken != null ) {
			Sentence newPhrase = new Sentence();
			newPhrase.putConstituent(currentToken);
			newPhrase.setType(currentToken.getType());
			result.add(newPhrase);
		}
		
		if ( data.size() > 0 ) {
			createPhrase(currentToken, data, result);
		}
		
		return result;
	}
	
	/**
	 * Method untuk menganalisa fungsi sintaksis dari masing-masing frasa
	 */
	private List<Sentence> analyzeSyntacticFunction(List<Sentence> clause){
		
		int predicatePosition = getPredicatePosition(clause);
		
		//////////////////////////////////////////////////////////////////////////
		// Jika jumlah frasa hanya dua, maka bentuk fungsi sintaksis hanya		//
		// P-S atau S-P.														//
		//////////////////////////////////////////////////////////////////////////
		if ( clause.size() == 2 ) {
			if ( predicatePosition == 0 ) {
				clause.get(0).setFunction(Type.Phrase.Function.PREDIKAT);
				clause.get(1).setFunction(Type.Phrase.Function.SUBJEK);
			} else {
				clause.get(0).setFunction(Type.Phrase.Function.SUBJEK);
				clause.get(1).setFunction(Type.Phrase.Function.PREDIKAT);
			}
		}
		
		if ( clause.size() > 2 ) {
			
			clause.get(predicatePosition).setFunction(Type.Phrase.Function.PREDIKAT);
			
			///////
			// jika posisi predikat bukan di urutan pertama dan terahir (ada di tengah2 kalimat)
			/////
			if ( (predicatePosition > 0) && (predicatePosition < (clause.size() - 1))) {
				
				
				
				if ( clause.get(predicatePosition).getType().matches("(F?V)")) {
					/////
					// tentukan fungsi frasa yang ada di sebelah kiri predikat
					///////
					switch (clause.get(predicatePosition - 1).getType()) {
					case Type.Token.PREPOSISI:
					case Type.Phrase.PREPOSISIONAL:
						clause.get(predicatePosition - 1).setFunction(Type.Phrase.Function.OBJEK);
						break;
						
					case Type.Token.PRONOMINA:
					case Type.Phrase.PRONOMINAL:
					case Type.Phrase.NOMINAL:
					case Type.Token.NOMINA:
					default:
						clause.get(predicatePosition - 1).setFunction(Type.Phrase.Function.SUBJEK);
						break;
					}
					
					switch (clause.get(predicatePosition + 1).getType()) {
					case Type.Token.NOMINA:
					case Type.Phrase.NOMINAL:
					default:
						if ( clause.get(predicatePosition - 1).getFunction().equals(Type.Phrase.Function.SUBJEK) ) {
							clause.get(predicatePosition + 1).setFunction(Type.Phrase.Function.OBJEK);
						} else {
							clause.get(predicatePosition + 1).setFunction(Type.Phrase.Function.SUBJEK);
						}
						break;
					case Type.Token.NUMERALIA:
					case Type.Phrase.NUMERALIA:
					case Type.Token.PREPOSISI:
					case Type.Phrase.PREPOSISIONAL:
						clause.get(predicatePosition + 1).setFunction(Type.Phrase.Function.KETERANGAN);
						break;
					}
					
					if ( clause.size() > (predicatePosition + 2 ) ) {
						if(clause.get(predicatePosition + 2).getType().matches("(Numeralia|FNUM)")) {
							clause.get(predicatePosition + 2).setFunction(Type.Phrase.Function.KETERANGAN);
						} else {
							clause.get(predicatePosition + 2).setFunction(Type.Phrase.Function.PELENGKAP);
						}
					}
				}
				
			}
		}
		
		return clause;
	}
		
	private int getPredicatePosition(List<Sentence> phrase){
		int position = -1;
		
		if ( phrase.size() == 2 ) {
			position = ( phrase.get(0).getType().equals(Type.Token.PRONOMINA) || 
					phrase.get(0).getType().equals(Type.Phrase.PRONOMINAL) ) ? 0 : 1;
		}
		
		if ( phrase.size() > 2 ) {
			for ( int i = 0; i < phrase.size() - 1; i++ ) {
				Sentence currentPhrase = phrase.get(i);
				if ( currentPhrase.getType().equals(Type.Token.VERBA) ||
						currentPhrase.getType().equals(Type.Phrase.VERBAL) ) {
					position = i;
					break;
				}
			}
			
			// jika tidak ada frasa verbal
			if ( position == -1 ){
				for ( int i = 0; i < phrase.size() - 1; i++ ) {
					
				}
			}
		}
		
		return position == -1 ? 0 : position;
	}
}
