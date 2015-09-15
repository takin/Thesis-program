package SemanticQA.controllers;

import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;

import SemanticQA.helpers.QATokenModel;
import SemanticQA.models.nlp.Parser;
import SemanticQA.models.nlp.Parser.ParserListener;
import SemanticQA.models.nlp.Tokenizer;
import SemanticQA.models.nlp.Tokenizer.TokenizerListener;
import SemanticQA.models.ontology.OntologyMapper;

class ThesisDesktopVersion implements TokenizerListener, ParserListener {
	
	private static OntologyMapper ontoMapper;
	
	public static void main(String args[]){
		
		ontoMapper = new OntologyMapper();
		
		System.out.println("Masukkan pertanyaan: ");
		
		Scanner s = new Scanner(System.in);
		String pertanyaan = s.nextLine();
		s.close();
		Tokenizer t = new Tokenizer();
		
		t.tokenize(pertanyaan, new ThesisDesktopVersion());
		
	}

	@Override
	public void onTokenizeSuccess(List<QATokenModel> taggedToken) {
//		cetak(taggedToken);
		new Parser(ontoMapper).parse(taggedToken, this);
	}

	@Override
	public void onParseSuccess(TreeMap<String, String> parseTree) {
		// TODO Auto-generated method stub
		
	}
	
	public void cetak(List<QATokenModel> token){
		for(QATokenModel t: token){
			System.out.println(t.getWord() + " - " + t.getWordType());
		}
	}
	
}