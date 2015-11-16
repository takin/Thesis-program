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
		public static final String NOMINAL = "FN";
		public static final String NUMERALIA = "FNUM";
		public static final String VERBAL = "FV";
		public static final String ADJECTIVAL = "FADJ";
		public static final String ADVERBIAL = "FADV";
		public static final String PREPOSISIONAL = "FPREP";
		public static final String PRONOMINAL = "FPRON";
		
		public static class Function {
			public static final String SUBJEK = "Subjek";
			public static final String PREDIKAT = "Predikat";
			public static final String OBJEK = "Objek";
			public static final String PELENGKAP = "Pelengkap";
			public static final String KETERANGAN = "Keterangan";
		}
	}
	
	public static class Ontology {
		public static final String CLASS = "OWLClass";
		public static final String OBJECT_PROPERTY = "OWLObjectPropery";
		public static final String DATATYPE_PROPERTY = "OWLDataProperty";
		public static final String INDIVIDUAL = "OWLNamedIndividual";
	}
}
