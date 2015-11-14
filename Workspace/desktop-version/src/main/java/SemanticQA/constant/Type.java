/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SemanticQA.constant;

/**
 *
 * @author syamsul
 */
public abstract class Type {
    
	public static class Token {
		public static final String NOMINA = "Nomina";
		public static final String ADJEKTIVA = "Adjektiva";
		public static final String VERBA = "Verba";
		public static final String ADVERBIA = "Adverbia";
		public static final String PREPOSISI = "Preposisi";
		public static final String KONJUNGSI = "Konjungsi";
		public static final String PRONOMINA = "Pronomina";
		public static final String NUMERALIA = "Numeralia";
	}
	
	public static class Phrase {
		public static final String FRASA_NOMINAL = "FN";
		public static final String FRASA_NUMERALIA = "FNUM";
		public static final String FRASA_VERBAL = "FV";
		public static final String FRASA_ADJECTIVAL = "FADJ";
		public static final String FRASA_ADVERBIAL = "FADV";
		public static final String FRASA_PREPOSISIONAL = "FPREP";
		public static final String FRASA_PRONOMINAL = "FPRON";
		
		public static class Function {
			public static final String SUBJEK = "S";
			public static final String PREDIKAT = "P";
			public static final String OBJEK = "O";
			public static final String PELENGKAP = "Pel";
			public static final String KETERANGAN = "Ket";
		}
	}
}
