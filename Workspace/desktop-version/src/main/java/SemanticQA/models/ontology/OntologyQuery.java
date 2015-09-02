/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SemanticQA.models.ontology;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.reasoner.InferenceType;

import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryBinding;
import de.derivo.sparqldlapi.QueryEngine;
import de.derivo.sparqldlapi.QueryResult;
import de.derivo.sparqldlapi.exceptions.QueryEngineException;
import de.derivo.sparqldlapi.exceptions.QueryParserException;
import SemanticQA.constant.Ontology;
import SemanticQA.constant.Token;

/**
 *
 * @author syamsul
 */
public class OntologyQuery extends OntologyMapper {
    
	private List<Map<String,String>> query;
	private QueryEngine queryEngine;
	
	public OntologyQuery(List<Map<String,String>> query) {
		super();
		this.query = query;
		this.queryEngine =  QueryEngine.create(ontology.getOWLOntologyManager(), reasoner);
	}
	
	public void find(){
		
		try {
			Query q = buildQuery();
			QueryResult r = queryEngine.execute(q);
			
			for(QueryBinding b: r){
				Set<QueryArgument> arg = b.getBoundArgs();
				
				OWLEntity out = null;
				
				for(QueryArgument a: arg){
					
					String argValue = b.get(a).getValue();
				
					IRI iri = IRI.create(argValue);
					
					switch (getType(argValue)) {
					case Ontology.TYPE_CLASS:
						out = dataFactory.getOWLClass(iri);
						break;
					case Ontology.TYPE_INDIVIDUAL:
						out = dataFactory.getOWLNamedIndividual(iri);
						break;
					}
					
					System.out.println(getShortForm(out));
					
				}
			}
			
		} catch (QueryParserException | QueryEngineException e) {
			e.printStackTrace();
		}
		
	}
	
	private Query buildQuery() throws QueryParserException{
		
		String q = "Select ?subject where {";
		
		Map<String, String> lastItem = query.get(query.size() - 1);
		
		for(Map<String,String> qItem: query){
			
			String semanticType = qItem.get(Token.KEY_TOKEN_SEMANTIC_TYPE);
			String word = qItem.get(Token.KEY_TOKEN_WORD);
			Map<String,Object> o = getOWLObject(word, semanticType);
			
			switch (semanticType) {
			case Ontology.TYPE_CLASS:
				q += "Type(?subject, "+ o.get(Ontology.KEY_OBJECT_URI) +")";
				break;
			case Ontology.TYPE_INDIVIDUAL:
				q += "PropertyValue(?subject,?y,"+ o.get(Ontology.KEY_OBJECT_URI) +")";
				break;
			}
			
			q += (qItem == lastItem) ? "}" : ",";
		}
		
		return Query.create(q);
	}
	
}
