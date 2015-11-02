package SemanticQA.controllers;

import java.util.List;
import java.util.Scanner;

import SemanticQA.models.nlp.Parser;
import SemanticQA.models.nlp.QuestionModel;
import SemanticQA.models.nlp.TokenModel;
import SemanticQA.models.nlp.Tokenizer;
import SemanticQA.models.ontology.OntologyMapper;

class ThesisDesktopVersion {
	
	public static void main(String args[]){
		
		System.out.println("Masukkan pertanyaan: ");
		
		Scanner s = new Scanner(System.in);
		String pertanyaan = s.nextLine();
		s.close();
		
		Tokenizer t = new Tokenizer();
		
		List<TokenModel> token = t.tokenize(pertanyaan);
		
		cetak(token);
		
		Parser p = new Parser();
		List<QuestionModel> result = p.parse(token);
		
		cetakFrasa(result);
		
		OntologyMapper mapper = new OntologyMapper(result);
		cetakFrasa(mapper.map());
		
	}
	
	public static void cetak(List<TokenModel> token){
		for(TokenModel t: token)
		{
			System.out.print("[" + t.getType()  + " = " + t.getWord() + "]");
		}
		System.out.println("");
		System.out.println("--------------------");
	}
	
	public static void cetakFrasa(List<QuestionModel> token){
		
		System.out.println("[");
		
		for(QuestionModel m: token)
		{
			System.out.println("{type:" + m.getType()  + ", func:"+ m.getSyntacticFunction() + ", constituent:[");
			
			List<TokenModel> tm = m.getPhrases();
			
			for ( TokenModel t: tm ) {
				if(t.equals(tm.get(tm.size() - 1))) {
					System.out.println("{type:" + t.getType() + ", word:" + t.getWord() + ", OWLType: "+ t.getOWLType() +", restriction:" + t.getRestriction() + "}");
				} else {
					System.out.println("{type:" + t.getType() + ", word:" + t.getWord() + ", OWLType: "+ t.getOWLType() +", restriction:" + t.getRestriction() + "}, ");
				}
			}
			
			if (tm.size() > 1) {
				System.out.println(" ]},");
			} else {
				System.out.print(" ]},");
			}
		}
		
		System.out.println("]");
		System.out.println("--------------------");
	}
	
	public static void cetak(TokenModel token){
		System.out.println(token.getWord() + " - " + token.getType());
	}
	public static void cetak(String message){
		System.out.println(message);
	}
	
}