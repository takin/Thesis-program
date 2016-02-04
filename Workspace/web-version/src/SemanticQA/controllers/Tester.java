package SemanticQA.controllers;

import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import SemanticQA.constant.Ontology;
import SemanticQA.helpers.AnswerBuilder;
import SemanticQA.model.MySQLDatabase;
import SemanticQA.model.QueryResultModel;
import SemanticQA.model.SemanticToken;
import SemanticQA.model.Sentence;
import SemanticQA.module.nlp.Parser;
import SemanticQA.module.nlp.Tokenizer;
import SemanticQA.module.sw.OntologyMapper;
import SemanticQA.module.sw.OntologyQuery;

public class Tester {

	public static void main(String[] args) {

		String[] ontologies = new String[]{
				Ontology.Path.ONTOPAR, 
				Ontology.Path.ONTOGEO, 
				Ontology.Path.ONTOGOV,
				Ontology.Path.DATASET,
				Ontology.Path.UNIVERSITAS
		};
		
		String question = "siapakah ali bin dahlan";
		
		Tokenizer tokenizer = new Tokenizer(new MySQLDatabase());
		Parser parser = new Parser();
		OntologyMapper ontologyMapper;
		try {
			ontologyMapper = new OntologyMapper(ontologies, Ontology.Path.MERGED_URI);
			OWLOntology ontology = ontologyMapper.getOntology();
			OWLReasoner reasoner = new Reasoner(ontology);
			OntologyQuery queryEngine = new OntologyQuery(ontologyMapper, reasoner);
			
			List<SemanticToken> tokenizerResult = tokenizer.tokenize(question);
			List<Sentence> parsingResult = parser.parse(tokenizerResult);
			List<Sentence> bufferedParseResult = Main.clone(parsingResult);
			List<Sentence> mappingResult = ontologyMapper.map(parsingResult);
			
			Map<String, List<? extends QueryResultModel>> queryResult = ( question.toLowerCase().contains("saja") ) 
					? queryEngine.execute(mappingResult) 
					: queryEngine.execute(mappingResult, OntologyQuery.theQuestionIsSingular);
			
			JSONObject finalResult = AnswerBuilder.json(bufferedParseResult, queryResult);
			
			System.out.println(finalResult);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

}
