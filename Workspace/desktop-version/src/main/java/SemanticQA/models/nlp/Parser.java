package SemanticQA.models.nlp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import SemanticQA.constant.Token;
import SemanticQA.models.ontology.OntologyMapper;

public class Parser {
	
	private OntologyMapper ontoMapper;
	private List<Map<String,String>> syntaxFunction; 

	public static interface ParserListener{
		public void onParseSuccess(TreeMap<String, String> parseTree);
	}
	
	public Parser(OntologyMapper mapper) {
		ontoMapper = mapper;
		syntaxFunction = new ArrayList<>();
	}
	
	public void parse(List<Map<String,String>> taggedToken, ParserListener listener){
		analyzeSyntacticFunction(taggedToken);
	}
	
	private boolean analyzeSyntacticFunction(List<Map<String,String>> taggedToken){
		
		Map<String,String> currentToken = taggedToken.get(0);
		
		/**
		 * Cek token apakah sudah memiliki tipe semantik atau belum
		 * jiak sudah, maka token yang bersangkutan tidak perlu untuk dilakukan
		 * proses pembentukan frasa, karena sudah memiliki makna di dalam ontologi
		 * 
		 * Jika belum maka lakukan proses pembentukan frasa dengan metode bottom-up
		 */
		if(currentToken.get(Token.KEY_TOKEN_SEMANTIC_TYPE) != null){
			
			syntaxFunction.add(currentToken);
			taggedToken.remove(currentToken);
			
			if(taggedToken.size() > 0){
				analyzeSyntacticFunction(taggedToken);
			}
			return true;
		}
		
//		makePhrase(reference, lists)
		
		return false;
		
	}
	
	private boolean analyzeSemanticRole(){
		return false;
	}
	
	private Map<String,String> makePhrase(Map<String,String> reference, List<Map<String,String>> lists){
		
		for(Map<String,String> token: lists){
			switch (reference.get(Token.KEY_TOKEN_WORD_TYPE)) {
			case "N":
				
				break;
			}
		}
		
		return reference;
	}
	
}
