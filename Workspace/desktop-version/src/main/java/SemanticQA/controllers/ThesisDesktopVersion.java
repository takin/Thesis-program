package SemanticQA.controllers;

import java.util.List;
import java.util.Scanner;

import SemanticQA.models.nlp.MorphologicalAnalyzer;
import SemanticQA.models.nlp.Parser;
import SemanticQA.models.nlp.QuestionModel;
import SemanticQA.models.nlp.TokenModel;
import SemanticQA.models.nlp.Tokenizer;
import SemanticQA.models.ontology.OntologyLoader;
import SemanticQA.models.ontology.OntologyMapper;
import SemanticQA.models.ontology.OntologyQuery;

class ThesisDesktopVersion {
	
	public static void main(String args[]){
		
//		System.out.println("Masukkan pertanyaan: ");
		
//		Scanner s = new Scanner(System.in);
//		String pertanyaan = s.nextLine(); 
		
		String[] pertanyaan = new String[]{
				"siapa yang terpilih menjadi kepala desa danger tahun 2015",
				"siapa nama kepala desa yang baiknya",
				"di mana letak pantai selong belanak",
				"di mana letak pantai tanjung an pantai mawun",
				"siapa bupati kabupaten lombok timur",
				"bupati kabupaten lombok timur siapa",
				"apa saja wisata budaya di lombok",
				"di lombok ada wisata budaya apa saja",
				"siapa yang menjadi kepala desa danger",
				"apa saja destinasi wisata yang ada di lombok tengah",
				"apa saja destinasi wisata lombok tengah"
				};
//		s.close();
		
		Tokenizer t = new Tokenizer();

		List<TokenModel> token = t.tokenize(pertanyaan[1]);
		
		cetak(token);
		
//		Parser p = new Parser();
//		List<QuestionModel> result = p.parse(token);
		
//		cetakKlausa(result);
		
//		OntologyMapper mapper = new OntologyMapper(result);
//		List<QuestionModel> mapResult = mapper.map();
		
//		long startTime = System.currentTimeMillis();
//		cetakMap(mapResult);
		
//		long endTime = System.currentTimeMillis();
//		long executionTime = endTime - startTime;
		
//		System.out.println("Mapping is executed in: " + executionTime + " seconds");
		
//		OntologyQuery q = new OntologyQuery(mapper);
//		q.execute(mapResult);
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