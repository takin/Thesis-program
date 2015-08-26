package SemanticQA.models.nlp;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Parser {

	public static interface ParserListener{
		public void onParseSuccess(TreeMap<String, String> parseTree);
	}
	
	/**
	 * Proses penentuan fungsi sintaksis dari kalimat yang sudah diberi 
	 * tag kelas kata
	 * @param taggedToken array list tagged sentence
	 * @return array list fungsi sintaksis <kata/frasa;fungsi> foo bar;S baz;P
	 */
	private List<String> analyzeSyntacticFunction(List<Map<String,String>> taggedToken){
		 
		/**
		 * Algortima yang digunakan adalah bottom-up 
		 * urutan kata diambil mulai dari konstituen terkecil sebelah kri
		 */
		
		
		return null;
	}
	
	public void parse(List<Map<String,String>> taggedToken, ParserListener listener){
		System.out.println(taggedToken);
		
		analyzeSyntacticFunction(taggedToken);
		
	}
}
