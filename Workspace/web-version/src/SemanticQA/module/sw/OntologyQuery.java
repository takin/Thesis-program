/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SemanticQA.module.sw;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryBinding;
import de.derivo.sparqldlapi.QueryEngine;
import de.derivo.sparqldlapi.QueryResult;
import de.derivo.sparqldlapi.exceptions.QueryEngineException;
import de.derivo.sparqldlapi.exceptions.QueryParserException;
import SemanticQA.constant.Ontology;
import SemanticQA.constant.Type;
import SemanticQA.model.QueryResultModel;
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
		public static final String SPARQLDL = "sparqldl";
		public static final String INFERED_DATA = "inferedData";
	}
	
	///////////////////////////
	// variabel untuk menyimpan URI dari subjek pertanyaan
	// URI ini nantinya akan digunakan untuk melakukan query informasi tambahan 
	// tentang subjek yang bersangkutan
	// URI ini didapatkan pada saat proses pembentukan query dalam method buidQuery()
	////////////////////////////////////////////////
	private List<String> inferredData;
	private OWLReasoner reasoner;
	private OntologyMapper mapper;
	
	public OntologyQuery(OntologyMapper mapper, OWLReasoner reasoner) {
		this.queryEngine =  QueryEngine.create(mapper.ontology.getOWLOntologyManager(), reasoner);
		this.reasoner = reasoner;
		this.mapper = mapper;
		inferredData = new ArrayList<String>();
	}
	
	public Map<String, Object> execute(List<Sentence> model) throws Exception{
		
		////////////////////
		// ----------------------------
		// Hashmap data hasil query
		// ----------------------------
		// data terdiri berupa:
		// {
		//		text:[] // array list "answerText" hasil query sparql dl,
		//		additionalData: [ list map
		//			about: // nama instance
		//			data:{ // hashmap
		//			}
		//		]
		// }
		//
		// objek ini selanjutnya akan di proses menjadi objek JSON
		// di dalam method AnswerBuilder
		/////////////////////////
		Map<String,Object> result = new HashMap<String, Object>();
		
		///////////////////////////////////////////
		//		listOfAdditionalData: [ list map
		//			about: // nama instance
		//			data:{ // hashmap
		//			}
		//		]
		//////////////////////////////////////////
		List<QueryResultModel> listOfInferedData = new ArrayList<QueryResultModel>();
		
		QueryResult sparqldlQueryResult = null;
		
		try {
			Query query = buildQuery(model);
			sparqldlQueryResult = queryEngine.execute(query);
			
			for ( QueryBinding queryBinding : sparqldlQueryResult ) {
				
				// ambil semua variabel binding dari query sparqldl
				Set<QueryArgument> args = queryBinding.getBoundArgs();
				
				for ( QueryArgument arg:args ) {
					
					QueryArgument item = queryBinding.get(arg);
					
					if ( item.isURI() ) {
					
						String itemValue = item.getValue();
						
						
						////////////////////
						// Untuk objek yang akan dimasukkan ke dalam array additional info
						// lakukan filtering terlebih dahulu.
						// item yang akan diambil hanya item yang berupa subjek atau objek
						// karena kedua item sudah pasti berupa individual yang nantinya 
						// akan dicari propertynya melalui query sparql biasa dengan menggunakan 
						// sesame API.
						// 
						// Adapun query yang dilakukan nantinya dapat berupa query internal ontologi
						// ataupun terhadap endpoint DBPEDIA Indonesia (tergantung URI dari objek yang berssangkutan
						/////////////////////////////////
						if ( arg.getValue().equals("subject") ) {
							inferredData.add(0,itemValue);
						}
						
						if ( arg.getValue().equals("object") ) {
							inferredData.add(itemValue);
						}
					}
				}
			}
			
			///////////
			// Proses query pencarian tambahan informasi dilakukan 
			// mulai dari isi array yang paling belakang karena posisi data yang
			// relevan dengan subjek berada di paling depan sehingga kalau dilakkukan
			// query mulai dari yang paling depan, list data hasil query akan menempatkan 
			// data yang paling relevan (subjek) menjadi posisi yang paling bawah
			////////////////
			for ( int i = 0; i < inferredData.size(); i++ ) {
				
				String inferredObject = inferredData.get(i);
				
				////////////////////////////////
				// Jika individu bukan berasal dari dbpedia
				// maka lakukan pengecekan untuk mencari individu yang sama
				// (individu dengan property owl:sameAs) yang berasal dari dbpedia
				// Jika ditemukan, maka lakukan query sparql terhadap individu yang 
				// berasal dari dbpedia!!
				//////////////////////////////////
				if ( !inferredObject.matches("^(<?http://id.dbpedia).*") ) {
					
					if( inferredObject.startsWith("<") ) {
						inferredObject = inferredObject.substring(1, inferredObject.length());
					}
					
					if ( inferredObject.endsWith(">") ) {
						inferredObject = inferredObject.substring(0, inferredObject.length() - 1);
					}
					
					IRI iri = IRI.create(inferredObject);
					
					OWLNamedIndividual newIndividual = mapper.dataFactory.getOWLNamedIndividual(iri);
					Set<OWLNamedIndividual> listOfSameIndividuals = this.reasoner.getSameIndividuals(newIndividual).getEntities();
					
					if (  listOfSameIndividuals.size() > 0 ) {
						for ( OWLNamedIndividual individu : listOfSameIndividuals ) {
							if ( individu.toString().matches("^(<?http://id.dbpedia).*") ) {
								
								String stringify = individu.toString();
								stringify = stringify.substring(1, stringify.length() - 1);
								/////////
								// cek apakah individual yang sama sudah ada di dalam arraylist inferedList atau tidak
								// Jika ada maka abaikan
								////////////
								if ( !inferredData.contains(stringify) ){
									inferredObject = stringify;
								}
								
								break;
							}
						}
					}
					
				}
				
				QueryResultModel queryResultModel = doSPARQLQuery(inferredObject);
				listOfInferedData.add(queryResultModel);
			}
			
		} catch (QueryParserException | QueryEngineException e) {
			throw new Exception("Tidak dapat melakukan query terhadap basis pengetahuan!");
		}
		
		result.put(ResultKey.SPARQLDL, sparqldlQueryResult);
		result.put(ResultKey.INFERED_DATA, listOfInferedData);
		
		return result;
	}
	
	private QueryResultModel doSPARQLQuery(String instancePath) throws Exception {
		QueryResultModel result = new QueryResultModel();
		Map<String, String> data = new HashMap<String, String>();
		
		String query = "";
		Repository repo = null;
		RepositoryConnection conn = null;
		
		///////////////////////////
		// karena default kembalian hasil mapping di dalam ontology adalah
		// <http://foo.bar/>
		// maka untuk menyeragamkan uri dengan hasil query dari dpbedia,
		// hapus "<" dan ">" agar proses pembentukan query seragam
		////////////////////////////
		if ( instancePath.startsWith("<") ) {
			instancePath = instancePath.substring(1, instancePath.length());
		}
		
		if ( instancePath.endsWith(">") ) {
			instancePath = instancePath.substring(0, instancePath.length() - 1);
		}
		
		if ( instancePath.matches("^(http://id.dbpedia.org).*") ) {
			
			repo = new SPARQLRepository(Ontology.Path.DBPEDIA_ENDPOINT);
			repo.initialize();
			
			try {
				conn = repo.getConnection();
			} catch (OpenRDFException e) {
				throw new Exception("Tidak dapat menghubungi DBPedia Endpoint");
			}
			
			query = "SELECT * WHERE {"
			 			+ "<" + instancePath + "> ?prop ?value . "
			 			+ "FILTER(regex(?prop, \"(id.dbpedia.org/property|(rdf-schema#[(comment|label)])|depiction)\", \"i\"))"
			 		+ "}";
		} 
		
		if ( !instancePath.matches("^(http://id.dbpedia.org).*") ) {
			
			try {
				URL location = new URL(Ontology.Path.DATASET);
				
				repo = new SailRepository(new MemoryStore());
				repo.initialize();
				conn = repo.getConnection(); 
				
				String localPath = "http://semanticweb.techtalk.web.id/ontology/dataset";
				conn.add(location, localPath, RDFFormat.TURTLE);
				
			} catch (OpenRDFException | IOException e) {
				throw new Exception("Tidak dapat me-load dataset lokal");
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
							data.put(prop.stringValue(), val.stringValue());
						}
					}					
				}
			}
			catch (Exception e) {
				throw new Exception("Proses pembentukan hasil query SPARQL gagal");
			}
			
		} catch (OpenRDFException e ) {
			throw new Exception("Proses Query SPARQL Gagagl");
		}
		
		result.setSubject(instancePath);
		result.addData(data);
		
		return result;
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
					
					inferredData.add(listOfObjects.get(1).toString());
					
					analyzedQuery = "{\n"
							+ "Type(?subject," + listOfObjects.get(0) +"),\n"
							+ "PropertyValue(?subject, ?prop, " + listOfObjects.get(1) + ")"
							+ "\n}";
					break;
				case "OPCI":
					
					inferredData.add(listOfObjects.get(2).toString());
					
					analyzedQuery = "{\n"
							+ "Type(" + listOfObjects.get(2) + "," + listOfObjects.get(1) + "),\n"
							+ "PropertyValue(" + listOfObjects.get(2) + ", " + listOfObjects.get(0) + ", ?object),\n"
							+ "DirectType(?object, ?class)"
							+ "\n}";
					break;
				case "OPCOPI":
					
					inferredData.add(listOfObjects.get(3).toString());
					
					analyzedQuery = "{\n "
							+ "Type(?subject," + listOfObjects.get(1) + "),\n "
							+ "PropertyValue(?subject, " + listOfObjects.get(2) + "," + listOfObjects.get(3)+")"
							+ "\n}";
					break;
				case "COPI":
					
					inferredData.add(listOfObjects.get(2).toString());
					
					analyzedQuery = "{\n "
							+ "Type(?subject," + listOfObjects.get(0) + "),\n "
							+ "PropertyValue(?subject, " + listOfObjects.get(1) + "," + listOfObjects.get(2) + ")"
							+ "\n}";
					break;
				case "DPCI":
					
					inferredData.add(listOfObjects.get(2).toString());
					
					analyzedQuery = "{\nType(?subject," + listOfObjects.get(1) + "),\n"
							+ "PropertyValue(?subject," + listOfObjects.get(0) + ", " + listOfObjects.get(2) + ")\n}";
					break;
				case "COPCI":
					
					inferredData.add(listOfObjects.get(3).toString());
					
					analyzedQuery = "{Type(?subject, " + listOfObjects.get(0) + "),"
									+ "PropertyValue(?subject, " + listOfObjects.get(1) + ", " + listOfObjects.get(3) + ")"
							+ "}";
					break;
				case "I":
					
					inferredData.add(listOfObjects.get(0).toString());
					
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
