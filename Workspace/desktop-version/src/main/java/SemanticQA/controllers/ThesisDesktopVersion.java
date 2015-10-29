package SemanticQA.controllers;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import SemanticQA.models.nlp.Parser;
import SemanticQA.models.nlp.QATokenModel;
import SemanticQA.models.nlp.Tokenizer;

class ThesisDesktopVersion {
	
//	private static OntologyMapper ontoMapper;
	
	public static void main(String args[]){
		
//		ontoMapper = new OntologyMapper();
		
		System.out.println("Masukkan pertanyaan: ");
		
		Scanner s = new Scanner(System.in);
		String pertanyaan = s.nextLine();
		s.close();
		Tokenizer t = new Tokenizer();
		
		List<QATokenModel> token = t.tokenize(pertanyaan);
		
		cetak(token);
		
		Parser p = new Parser();
		Map<String, List<QATokenModel>> result = p.parse(token);
		
		cetak(result.get(Parser.PARSE_RESULT));
		
	}
	
	public static void cetak(List<QATokenModel> token){
		for(QATokenModel t: token)
		{
			System.out.print("[" + t.getWordType() + "(" + t.getSyntacticFunction() + ") = " + t.getWord() + "]");
		}
		System.out.println("");
		System.out.println("--------------------");
	}
	public static void cetak(QATokenModel token){
		System.out.println(token.getWord() + " - " + token.getWordType());
	}
	public static void cetak(String message){
		System.out.println(message);
	}
	
}