/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SemanticQA.module.sw;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLObject;

import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryBinding;
import de.derivo.sparqldlapi.QueryEngine;
import de.derivo.sparqldlapi.QueryResult;
import de.derivo.sparqldlapi.exceptions.QueryEngineException;
import de.derivo.sparqldlapi.exceptions.QueryParserException;
import SemanticQA.constant.Ontology;
import SemanticQA.constant.Type;
import SemanticQA.helpers.StringManipulation;
import SemanticQA.model.SemanticToken;
import SemanticQA.model.Sentence;

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
	
	public QueryResult execute(List<Sentence> model){
		
		QueryResult result = null;
		
		try {
			
			String q = buildQuery(model);
			
			System.out.println(q);
			
			result = queryEngine.execute(Query.create(q));
			
			for ( QueryBinding b:result ) {
				Set<QueryArgument> arg = b.getBoundArgs();
				for (QueryArgument a: arg) {
					
					String res = ontologyMapper.getShortForm(b.get(a).getValue());
					
					res = StringManipulation.split(res);
					res = StringManipulation.capitalize(res);
					
					System.out.println(res);
				}
				
			}
			
			
		} catch (QueryParserException | QueryEngineException e) {
			System.out.println("Error: " + e.getMessage());
		}
		
		return result;
	}
	
	
	private String buildQuery(List<Sentence> model) throws QueryParserException{
		
		String analyzedQuery = "";
		
		for ( int modelIndex = 0; modelIndex < model.size(); modelIndex++ ) {
			
			Sentence m = model.get(modelIndex);
			String pattern = "";
			List<SemanticToken> tm = m.getConstituents();
			
			if ( !m.getType().matches("("+Type.Phrase.FRASA_PRONOMINAL + "|" + Type.Token.PRONOMINA + ")") ) {
				
				List<OWLObject> ontologyObject = new ArrayList<OWLObject>();
				
				for ( SemanticToken t:tm ) {
					
					switch (t.getOWLType()) {
					case Ontology.TYPE_CLASS:
						if ( !pattern.matches("C")  ) {
							pattern += "C";
							ontologyObject.add(t.getOWLPath());
						}
						break;
					case Ontology.TYPE_OBJECT_PROPERTY:
						pattern += "OP";
						ontologyObject.add(t.getOWLPath());
						break;
					case Ontology.TYPE_DATATYPE_PROPERTY:
						pattern += "DP";
						ontologyObject.add(t.getOWLPath());
						break;
					case Ontology.TYPE_INDIVIDUAL:
						pattern += "I";
						ontologyObject.add(t.getOWLPath());
						break;
					}
				}
				
				switch (pattern) {
				case "CI":
					analyzedQuery = "Type(?object," + ontologyObject.get(0) +"),"
							+ "PropertyValue(?object, ?prop, " + ontologyObject.get(1) + ")";
					break;
				case "OPCI":
					analyzedQuery = "Type(" + ontologyObject.get(2) + "," + ontologyObject.get(1) + "),"
							+ "PropertyValue(" + ontologyObject.get(2) + ", " + ontologyObject.get(0) + ", ?object)";
					break;
				case "DPCI":
					analyzedQuery = "Type(?object," + ontologyObject.get(1) + "),"
							+ "PropertyValue(?object," + ontologyObject.get(0) + ", " + ontologyObject.get(2) + ")";
					break;
				}
			}
		}
		
		String query = "select distinct ?object where {\n" + analyzedQuery + "\n}";
		return query;
	}
}
