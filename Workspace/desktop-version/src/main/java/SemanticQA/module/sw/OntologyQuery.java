/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SemanticQA.module.sw;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.algebra.Str;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryBinding;
import de.derivo.sparqldlapi.QueryEngine;
import de.derivo.sparqldlapi.QueryResult;
import de.derivo.sparqldlapi.exceptions.QueryEngineException;
import de.derivo.sparqldlapi.exceptions.QueryParserException;
import de.derivo.sparqldlapi.types.QueryArgumentType;
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
	
	public static abstract class ResultKey {
		public static final String MAIN = "main";
		public static final String MAIN_TEXT = "mainText";
		public static final String MAIN_DATA = "mainData";
		public static final String ADDITIONAL = "additional";
		public static final String ADDITIONAL_INSTANCE = "additionalInstance";
		public static final String ADDITIONAL_DATA = "additionalData";
	}
	
	///////////////////////////
	// variabel untuk menyimpan URI dari subjek pertanyaan
	// URI ini nantinya akan digunakan untuk melakukan query informasi tambahan 
	// tentang subjek yang bersangkutan
	// URI ini didapatkan pada saat proses pembentukan query dalam method buidQuery()
	////////////////////////////////////////////////
	private OWLObject subjectPath;
	
	public OntologyQuery(OntologyMapper mapper, OWLReasoner reasoner) {
		this.queryEngine =  QueryEngine.create(mapper.ontology.getOWLOntologyManager(), reasoner);
	}
	
	public Map<String, Object> execute(List<Sentence> model){
		
		////////////////////
		// ----------------------------
		// Hashmap data hasil query
		// ----------------------------
		// data terdiri berupa:
		// {
		//		main: { // hashmap
		//			text:[] // arraylist pembentuk jawaban
		//			data:{ // hashmap
						// data hasil query sparql
		//			}
		//		}
		//		additional: [ list map
		//			instance: // nama instance
		//			data:{ // hashmap
		//			}
		//		]
		// }
		//
		// objek ini selanjutnya akan di proses menjadi objek JSON
		// di dalam method AnswerBuilder
		/////////////////////////
		Map<String,Object> result = new HashMap<String, Object>();
		
		List<String> mainAnswerText = new ArrayList<String>();
		
		///////////////////
		// Arraylist untuk menyimpan URI dari instance hasil query
		// yang berkaitan dengan subjek pertanyaan.
		//
		// URI ini selanjutnya akan digunakan untuk melakukan query untuk mengambil
		// data mengenai objek yang bersangkutan, data ini nantinya akan menjadi 
		// data additional dalam hashmap result
		//////////////////////
		List<String> additionalInfoPath = new ArrayList<String>();
		
		try {
			Query query = buildQuery(model);
			QueryResult sparqldlQueryResult = queryEngine.execute(query); 
			
			
			for ( QueryBinding queryBinding : sparqldlQueryResult ) {
				
				// ambil semua variabel binding dari query sparqldl
				Set<QueryArgument> args = queryBinding.getBoundArgs();
				
				for ( QueryArgument arg:args ) {
					
					QueryArgument item = queryBinding.get(arg);
					
					if ( item.isURI() ) {
					
						String itemValue = item.getValue();
						
						////////////////////////
						// masukkan semua hasil binding ke dalam array mainAnswerText
						// karena objek ini akan digunakan untuk membentuk text jawaban
						////////////////////////
						mainAnswerText.add(itemValue);
						
						////////////////////
						// Untuk objek yang akan dimasukkan ke dalam array additional info
						// lakukan filtering terlebih dahulu.
						// item yang akan diambil hanya item yang berupa subjek atau objek
						// karena kedua item sudah pasti berupa individual yang nantinya 
						// akan dicari propertynya melalui query sparql biasa dengan menggunakan 
						// sesame API.
						// 
						// Adapun query yang dilakukan nantinya dapat berupa query internal ontologi
						// ataupun terhadap endpoint DPBEDIA Indonesia (tergantung URI dari objek yang berssangkutan
						/////////////////////////////////
						if ( arg.getValue().matches("(subject|object)") ) {
							additionalInfoPath.add(itemValue);
						}
					}
				}
			}
			
			
			Map<String, String> additionalData = null;
			
			for ( String additionalObject : additionalInfoPath ) {
				additionalData = doSPARQLQuery(additionalObject);
			}
			
			Map<String,String> subjectData = doSPARQLQuery(subjectPath);
			
			for ( String key:subjectData.keySet() ) {
				System.out.println("mainData -> " + key + " <-> " + subjectData.get(key));
			}
			
			for ( String key: additionalData.keySet() ) {
				System.out.println("addData -> " + key + " <-> " + additionalData.get(key));
			}

			
		} catch (QueryParserException | QueryEngineException e) {
			System.out.println("Error: " + e.getMessage());
		}
		
		return result;
	}
	
	
	private Map<String, String> doSPARQLQuery(String instancePath) {
		Map<String, String> result = new HashMap<String, String>();
		String query = "";
		Repository repo = null;
		RepositoryConnection conn = null;
		
		if ( instancePath.matches("^(http://id.dbpedia.org).*") ) {
			
			repo = new SPARQLRepository(Ontology.Path.DBPEDIA_ENDPOINT);
			repo.initialize();
			
			try {
				conn = repo.getConnection();
			} catch (OpenRDFException e) {
				System.out.println(e.getMessage());
			}
			
			query = "SELECT * WHERE {"
			 			+ "<" + instancePath + "> ?prop ?value . "
			 			+ "FILTER(regex(?prop, \"(id.dbpedia.org/property|(rdf-schema#[(comment|label)])|depiction)\", \"i\"))"
			 		+ "}";
		} 
		
		if ( !instancePath.matches("^(http://id.dbpedia.org).*") ) {
			
			try {
				File location = new File("/Users/syamsul/Documents/Thesis-program/Ontologi/dataset-turtle.ttl");
				
				repo = new SailRepository(new MemoryStore());
				repo.initialize();
				conn = repo.getConnection(); 
				
				String localPath = "http://semanticweb.techtalk.web.id/ontology/dataset";
				
				conn.add(location, localPath, RDFFormat.TURTLE);
				
			} catch (OpenRDFException | IOException e) {
				System.out.println(e.getMessage());
			}
			
			query = "SELECT * WHERE {\n"
					+ "<" + instancePath + "> ?prop ?value . \n"
					+ "}";
		}
		try {

			
			TupleQuery tquery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
			
			try(TupleQueryResult tr = tquery.evaluate()) {
			
				List<String> bindings = tr.getBindingNames();
				
				if ( tr.hasNext() ){
					while (tr.hasNext()) {
						
						BindingSet bs = tr.next();
						
						Value prop = bs.getValue(bindings.get(0));
						Value val = bs.getValue(bindings.get(1));
						
						////////
						// ambil hanya property yang memiliki nilai
						///////
						if ( val.stringValue().matches("[a-zA-Z0-9]+.*") ) {
							result.put(prop.stringValue(), val.stringValue());
						}
					}					
				}
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
			
		} catch (OpenRDFException e ) {
			System.out.println(e.getMessage());
		}
		
		return result;
	}
	
	private Map<String, String> doSPARQLQuery(OWLObject instancePath) {
		String ip = instancePath.toString();
		
		///////////////////////////
		// karena default kembalian hasil mapping di dalam ontology adalah
		// <http://foo.bar/>
		// maka untuk menyeragamkan uri dengan hasil query dari dpbedia,
		// hapus "<" dan ">" agar proses pembentukan query seragam
		////////////////////////////
		ip = ip.substring(1, ip.length() - 1);
		
		return doSPARQLQuery(ip);
	}
	
	private Query buildQuery(List<Sentence> model) throws QueryParserException {
		
		String analyzedQuery = "";
		List<OWLObject> listOfObjects = new ArrayList<OWLObject>();
		
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
								listOfObjects.set(listOfObjects.size() - 1, t.getOWLPath());								
							}
							
						} else {
							queryPattern += "C";							
							listOfObjects.add(t.getOWLPath());
						}
						break;
					case Type.Ontology.OBJECT_PROPERTY:
						queryPattern += "OP";
						listOfObjects.add(t.getOWLPath());
						break;
					case Type.Ontology.DATATYPE_PROPERTY:
						queryPattern += "DP";
						listOfObjects.add(t.getOWLPath());
						break;
					case Type.Ontology.INDIVIDUAL:
						queryPattern += "I";
						listOfObjects.add(t.getOWLPath());
						break;
					}
					
					prevTokenType = t.getType();
				}
				
				switch (queryPattern) {
				case "CI":
					
					subjectPath = listOfObjects.get(1);
					
					analyzedQuery = "{\n"
							+ "Type(?subject," + listOfObjects.get(0) +"),\n"
							+ "PropertyValue(?subject, ?prop, " + listOfObjects.get(1) + ")"
							+ "\n}";
					break;
				case "OPCI":
					
					subjectPath = listOfObjects.get(2);
					
					analyzedQuery = "{\n"
							+ "Type(" + listOfObjects.get(2) + "," + listOfObjects.get(1) + "),\n"
							+ "PropertyValue(" + listOfObjects.get(2) + ", " + listOfObjects.get(0) + ", ?object),\n"
							+ "DirectType(?object, ?class)"
							+ "\n}";
					break;
				case "OPCOPI":
					
					subjectPath = listOfObjects.get(3);
					
					analyzedQuery = "{\n "
							+ "Type(?subject," + listOfObjects.get(1) + "),\n "
							+ "PropertyValue(?subject, " + listOfObjects.get(2) + "," + listOfObjects.get(3)+")"
							+ "\n}";
					break;
				case "COPI":
					
					subjectPath = listOfObjects.get(2);
					
					analyzedQuery = "{\n "
							+ "Type(?subject," + listOfObjects.get(0) + "),\n "
							+ "PropertyValue(?subject, " + listOfObjects.get(1) + "," + listOfObjects.get(2) + ")"
							+ "\n}";
					break;
				case "DPCI":
					
					subjectPath = listOfObjects.get(2);
					
					analyzedQuery = "{\nType(?subject," + listOfObjects.get(1) + "),\n"
							+ "PropertyValue(?subject," + listOfObjects.get(0) + ", " + listOfObjects.get(2) + ")\n}";
					break;
				case "I":
					
					subjectPath = listOfObjects.get(0);
					
					analyzedQuery = "{\nDirectType(" + listOfObjects.get(0) + ",?class),\n"
							+ "PropertyValue("+ listOfObjects.get(0) +",?prop, ?object)\n}";
					break;
				}
			}
		}
		
		String query = "select * where " + analyzedQuery;
		return Query.create(query);
	}
	
}
