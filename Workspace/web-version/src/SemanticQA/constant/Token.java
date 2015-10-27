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
public abstract class Token {
    
	public static final String TYPE_NOMINA = "N";
	public static final String TYPE_ADJEKTIVA = "ADJ";
	public static final String TYPE_VERBA = "V";
	public static final String TYPE_ADVERBIA = "ADV";
	public static final String TYPE_PREPOSISI = "PREP";
	public static final String TYPE_KONJUNGSI = "KONJ";
	public static final String TYPE_PRONOMINA = "PRON";
	public static final String TYPE_NUMERALIA = "NUM";
	public static final String TYPE_FRASA_NOMINAL = "FN";
	public static final String TYPE_FRASA_VERBAL = "FV";
	public static final String TYPE_FRASA_ADJECTIVAL = "FADJ";
	public static final String TYPE_FRASA_ADVERBIAL = "FADV";
	public static final String TYPE_FRASA_PREPOSISIONAL = "FPREP";
	public static final String TYPE_FRASA_PRONOMINAL = "FPRON";
	
    public static final String KEY_TOKEN_WORD = "word";
    public static final String KEY_TOKEN_WORD_TYPE = "wordType";
    public static final String KEY_TOKEN_SEMANTIC_TYPE = "semanticType";
    public static final String KEY_TOKEN_WORD_ROLE = "wordRole";
    public static final String KEY_TOKEN_WORD_FUNCTION = "wordFunction";
    public static final String KEY_TOKEN_OBJECT_RESTRICTION = "objectRestriction";
}
