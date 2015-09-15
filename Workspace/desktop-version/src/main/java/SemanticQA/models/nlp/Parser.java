package SemanticQA.models.nlp;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import SemanticQA.helpers.QATokenModel;
import SemanticQA.models.ontology.OntologyMapper;

public class Parser {
	
	private OntologyMapper ontoMapper;
	private List<QATokenModel> syntaxFunction; 

	public static interface ParserListener{
		public void onParseSuccess(TreeMap<String, String> parseTree);
	}
	
	public Parser(OntologyMapper mapper) {
		ontoMapper = mapper;
		syntaxFunction = new ArrayList<>();
	}
	
	public void parse(List<QATokenModel> taggedToken, ParserListener listener){
		
	}
	
}
