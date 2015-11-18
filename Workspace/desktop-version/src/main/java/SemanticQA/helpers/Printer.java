package SemanticQA.helpers;

import java.util.List;

import SemanticQA.model.SemanticToken;
import SemanticQA.model.Sentence;

public class Printer {

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
