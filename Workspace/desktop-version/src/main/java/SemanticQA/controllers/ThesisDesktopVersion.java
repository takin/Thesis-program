package SemanticQA.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jettison.json.JSONObject;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import de.derivo.sparqldlapi.QueryResult;
import SemanticQA.constant.Ontology;
import SemanticQA.helpers.AnswerBuilder;
import SemanticQA.helpers.Printer;
import SemanticQA.model.MySQLDatabase;
import SemanticQA.model.SemanticToken;
import SemanticQA.model.Sentence;
import SemanticQA.module.nlp.Parser;
import SemanticQA.module.nlp.Tokenizer;
import SemanticQA.module.sw.OntologyMapper;
import SemanticQA.module.sw.OntologyQuery;

class ThesisDesktopVersion {
	
	public static void main(String args[]){
//		OntologyQuery.findOnDBPedia();
		
		String[] ontologies = new String[]{
				Ontology.Path.ONTOPAR, 
				Ontology.Path.ONTOGEO, 
				Ontology.Path.ONTOGOV,
				Ontology.Path.DATASET
		};
		
		try {
			
			
			OntologyMapper ontologyMapper = new OntologyMapper(ontologies, Ontology.Path.MERGED_URI);
			Parser parser = new Parser();
			OWLOntology ontology = ontologyMapper.getOntology();
			OWLReasoner reasoner = new Reasoner(ontology);
			Tokenizer tokenizer = new Tokenizer(new MySQLDatabase());
			for ( String p:pertanyaan() ){
				System.out.println(p);
				OntologyQuery queryEngine = new OntologyQuery(ontologyMapper, reasoner);
				List<SemanticToken> tokenizerResult = tokenizer.tokenize(p);
//				Printer.cetak(tokenizerResult);
				List<List<Sentence>> parsingResult = parser.parse(tokenizerResult);
//				List<List<Sentence>> ps = clone(parsingResult);
//				Printer.cetakKlausa(ps);
//				List<Sentence> mappingResult = ontologyMapper.map(parsingResult);
//				Map<String, Object> queryResult = queryEngine.execute(mappingResult);
//				JSONObject finalResult = AnswerBuilder.json(ps,queryResult);
			
//				Printer.cetakMap(mappingResult);
//				System.out.println(finalResult);
			}
		} catch (Exception e) {
			
		}
	}
	
	public static List<List<Sentence>> clone(List<List<Sentence>> items){
		List<List<Sentence>> clone = new ArrayList<List<Sentence>>(items.size());
		
		for ( List<Sentence> sentences : items ) {
			
			List<Sentence> newSentences = new ArrayList<Sentence>();
			
			for ( Sentence sentence:sentences ) {
				
				Sentence newSentence = new Sentence();
				List<SemanticToken> newConstituents = new ArrayList<SemanticToken>();
				List<SemanticToken> constituents = sentence.getConstituents();
				
				for ( SemanticToken token : constituents ) {
					SemanticToken newToken = new SemanticToken();
					newToken.setToken(token.getToken());
					newToken.setType(token.getType());
					newConstituents.add(newToken);					
				}
				
				newSentence.setConstituent(newConstituents);
				newSentence.setFunction(sentence.getFunction());
				newSentence.setType(sentence.getType());
			}
			clone.add(newSentences);			
		}
		return clone;
	}
	
	public static String[] pertanyaan(){
		String[] pertanyaan = new String[]{
				"siapa bupati kabupaten lombok timur dan bupati kabupaten lombok tengah"
//				"siapakah ali bin dahlan",
//				"apa itu kabupaten lombok timur",
//				"di mana letak pantai senggigi",
//				"siapa yang terpilih menjadi kepala desa danger tahun 2015",
//				"di mana alamat kantor dinas pendidikan kabupaten lombok timur",
//				"di mana letak pantai tanjung an",
//				"siapakah bupati kabupaten lombok timur",
//				"bupati kabupaten lombok timur siapa",
//				"apa wisata pantai terbaik yang ada di kabupaten lombok tengah",
//				"di kabupaten lombok tengah ada wisata budaya apa saja",
//				"siapa yang menjadi kepala desa danger",
//				"apa saja destinasi wisata yang ada di lombok tengah",
//				"apa saja destinasi wisata yang terdapat di kabupaten lombok tengah"
				};
		return pertanyaan;
	}
}