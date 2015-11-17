package SemanticQA.controllers;

import java.util.List;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import SemanticQA.constant.Ontology;
import SemanticQA.model.MySQLDatabase;
import SemanticQA.model.SemanticToken;
import SemanticQA.model.Sentence;
import SemanticQA.module.nlp.Parser;
import SemanticQA.module.nlp.Tokenizer;
import SemanticQA.module.sw.OntologyMapper;
import SemanticQA.module.sw.OntologyQuery;

class ThesisDesktopVersion {
	
	
	public static void main(String args[]){
		
//		System.out.println("Masukkan pertanyaan: ");
		
//		Scanner s = new Scanner(System.in);
//		String pertanyaan = s.nextLine(); 
		
		String[] pertanyaan = new String[]{
//				"siapa yang terpilih menjadi kepala desa danger tahun 2015",
//				"di mana alamat kantor dinas pendidikan kabupaten lombok timur",
				"di mana letak pantai tanjung an",
				"siapakah bupati kabupaten lombok timur",
				"bupati kabupaten lombok timur siapa",
//				"apa saja wisata budaya di lombok",
//				"di lombok ada wisata budaya apa saja",
//				"siapa yang menjadi kepala desa danger",
//				"apa saja destinasi wisata yang ada di lombok tengah",
//				"apa saja destinasi wisata yang terdapat di lombok tengah"
				};
//		s.close();
		
		String[] ontologies = new String[]{Ontology.Path.ONTOPAR, Ontology.Path.ONTOGEO, Ontology.Path.ONTOGOV,Ontology.Path.DATASET};
		Tokenizer t = new Tokenizer(new MySQLDatabase());
		Parser p = new Parser();
		OntologyMapper mapper = new OntologyMapper(ontologies, Ontology.Path.MERGED_URI);
		
		OWLReasoner reasoner = new Reasoner(mapper.getOntology());
		OntologyQuery query = new OntologyQuery(mapper, reasoner);
		
		for ( String q: pertanyaan ){
//			try {
				List<SemanticToken> tm = t.tokenize(q);
		
				List<Sentence> result = p.parse(tm);
				List<Sentence> mapResult = mapper.map(result);
				
//				cetak(tm);
//				cetakKlausa(result);
//				cetakMap(mapResult);
				query.execute(mapResult);
//			} catch (Exception e) {
//				System.out.println(e.getMessage());
//			}
		}
		
		
//		long startTime = System.currentTimeMillis();
		
//		long endTime = System.currentTimeMillis();
//		long executionTime = endTime - startTime;
		
//		System.out.println("Mapping is executed in: " + executionTime + " seconds");
		
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