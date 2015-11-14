package SemanticQA.controllers;

import java.util.List;


import SemanticQA.model.MySQLDatabase;
import SemanticQA.model.SemanticToken;
import SemanticQA.model.Sentence;
import SemanticQA.module.nlp.Parser;
import SemanticQA.module.nlp.Tokenizer;

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
				"apa saja destinasi wisata yang terdapat di lombok tengah"
				};
//		s.close();
		
		Tokenizer t = new Tokenizer(new MySQLDatabase());

		List<SemanticToken> tm = t.tokenize(pertanyaan[2]);
		
		cetak(tm);
		
		Parser p = new Parser();
		List<Sentence> result = p.parse(tm);
		
		cetakKlausa(result);
		
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
	
	public static void cetak(List<SemanticToken> token){
		
		System.out.println("--------------------------------------------------------------");
		System.out.println("|                 Hasil Proses Tokenisasi                    |");
		System.out.println("--------------------------------------------------------------");
		System.out.println("[");
		for(SemanticToken t: token)
		{
			System.out.println("    {kata:" + t.getToken()  + ", kelas:" + t.getType() + "}");
		}
		System.out.println("]");
		System.out.println("--------------------------------------------------------------");
	}
	
	public static void cetakKlausa(List<Sentence> token) {
		
		System.out.println("--------------------------------------------------------------");
		System.out.println("|                    Hasil Proses Parsing                    |");
		System.out.println("--------------------------------------------------------------");
		System.out.println("[");
		
		for(Sentence m: token)
		{
			System.out.println("    {kelasFrasa:" + m.getType()  + ", fungsi:"+ m.getFunction() + ", konstituen:");
			
			List<SemanticToken> tm = m.getConstituents();
			
			if ( tm.size() == 1 ) {
				
				System.out.println("        [{kata:" + tm.get(0).getToken() + ", kelas:" + tm.get(0).getType() + "}]");
				
			} else {
				System.out.println("        [");
				for ( SemanticToken t: tm ) {
					if(t.equals(tm.get(tm.size() - 1))) {
						System.out.println("            {kata:" + t.getToken() + ", kelas:" + t.getType() + "}");
					} else {
						System.out.println("            {kata:" + t.getToken() + ", kelas:" + t.getType() + "},");
					}
				}
				System.out.println("        ]");
			}
			
			System.out.println("    },");
		}
		
		System.out.println("]");
		System.out.println("--------------------------------------------------------------");
	}
	
	public static void cetakMap(List<Sentence> token){
		
		System.out.println("--------------------------------------------------------------");
		System.out.println("|                    Hasil Proses Mapping                    |");
		System.out.println("--------------------------------------------------------------");
		System.out.println("[");
		
		for(Sentence m: token)
		{
			System.out.println("    {kelasFrasa:" + m.getType()  + ", fungsi:"+ m.getFunction() + ", konstituen:");
			
			List<SemanticToken> tm = m.getConstituents();
			
			System.out.println("        [");
			for ( SemanticToken t: tm ) {
				if(t.equals(tm.get(tm.size() - 1))) {
					System.out.println("            {kata:" + t.getToken() + ", kelas:" + t.getType() + ", ontologi:" + t.getOWLType() + "}");
				} else {
					System.out.println("            {kata:" + t.getToken() + ", kelas:" + t.getType() + ", ontologi:" + t.getOWLType() + "},");
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