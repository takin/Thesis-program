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
		
//		System.out.println("Masukkan pertanyaan: ");
		
//		Scanner s = new Scanner(System.in);
//		String pertanyaan = s.nextLine();
		String pertanyaan = "bupati kabupaten lombok timur siapa";
//		s.close();
		
		Tokenizer t = new Tokenizer();
		
		List<TokenModel> token = t.tokenize(pertanyaan);
		
//		cetak(token);
		
		Parser p = new Parser();
		List<QuestionModel> result = p.parse(token);
		
		cetakKlausa(result);
		
		OntologyMapper mapper = new OntologyMapper(result);
		
		long startTime = System.currentTimeMillis();
		cetakMap(mapper.map());
		
		long endTime = System.currentTimeMillis();
		long executionTime = endTime - startTime;
		
		System.out.println("Mapping is executed in: " + executionTime + " seconds");
	}
	
	public static void cetak(List<TokenModel> token){
		
		System.out.println("--------------------------------------------------------------");
		System.out.println("|                 Hasil Proses Tokenisasi                    |");
		System.out.println("--------------------------------------------------------------");
		System.out.println("[");
		for(TokenModel t: token)
		{
			System.out.println("    {kata:" + t.getToken()  + ", kelas:" + t.getTokenType() + "}");
		}
		System.out.println("]");
		System.out.println("--------------------------------------------------------------");
	}
	
	public static void cetakKlausa(List<QuestionModel> token) {
		
		System.out.println("--------------------------------------------------------------");
		System.out.println("|                    Hasil Proses Parsing                    |");
		System.out.println("--------------------------------------------------------------");
		System.out.println("[");
		
		for(QuestionModel m: token)
		{
			System.out.println("    {kelasFrasa:" + m.getType()  + ", fungsi:"+ m.getSyntacticFunction() + ", konstituen:");
			
			List<TokenModel> tm = m.getConstituents();
			
			if ( tm.size() == 1 ) {
				
				System.out.println("        [{kata:" + tm.get(0).getToken() + ", kelas:" + tm.get(0).getTokenType() + "}]");
				
			} else {
				System.out.println("        [");
				for ( TokenModel t: tm ) {
					if(t.equals(tm.get(tm.size() - 1))) {
						System.out.println("            {kata:" + t.getToken() + ", kelas:" + t.getTokenType() + "}");
					} else {
						System.out.println("            {kata:" + t.getToken() + ", kelas:" + t.getTokenType() + "},");
					}
				}
				System.out.println("        ]");
			}
			
			System.out.println("    },");
		}
		
		System.out.println("]");
		System.out.println("--------------------------------------------------------------");
	}
	
	public static void cetakMap(List<QuestionModel> token){
		
		System.out.println("--------------------------------------------------------------");
		System.out.println("|                    Hasil Proses Mapping                    |");
		System.out.println("--------------------------------------------------------------");
		System.out.println("[");
		
		for(QuestionModel m: token)
		{
			System.out.println("    {kelasFrasa:" + m.getType()  + ", fungsi:"+ m.getSyntacticFunction() + ", konstituen:");
			
			List<TokenModel> tm = m.getConstituents();
			
			System.out.println("        [");
			for ( TokenModel t: tm ) {
				if(t.equals(tm.get(tm.size() - 1))) {
					System.out.println("            {kata:" + t.getToken() + ", kelas:" + t.getTokenType() + ", ontologi:" + t.getTokenOWLType() + "}");
				} else {
					System.out.println("            {kata:" + t.getToken() + ", kelas:" + t.getTokenType() + ", ontologi:" + t.getTokenOWLType() + "},");
				}
			}
			System.out.println("        ]");
			
			System.out.println("    },");
		}
		
		System.out.println("]");
		System.out.println("--------------------------------------------------------------");
	}
	
	public static void cetak(String message){
		System.out.println(message);
	}
	
}