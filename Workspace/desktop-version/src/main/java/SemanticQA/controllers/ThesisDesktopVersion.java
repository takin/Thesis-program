package SemanticQA.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import SemanticQA.constant.Ontology;
import SemanticQA.helpers.AnswerBuilder;
import SemanticQA.helpers.Printer;
import SemanticQA.model.MySQLDatabase;
import SemanticQA.model.QueryResultModel;
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
			reasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_ASSERTIONS);
			reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS);
			Tokenizer tokenizer = new Tokenizer(new MySQLDatabase());
			for ( String p:pertanyaan() ){
				System.out.println(p);
				OntologyQuery queryEngine = new OntologyQuery(ontologyMapper, reasoner);
				List<SemanticToken> tokenizerResult = tokenizer.tokenize(p);
				List<Sentence> parsingResult = parser.parse(tokenizerResult);
				List<Sentence> ps = clone(parsingResult);
//				Printer.cetakKlausa(ps);
				List<Sentence> mappingResult = ontologyMapper.map(parsingResult);
				long start = System.currentTimeMillis();
				Map<String, List<? extends QueryResultModel>> queryResult = queryEngine.execute(mappingResult);
				long end = System.currentTimeMillis();
				System.out.println("query selesai dalam = " + ((end - start) / 60) + " detik");
				JSONObject finalResult = AnswerBuilder.json(ps,queryResult);
			
//				Printer.cetakMap(mappingResult);
				System.out.println(finalResult);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static List<Sentence> clone(List<Sentence> items){
		List<Sentence> clone = new ArrayList<Sentence>(items.size());
		for ( Sentence s:items ) {
			Sentence st = new Sentence();
			List<SemanticToken> tk = s.getConstituents();
			List<SemanticToken> ntk = new ArrayList<SemanticToken>();
			for ( SemanticToken t:tk ) {
				SemanticToken stk = new SemanticToken();
				stk.setToken(t.getToken());
				stk.setType(t.getType());
				ntk.add(stk);
			}
			
			st.setConstituent(ntk);
			st.setFunction(s.getFunction());
			st.setType(s.getType());
			clone.add(st);
		}
		return clone;
	}
	
	public static String[] pertanyaan(){
		String[] pertanyaan = new String[]{
//				"siapakah ali bin dahlan",
//				"apa itu kabupaten lombok timur",
//				"di mana letak pantai senggigi",
//				"siapa yang terpilih menjadi kepala desa danger tahun 2015",
//				"di mana alamat kantor dinas pendidikan kabupaten lombok timur",
//				"di mana letak pantai tanjung an",
				"siapakah bupati kabupaten lombok timur",
//				"bupati kabupaten lombok timur siapa",
//				"apa wisata pantai terbaik yang ada di kabupaten lombok tengah",
//				"di kabupaten lombok tengah ada wisata budaya apa saja",
//				"siapa yang menjadi kepala desa danger",
//				"apa saja destinasi wisata yang ada di lombok tengah",
//				"apa saja destinasi wisata yang terletak di kabupaten lombok tengah",
//				"di mana letak pantai pink"
				};
		return pertanyaan;
	}
}