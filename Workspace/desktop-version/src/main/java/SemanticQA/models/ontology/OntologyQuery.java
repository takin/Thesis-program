/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SemanticQA.models.ontology;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;

import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryBinding;
import de.derivo.sparqldlapi.QueryEngine;
import de.derivo.sparqldlapi.QueryResult;
import de.derivo.sparqldlapi.exceptions.QueryEngineException;
import de.derivo.sparqldlapi.exceptions.QueryParserException;
import SemanticQA.constant.Ontology;
import SemanticQA.constant.Token;
import SemanticQA.models.nlp.QuestionModel;

/**
 *
 * @author syamsul
 */
public class OntologyQuery {

	private QueryEngine queryEngine;
	private OntologyLoader ontologyLoader;
	
	public OntologyQuery(OntologyLoader ontologyLoader) {
		this.ontologyLoader = ontologyLoader;
		this.queryEngine =  QueryEngine.create(ontologyLoader.getOntology().getOWLOntologyManager(), ontologyLoader.getReasoner());
	}
	
	public void execute(List<QuestionModel> model){
		
		try {
			Query q = buildQuery();
			QueryResult r = queryEngine.execute(q);
			
		} catch (QueryParserException | QueryEngineException e) {
			e.printStackTrace();
		}
		
	}
	
	private Query buildQuery() throws QueryParserException{
		
		String q = "Select ?subject where {";
		
		
		
		return Query.create(q);
	}
	
}
