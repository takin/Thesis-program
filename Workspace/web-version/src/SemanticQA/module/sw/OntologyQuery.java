/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SemanticQA.module.sw;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryEngine;
import de.derivo.sparqldlapi.QueryResult;
import de.derivo.sparqldlapi.exceptions.QueryEngineException;
import de.derivo.sparqldlapi.exceptions.QueryParserException;
import SemanticQA.constant.Ontology;
import SemanticQA.constant.Type;
import SemanticQA.model.SemanticToken;
import SemanticQA.model.Sentence;

/**
 *
 * @author syamsul
 */
public class OntologyQuery {

	private QueryEngine queryEngine;
	private String queryPattern = "";
	private RepositoryConnection sesameRepositoryConnection;
	
	public OntologyQuery(OntologyMapper mapper, OWLReasoner reasoner) {
		this.queryEngine =  QueryEngine.create(mapper.ontology.getOWLOntologyManager(), reasoner);
		Repository repo = new SPARQLRepository(Ontology.Path.DBPEDIA_ENDPOINT).initialize();
		this.sesameRepositoryConnection = repo.getConnection();
	}
	
	public QueryResult execute(List<Sentence> model){
		QueryResult result = null;
		try {
			String q = buildQuery(model);
			result = queryEngine.execute(Query.create(q)); 
		} catch (QueryParserException | QueryEngineException e) {
			System.out.println("Error: " + e.getMessage());
		}
		
		return result;
	}
	
	private void findOnDbpedia(String path){
		
		Repository repo = new SPARQLRepository(path)
		
	}
	
	private String buildQuery(List<Sentence> model) throws QueryParserException{
		
		String analyzedQuery = "";
		List<OWLObject> ontologyObject = new ArrayList<OWLObject>();
		
		for ( int modelIndex = 0; modelIndex < model.size(); modelIndex++ ) {
			
			Sentence m = model.get(modelIndex);
			List<SemanticToken> tm = m.getConstituents();
			
			String prevTokenType = null;
			
			if ( !m.getType().matches("("+Type.Phrase.PRONOMINAL + "|" + Type.Token.PRONOMINA + ")") ) {
				
				for ( SemanticToken t:tm ) {
					
					switch (t.getOWLType()) {
					case Type.Ontology.CLASS:
						if ( queryPattern.matches("C")){
							
							if ( !prevTokenType.equals(Type.Token.NOMINA) ){
								ontologyObject.set(ontologyObject.size() - 1, t.getOWLPath());								
							}
							
						} else {
							queryPattern += "C";							
							ontologyObject.add(t.getOWLPath());
						}
						break;
					case Type.Ontology.OBJECT_PROPERTY:
						queryPattern += "OP";
						ontologyObject.add(t.getOWLPath());
						break;
					case Type.Ontology.DATATYPE_PROPERTY:
						queryPattern += "DP";
						ontologyObject.add(t.getOWLPath());
						break;
					case Type.Ontology.INDIVIDUAL:
						queryPattern += "I";
						ontologyObject.add(t.getOWLPath());
						break;
					}
					
					prevTokenType = t.getType();
				}
				
				switch (queryPattern) {
				case "CI":
					analyzedQuery = "{\n"
							+ "Type(?object," + ontologyObject.get(0) +"),\n"
							+ "PropertyValue(?object, ?prop, " + ontologyObject.get(1) + ")"
							+ "\n}";
					break;
				case "OPCI":
					analyzedQuery = "{\n"
							+ "Type(" + ontologyObject.get(2) + "," + ontologyObject.get(1) + "),\n"
							+ "PropertyValue(" + ontologyObject.get(2) + ", " + ontologyObject.get(0) + ", ?object),\n"
							+ "DirectType(?object, ?type)"
							+ "\n}";
					break;
				case "OPCOPI":
					analyzedQuery = "{\n "
							+ "Type(?subject,"+ontologyObject.get(1)+"),\n "
							+ "PropertyValue(?subject, "+ontologyObject.get(2)+","+ontologyObject.get(3)+")"
							+ "\n}";
					break;
				case "COPI":
					analyzedQuery = "{\n "
							+ "Type(?subject,"+ontologyObject.get(0)+"),\n "
							+ "PropertyValue(?subject, "+ontologyObject.get(1)+","+ontologyObject.get(2)+")"
							+ "\n}";
					break;
				case "DPCI":
					analyzedQuery = "{\nType(?object," + ontologyObject.get(1) + "),\n"
							+ "PropertyValue(?object," + ontologyObject.get(0) + ", " + ontologyObject.get(2) + ")\n}";
					break;
				case "I":
					analyzedQuery = "{\nDirectType(" + ontologyObject.get(0) + ",?type),\n"
							+ "PropertyValue("+ ontologyObject.get(0) +",?prop, ?value)\n}";
					break;
				}
			}
		}
		
		String query = "select * where " + analyzedQuery;
		return query;
	}
}
