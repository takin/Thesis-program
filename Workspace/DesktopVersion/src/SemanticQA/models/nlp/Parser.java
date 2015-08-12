package SemanticQA.models.nlp;

import java.util.List;
import java.util.TreeMap;

public class Parser {

	public static interface ParserListener{
		public void onParseSuccess(TreeMap<String, String> parseTree);
	}
	
	public void parse(List<String> taggedToken, ParserListener listener){
		
	}
}
