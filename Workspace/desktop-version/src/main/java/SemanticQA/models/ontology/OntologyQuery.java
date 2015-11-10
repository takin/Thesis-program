/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SemanticQA.models.ontology;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;

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
import SemanticQA.models.nlp.TokenModel;

/**
 *
 * @author syamsul
 */
public class OntologyQuery {

	private QueryEngine queryEngine;
	private OntologyMapper ontologyMapper;
	
	public OntologyQuery(OntologyMapper mapper) {
		this.ontologyMapper = mapper;
		this.queryEngine =  QueryEngine.create(mapper.ontology.getOWLOntologyManager(), mapper.reasoner);
	}
	
	public void execute(List<QuestionModel> model){
		
		try {
			
			String q = buildQuery(model);
			QueryResult r = queryEngine.execute(Query.create(q));
			System.out.println(r);
		} catch (QueryParserException | QueryEngineException e) {
			e.printStackTrace();
		}
		
	}
	
	private String buildQuery(List<QuestionModel> model) throws QueryParserException{
		
		String analyzedQuery = "";
		
		for ( int modelIndex = 0; modelIndex < model.size(); modelIndex++ ) {
			
			QuestionModel m = model.get(modelIndex);
			
			if ( !m.getType().matches("(" + Token.TYPE_PRONOMINA + "|" + Token.TYPE_FRASA_PRONOMINAL + ")") ) {
				
				List<TokenModel> tm = m.getConstituents();
				TokenModel prevToken = null;
				
				for ( int tokenModelIndex = 0; tokenModelIndex < tm.size(); tokenModelIndex++ ) {
					TokenModel t = tm.get(tokenModelIndex);
					
					if ( prevToken != null && prevToken.getTokenOWLType().equals(t.getTokenOWLType()) ) {
						continue;
					}
					
					switch (t.getTokenOWLType()) {
					
					case Ontology.TYPE_CLASS:
						
						analyzedQuery += "Type(?object, "+ t.getOntologyObject() + ")";
						
						break;
					
					case Ontology.TYPE_INDIVIDUAL:
						
						if ( prevToken.getTokenOWLType().equals(Ontology.TYPE_CLASS) ) {
							analyzedQuery += "PropertyValue(?object, ?prop, " + t.getOntologyObject() + ")"; 
						}
						
						break;
					}
					
					if ( !t.equals(tm.get(tm.size() - 1))) {
						analyzedQuery += ",\n";
					}
					prevToken = t;
				}
				
			}
		}
		
		String query = "select * where {\n" + analyzedQuery + "\n}";
		return query;
	}
}
